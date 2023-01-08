package com.constituentconnect.database

import com.constituentconnect.database.UserSettings.userID
import com.constituentconnect.models.UpdateUserRequest
import com.constituentconnect.models.User
import com.constituentconnect.models.UserSettings
import com.constituentconnect.plugins.AuthenticationException
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(Users)

    var authId by Users.authId
    var firstName by Users.firstName
    var lastName by Users.lastName
    var displayName by Users.displayName
    var phone by Users.phone
    var email by Users.email
    var created by Users.created
    var updated by Users.updated
}

object Users : IntIdTable("core.users") {
    val authId = text("authId")
    val firstName = text("firstName").nullable()
    val lastName = text("lastName").nullable()
    val displayName = text("displayName").nullable()
    val phone = integer("phone").nullable()
    val email = text("email")
    val created = timestamp("created").default(Instant.now())
    val updated = timestamp("updated").default(Instant.now())
}

fun createNewUser(username: String, userEmail: String): User {
    val user = transaction {
        UserEntity.new {
            authId = username
            email = userEmail
        }.let {
            User(
                it.id.value,
                it.authId,
                it.firstName,
                it.lastName,
                it.displayName,
                it.phone,
                it.email,
                null,
                null,
                it.created,
                it.updated
            )
        }
    }

    val userSettings = transaction {
        UserSettingEntity.new {
            userID = user.id
        }
    }

    user.settings = userSettings.let {
        UserSettings(
            it.voteTextNotificationEnabled,
            it.twitterVotePostEnabled
        )
    }

    return user
}

fun updateUserInfo(updateUserRequest: UpdateUserRequest) {
    transaction {
        val userId = Users.select {
            Users.id eq updateUserRequest.id
        }.limit(1)
            .singleOrNull()
            ?.let {
                it[Users.id].toString().toInt()
            } ?: throw AuthenticationException()

        val user = UserEntity.findById(userId)
        user?.firstName = updateUserRequest.firstName
        user?.lastName = updateUserRequest.lastName
        user?.displayName = updateUserRequest.displayName
        user?.phone = updateUserRequest.phone

        user
    }
}
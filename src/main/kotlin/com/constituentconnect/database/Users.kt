package com.constituentconnect.database

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import java.util.*

class UserEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserEntity>(Users)

    var authId by Users.authId
    var firstName by Users.firstName
    var lastName by Users.lastName
    var displayName by Users.displayName
    var phone by Users.phone
    var email by Users.email
    var created by Users.created
    var updated by Users.updated
}

object Users : UUIDTable("core.users") {
    val authId = text("authId")
    val firstName = text("firstName").nullable()
    val lastName = text("lastName").nullable()
    val displayName = text("displayName").nullable()
    val phone = integer("phone").nullable()
    val email = text("email")
    val created = timestamp("created").default(Instant.now())
    val updated = timestamp("updated").default(Instant.now())
}
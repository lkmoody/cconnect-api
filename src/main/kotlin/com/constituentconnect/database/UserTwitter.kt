package com.constituentconnect.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import java.util.UUID

class UserTwitterEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserTwitterEntity>(UserTwitters)

    var userId by UserTwitters.userId
    var token by UserTwitters.token
    var secret by UserTwitters.secret
    var requestAccessToken by UserTwitters.requestAccessToken
    var requestAccessTokenSecret by UserTwitters.requestAccessTokenSecret
    var created by UserTwitters.created
    var updated by UserTwitters.updated
}

object UserTwitters : UUIDTable("core.user-twitters") {
    val userId = uuid("userId")
    val token = text("token").default("")
    val secret = text("secret").default("")
    val requestAccessToken = text("requestAccessToken").default("")
    val requestAccessTokenSecret = text("requestAccessTokenSecret").default("")
    val created = timestamp("created").default(Instant.now())
    val updated = timestamp("updated").default(Instant.now())
}

package com.constituentconnect.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

class UserTwitterEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserTwitterEntity>(UserTwitters)

    var userId by UserTwitters.userId
    var token by UserTwitters.token
    var secret by UserTwitters.secret
    var created by UserTwitters.created
    var updated by UserTwitters.updated
}

object UserTwitters : IntIdTable("core.user-twitters") {
    val userId = integer("userId")
    val token = text("token")
    val secret = text("secret")
    val created = timestamp("created").default(Instant.now())
    val updated = timestamp("updated").default(Instant.now())
}

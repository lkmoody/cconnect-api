package com.constituentconnect.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

class GroupEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<GroupEntity>(Groups)

    var name by Groups.name
    var displayName by Groups.displayName
    var created by Groups.created
    var updated by Groups.updated
}

object Groups : IntIdTable("core.groups") {
    val name = text("name")
    val displayName = text("displayName")
    val created = timestamp("created").default(Instant.now())
    val updated = timestamp("updated").default(Instant.now())
}

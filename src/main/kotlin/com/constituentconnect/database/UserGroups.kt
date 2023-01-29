package com.constituentconnect.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

class UserGroupEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserGroupEntity>(UserGroups)

    var userId by UserGroups.userId
    var groupId by UserGroups.groupId
}

object UserGroups : UUIDTable("core.user-groups") {
    val userId = uuid("userId")
    val groupId = uuid("groupId")
}

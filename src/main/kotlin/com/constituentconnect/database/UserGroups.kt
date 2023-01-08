package com.constituentconnect.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

class UserGroupEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserGroupEntity>(UserGroups)

    var userId by UserGroups.userId
    var groupId by UserGroups.groupId
}

object UserGroups : IntIdTable("core.user-groups") {
    val userId = integer("userId")
    val groupId = integer("groupId")
}

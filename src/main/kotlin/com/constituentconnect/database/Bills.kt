package com.constituentconnect.database

import com.constituentconnect.models.Bill
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

class BillEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<BillEntity>(Bills)

    var name by Bills.name
    var description by Bills.description
    var voteClosed by Bills.voteClosed
    var groupId by Bills.groupId
    var created by Bills.created
    var updated by Bills.updated
}


object Bills : UUIDTable("core.bills") {
    val name = text("name")
    val description = text("description")
    val voteClosed = bool("voteClosed")
    val groupId = uuid("groupId")
    val created = timestamp("created")
    val updated = timestamp("updated")
}


fun billToApi(billEntity: BillEntity): Bill = Bill(
    billEntity.id.value,
    billEntity.name,
    billEntity.description,
    billEntity.voteClosed,
    billEntity.groupId,
    billEntity.created,
    billEntity.updated
)
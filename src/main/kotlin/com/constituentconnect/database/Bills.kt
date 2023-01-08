package com.constituentconnect.database

import com.constituentconnect.models.Bill
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

class BillEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BillEntity>(Bills)

    var name by Bills.name
    var description by Bills.description
    var voteClosed by Bills.voteClosed
    var groupId by Bills.groupId
    var created by Bills.created
    var updated by Bills.updated
}


object Bills : IntIdTable("core.bills") {
    val name = text("name")
    val description = text("description")
    val voteClosed = bool("voteClosed")
    val groupId = integer("groupId")
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
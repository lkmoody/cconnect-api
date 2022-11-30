package com.example.database

import com.example.models.Bill
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

class BillEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BillEntity>(Bills)

    val name by Bills.name
    val description by Bills.description
    val voteClosed by Bills.voteClosed
    val created by Bills.created
    val updated by Bills.updated
}


object Bills : IntIdTable("core.bills") {
    val name = text("name")
    val description = text("description")
    val voteClosed = bool("voteClosed")
    val created = timestamp("created")
    val updated = timestamp("updated")
}


fun billToApi(billEntity: BillEntity): Bill = Bill(
    billEntity.id.value,
    billEntity.name,
    billEntity.description,
    billEntity.voteClosed,
    billEntity.created,
    billEntity.updated
)
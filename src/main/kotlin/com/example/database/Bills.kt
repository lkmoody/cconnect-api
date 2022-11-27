package com.example.database

import com.example.models.Bill
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

class BillEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BillEntity>(Bills)
    val name by Bills.name
}


object Bills : IntIdTable("core.bills") {
    val name = text("name")
}


fun billToApi(billEntity: BillEntity): Bill = Bill(billEntity.id.value, billEntity.name)
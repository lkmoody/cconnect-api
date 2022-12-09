package com.constituentconnect.database

import com.constituentconnect.database.DatabaseFactory.dbQuery
import com.constituentconnect.models.Bill
import com.constituentconnect.routes.CreateBillRequest
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.transactions.transaction

class DAO() {
    val bills = BillEntity
}

//    suspend fun getBills(): List<Bill> = dbQuery {
//        transaction {
//            BillEntity.all().map { bill ->
//                billToApi(bill)
//            }
//        }
//    }
//
//    suspend fun getBills(id: Int): Bill = dbQuery {
//        transaction {
//            val billEntity = BillEntity.findById(id) ?: throw NotFoundException()
//            billToApi(billEntity)
//        }
//    }
//
//    fun createBill(bill: CreateBillRequest): Int {
//        val billEntity = BillEntity(name: )
//        BillEntity.new()
//        return 0
//    }
//}
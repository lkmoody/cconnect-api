package com.example.database

import com.example.database.DatabaseFactory.dbQuery
import com.example.models.Bill

class DAO {
    suspend fun getBills(): List<Bill> = dbQuery {
            BillEntity.all().map {bill ->
                billToApi(bill)
            }
    }
}
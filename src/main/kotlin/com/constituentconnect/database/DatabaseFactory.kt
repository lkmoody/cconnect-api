package com.constituentconnect.database

import io.ktor.server.application.*
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object DatabaseFactory {
    fun initialize(url: String, user: String, password: String) {
        val driverClassName = "org.postgresql.Driver"
        val database = Database.connect(url, driverClassName, user, password)
        TransactionManager.defaultDatabase = database
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

private var daoInstance: DAO? = null

val Application.dao: DAO
    get() = daoInstance ?: throw ApplicationConfigurationException("Unable to connect to the database.")

fun Application.configureDatabase() {
    val url = environment.config.property("database.url").getString()
    val user = environment.config.property("database.user").getString()
    val password = environment.config.property("database.password").getString()

    DatabaseFactory.initialize(url, user, password)
    daoInstance = DAO()
}

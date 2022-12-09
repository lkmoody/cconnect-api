package com.constituentconnect

import com.constituentconnect.database.configureDatabase
import io.ktor.server.application.*
import com.constituentconnect.plugins.*

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    configureSecurity()
    configureCORS()
    configureDatabase()
    configureRouting()
    configureSerialization()
}

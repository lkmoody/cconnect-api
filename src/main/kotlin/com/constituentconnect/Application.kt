package com.constituentconnect
import io.ktor.server.application.*
import com.constituentconnect.plugins.*
import com.constituentconnect.plugins.integrations.configureCognito

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    configureSecurity()
    configureCORS()
    configureRouting()
    configureCognito()
    configureSerialization()
}

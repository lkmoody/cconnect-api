package com.constituentconnect
import com.constituentconnect.database.configureDatabase
import io.ktor.server.application.*
import com.constituentconnect.plugins.*
import com.constituentconnect.plugins.integrations.configureCognito
import org.flywaydb.core.Flyway

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    val flyway = Flyway.configure().dataSource(
        environment.config.property("database.url").getString(),
        environment.config.property("database.user").getString(),
        environment.config.property("database.password").getString()
    ).schemas("core").load()
    flyway.baseline()
    flyway.migrate()

    configureSecurity()
    configureCORS()
    configureDatabase()
    configureRouting()
    configureCognito()
    configureSerialization()
}

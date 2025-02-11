package com.constituentconnect.plugins

import com.constituentconnect.routes.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    routing {
        route("/api/v1") {
            // Health check end point
            get("/health") {
                call.respond(HttpStatusCode.OK, "The server is healthy.")
            }

            // Authenticated Routes
            authenticate {
                testRouting()
            }
        }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
}

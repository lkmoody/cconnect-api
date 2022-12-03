package com.example.plugins

import com.example.routes.billRouting
import com.example.routes.voteDetailRouting
import com.example.routes.voteRouting
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    routing {
        route("/api/v1") {
            get("/health") {
                call.respond(HttpStatusCode.OK)
            }

            //authenticate {
            billRouting()
            voteRouting()
            voteDetailRouting()
            //}
        }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
}

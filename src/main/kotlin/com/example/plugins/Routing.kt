package com.example.plugins

import com.example.routes.billRouting
import com.example.routes.customerRouting
import com.example.routes.voteRouting
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.request.*

fun Application.configureRouting() {

    routing {
        get("/health") {
            call.respond(HttpStatusCode.OK)
        }
        authenticate {
            customerRouting()
            voteRouting()
            billRouting()
        }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
        }
    }
}

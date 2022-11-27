package com.example.plugins

import com.example.routes.billRouting
import com.example.routes.customerRouting
import com.example.routes.voteRouting
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*

fun Application.configureRouting() {

    routing {
        customerRouting()
        voteRouting()
        billRouting()
    }
}

package com.constituentconnect.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRouting() {
    route("/current-user") {
        getCurrentUser()
    }
}

fun Route.getCurrentUser() {
    get {
        call.respond(HttpStatusCode.OK, "Current user!")
    }
}
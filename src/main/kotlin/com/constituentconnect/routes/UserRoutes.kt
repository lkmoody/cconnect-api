package com.constituentconnect.routes

import com.constituentconnect.plugins.getCurrentUser
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
        val user = call.getCurrentUser()
        call.respondNullable(HttpStatusCode.OK, user)
    }
}
package com.constituentconnect.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.testRouting() {
    route("/test") {
        testRoute()
    }
}

fun Route.testRoute() {
    get {
        println("In here")
        call.respond(HttpStatusCode.OK, "Leroy")
    }
}
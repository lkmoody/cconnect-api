package com.example.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.voteRouting() {
    route("/vote") {
        get {
            call.respondText("Yolo", status = HttpStatusCode.OK)
        }
        get("{id?}") {

        }
        post {

        }
        delete("{id?}") {

        }
    }
}

private fun Route.getVotes(): HttpStatusCode {
    return HttpStatusCode(200, "Success")
}
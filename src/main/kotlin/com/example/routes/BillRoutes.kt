package com.example.routes

import com.example.models.Bill
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDateTime

fun Route.billRouting() {
    route("/bill") {
        get {
            val bill = Bill("18175d44-a2f6-4bf9-839a-833a2606c1ac", "Bill 123", "This is a test", false, LocalDateTime.now(), LocalDateTime.now())
            call.respond(HttpStatusCode.OK, bill)
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
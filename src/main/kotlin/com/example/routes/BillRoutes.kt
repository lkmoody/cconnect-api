package com.example.routes

import com.example.database.dao
import com.example.models.Bill
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDateTime

fun Route.billRouting() {
    route("/bill") {
        get {
            val response = application.dao.getBills()

            call.respond(HttpStatusCode.OK, response)
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
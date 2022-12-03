package com.example.routes

import com.example.database.VoteDetailEntity
import com.example.database.voteDetailToApi
import com.example.models.CreateVoteDetailRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import javax.naming.ServiceUnavailableException

fun Route.voteDetailRouting() {
    route("/vote-detail/{id}") {
        getVoteDetail()
    }

    route("/vote-detail") {
        createVoteDetail()
    }
}

private fun Route.getVoteDetail() {
    get {
        try {
            val id = call.parameters["id"]?.toInt() ?: 0

            val voteDetail = transaction {
                VoteDetailEntity.findById(id) ?: throw NotFoundException()
            }

            call.respond(HttpStatusCode.OK, voteDetailToApi(voteDetail))
        } catch (e: NotFoundException) {
            call.respond(HttpStatusCode.NotFound)
        } catch (e: Exception) {
            e.printStackTrace()
            throw ServiceUnavailableException()
        }
    }
}

private fun Route.createVoteDetail() {
    post {
        try {
            val createVoteDetailRequest = call.receive<CreateVoteDetailRequest>()

            val voteDetail = transaction {
                VoteDetailEntity.new {
                    voteId = createVoteDetailRequest.voteId
                    vote = createVoteDetailRequest.vote
                    pros = createVoteDetailRequest.pros ?: ""
                    cons = createVoteDetailRequest.cons ?: ""
                    reasoning = createVoteDetailRequest.reasoning
                    created = Instant.now()
                    updated = Instant.now()
                }
            }


            call.respond(HttpStatusCode.Created, voteDetailToApi(voteDetail))
        } catch (e: Exception) {
            e.printStackTrace()
            throw ServiceUnavailableException()
        }
    }
}
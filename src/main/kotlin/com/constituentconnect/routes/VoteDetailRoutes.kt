package com.constituentconnect.routes

import com.constituentconnect.database.VoteDetailEntity
import com.constituentconnect.database.Votes
import com.constituentconnect.database.voteDetailToApi
import com.constituentconnect.models.CreateVoteDetailRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
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

            transaction {
                Votes.update({Votes.id eq voteDetail.voteId}) {
                    it[voteDetailId] = voteDetail.id.toString().toInt()
                }
            }

            call.respond(HttpStatusCode.Created, voteDetailToApi(voteDetail))
        } catch (e: Exception) {
            e.printStackTrace()
            throw ServiceUnavailableException()
        }
    }
}
package com.example.routes

import com.example.database.VoteEntity
import com.example.database.voteToApi
import com.example.models.Vote
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import javax.naming.ServiceUnavailableException

fun Route.voteRouting() {
    route("/vote") {
        getVotes()
        createVote()
    }

    route("/vote/{id}") {
        getVoteById()
        deleteVote()
    }
}

private fun Route.getVotes() {
    get {
        try {
            val votes = transaction {
                VoteEntity.all().map {it ->
                    voteToApi(it)
                }
            }

            call.respond(HttpStatusCode.OK, votes)
        } catch (e: Exception) {
            e.printStackTrace()
            throw ServiceUnavailableException()
        }
    }
}

fun Route.getVoteById() {
    get {
        try {
            val id = call.parameters["id"]?.toInt() ?: 0
            val voteEntity = transaction {
                VoteEntity.findById(id) ?: throw NotFoundException()
            }

            val vote = voteToApi(voteEntity)
            call.respond(HttpStatusCode.OK, vote)
        } catch(e: NotFoundException) {
            call.respond(HttpStatusCode.NotFound)
        } catch (e: Exception) {
            e.printStackTrace()
            throw ServiceUnavailableException()
        }
    }
}

private fun Route.createVote() {
    post {
        try {
            val createVoteRequest = call.receive<CreateVoteRequest>()

            val voteEntity = transaction {
                VoteEntity.new {
                    billId = createVoteRequest.billId
                    vote = createVoteRequest.vote
                    pros = createVoteRequest.pros
                    cons = createVoteRequest.cons
                    reasoning = createVoteRequest.reasoning
                    created = Instant.now()
                    updated = Instant.now()
                }
            }

            call.respond(HttpStatusCode.Created, voteToApi(voteEntity))
        } catch (e: Exception) {
            e.printStackTrace()
            throw ServiceUnavailableException()
        }
    }
}

private fun Route.deleteVote() {
    delete {
        try {
            val id = call.parameters["id"]?.toInt() ?: 0
            transaction {
                val voteEntity = VoteEntity.findById(id) ?: throw NotFoundException()
                voteEntity.delete()
            }

            call.respond(HttpStatusCode.OK, "Deleted")
        } catch (e: NotFoundException) {
            call.respond(HttpStatusCode.NotFound)
        } catch (e: Exception) {
            e.printStackTrace()
            throw ServiceUnavailableException()
        }
    }
}

@Serializable
data class CreateVoteRequest(
    val billId: Int,
    val vote: String,
    val pros: String,
    val cons: String,
    val reasoning: String
)
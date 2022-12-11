package com.constituentconnect.routes

import com.constituentconnect.database.*
import com.constituentconnect.models.CreateVoteDetailRequest
import com.constituentconnect.models.VoteDetailResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import javax.naming.ServiceUnavailableException

fun Route.voteDetailRouting() {
    route("/vote-detail/{userId}/{id}") {
        getVoteDetail()
    }

    route("/vote-detail") {
        createVoteDetail()
    }
}

private fun Route.getVoteDetail() {
    get {
        try {
            val userId = call.parameters["userId"] ?: ""
            val id = call.parameters["id"]?.toInt() ?: 0

            val voteDetail = transaction {
                Votes.leftJoin(VoteDetails, { Votes.id }, { VoteDetails.voteId })
                    .innerJoin(Bills, { Votes.billId }, { Bills.id })
                    .slice(
                        Votes.id,
                        VoteDetails.id,
                        Bills.name,
                        Bills.description,
                        VoteDetails.vote,
                        VoteDetails.pros,
                        VoteDetails.cons,
                        VoteDetails.reasoning,
                        VoteDetails.created,
                        VoteDetails.updated
                    )
                    .select {
                        (Votes.userId eq userId) and (Votes.id eq id)
                    }
                    .limit(1)
                    .single()
                    .let {
                        VoteDetailResponse(
                            it[Votes.id].toString().toInt(),
                            it[VoteDetails.id]?.toString()?.toInt(),
                            it[VoteDetails.id] != null,
                            it[Bills.name],
                            it[Bills.description],
                            it[VoteDetails.vote],
                            it[VoteDetails.pros],
                            it[VoteDetails.cons],
                            it[VoteDetails.reasoning],
                            it[VoteDetails.created],
                            it[VoteDetails.updated]
                        )
                    }
            }

            call.respond(HttpStatusCode.OK, voteDetail)
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
                Votes.update({ Votes.id eq voteDetail.voteId }) {
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
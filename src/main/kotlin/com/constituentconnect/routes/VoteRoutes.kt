package com.constituentconnect.routes

import com.constituentconnect.database.Bills
import com.constituentconnect.database.Votes
import com.constituentconnect.database.Votes.billId
import com.constituentconnect.database.Votes.updated
import com.constituentconnect.database.Votes.userId
import com.constituentconnect.database.Votes.voteDetailId
import com.constituentconnect.models.VoteListResponse
import com.constituentconnect.models.VoteResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.transactions.transaction
import javax.naming.ServiceUnavailableException

fun Route.voteRouting() {
    route("/vote/{userId}") {
        getVotes()
    }

    route("/vote/{userId}/{id}") {
        getVote()
    }
}

private fun Route.getVote() {
    get {
        try {
            val userIdFilter = call.parameters["userId"] ?: ""
            val voteId = call.parameters["id"]?.toInt() ?: throw NotFoundException()

            val vote = transaction {
                Votes.innerJoin(Bills, { billId }, { Bills.id })
                    .slice(Votes.id, billId, userId, voteDetailId, voteDetailId.isNotNull(), Bills.name, Bills.description, updated)
                    .select {
                        (userId eq userIdFilter) and (Votes.id eq voteId)
                    }
                    .limit(1)
                    .single()
                    .let {
                        VoteResponse(
                            it[Votes.id].toString().toInt(),
                            it[billId],
                            it[userId],
                            //it[voteDetailId] ?: 0,
                            it[voteDetailId.isNotNull()],
                            it[Bills.name],
                            it[Bills.description],
                            it[updated]
                        )
                    }
            }

            call.respond(HttpStatusCode.OK, vote)
        } catch (e: NotFoundException) {
            throw NotFoundException()
        } catch (e: Exception) {
            e.printStackTrace()
            throw ServiceUnavailableException()
        }
    }
}

private fun Route.getVotes() {
    get {
        try {
            val userIdFilter = call.parameters["userId"] ?: ""
            val pageNumberFilter = call.request.queryParameters["page"]?.toInt() ?: 1
            val statusFilter = call.request.queryParameters["status"]
            val pageCount = 50
            val skip = ((pageNumberFilter - 1) * pageCount).toLong()

            // Get the count of all rows to determine pages
            val voteCount = transaction {
                val voteCountQuery = Votes.select {
                    userId eq userIdFilter
                }

                if (statusFilter != null) {
                    if (statusFilter == "closed") {
                        voteCountQuery.andWhere {
                            voteDetailId.isNotNull()
                        }
                    } else {
                        voteCountQuery.andWhere {
                            voteDetailId.isNull()
                        }
                    }
                }

                voteCountQuery.count()
            }

            var totalPages = (voteCount / pageCount).toInt()

            if ((voteCount % pageCount) > 0) {
                totalPages += 1
            }

            val votes = transaction {
                val votesQuery = Votes.innerJoin(Bills, { billId }, { Bills.id })
                    .slice(Votes.id, billId, userId, voteDetailId, voteDetailId.isNotNull(), Bills.name, Bills.description, updated)
                    .select {
                        userId eq userIdFilter
                    }
                    .orderBy(updated, SortOrder.DESC)
                    .limit(pageCount, skip)

                if (statusFilter != null) {
                    if (statusFilter == "closed") {
                        votesQuery.andWhere {
                            voteDetailId.isNotNull()
                        }
                    } else {
                        votesQuery.andWhere {
                            voteDetailId.isNull()
                        }
                    }
                }

                votesQuery.map {
                    VoteResponse(
                        it[Votes.id].toString().toInt(),
                        it[billId],
                        it[userId],
                        //it[voteDetailId] ?: 0,
                        it[voteDetailId.isNotNull()],
                        it[Bills.name],
                        it[Bills.description],
                        it[updated]
                    )
                }
            }

            val response = VoteListResponse(votes, pageNumberFilter, totalPages)

            call.respond(HttpStatusCode.OK, response)
        } catch (e: Exception) {
            e.printStackTrace()
            throw ServiceUnavailableException()
        }
    }
}
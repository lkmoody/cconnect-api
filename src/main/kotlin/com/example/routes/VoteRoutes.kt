package com.example.routes

import com.example.database.Bills
import com.example.database.Votes
import com.example.database.Votes.billId
import com.example.database.Votes.updated
import com.example.database.Votes.userId
import com.example.database.Votes.voteDetailId
import com.example.models.VoteListResponse
import com.example.models.VoteResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import javax.naming.ServiceUnavailableException

fun Route.voteRouting() {
    route("/vote/{userId}/{page}") {
        getVotes()
    }

    route("/vote/{userId}/{page}/{statusFilter}") {
        getVotes()
    }

    route("/vote/{id}") {

    }
}

private fun Route.getVotes() {
    get {
        try {
            val userIdFilter = call.parameters["userId"] ?: ""
            val pageNumberFilter = call.parameters["page"]?.toInt() ?: 1
            val statusFilter = call.parameters["statusFilter"]
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
                    .slice(Votes.id, billId, userId, voteDetailId.isNotNull(), Bills.name, Bills.description, updated)
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
            //throw ServiceUnavailableException()
            throw e
        }
    }
}
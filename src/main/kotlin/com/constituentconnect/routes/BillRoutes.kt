package com.constituentconnect.routes

import com.constituentconnect.database.*
import com.constituentconnect.models.BillListResponse
import com.constituentconnect.models.BillResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import javax.naming.ServiceUnavailableException

fun Route.billRouting() {
    route("/bill") {
        getBills()
        createBill()
        updateBill()
    }

    route("/bill/reset") {
        resetBill()
    }

    route("/bill/{id}") {
        getBillById()
        deleteBill()
    }

    route("/bill/close-bill-vote") {
        closeBillVote()
    }
}

fun Route.getBills() {
    get {
        try {
            val pageNumberFilter = call.request.queryParameters["page"]?.toInt() ?: 1
            val statusFilter = call.request.queryParameters["status"]
            val pageCount = 50
            val skip = ((pageNumberFilter - 1) * pageCount).toLong()

            val billCount = transaction {
                val billCountQuery = Bills.selectAll()

                if (statusFilter != null) {
                    if (statusFilter == "closed") {
                        billCountQuery.andWhere {
                            Bills.voteClosed eq true
                        }
                    } else if (statusFilter == "open") {
                        billCountQuery.andWhere {
                            Bills.voteClosed eq false
                        }
                    }
                }

                billCountQuery.count()
            }

            var totalPages = (billCount / pageCount).toInt()

            if ((billCount % pageCount) > 0) {
                totalPages += 1
            }

            val bills = transaction {
                val billsQuery = Bills.selectAll()
                    .orderBy(Bills.created, SortOrder.DESC)
                    .limit(pageCount, skip)

                if (statusFilter != null) {
                    if (statusFilter == "closed") {
                        billsQuery.andWhere {
                            Bills.voteClosed eq true
                        }
                    } else if (statusFilter == "open") {
                        billsQuery.andWhere {
                            Bills.voteClosed eq false
                        }
                    }
                }

                billsQuery.map {
                    BillResponse(
                        it[Bills.id].toString().toInt(),
                        it[Bills.name],
                        it[Bills.description],
                        it[Bills.voteClosed],
                        it[Bills.groupId],
                        it[Bills.created],
                        it[Bills.updated]
                    )
                }
            }

            val response = BillListResponse(bills, pageNumberFilter, totalPages)

            call.respond(HttpStatusCode.OK, response)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "There was a problem getting the list of bills.")
            println(e.message)
        }
    }
}

fun Route.getBillById() {
    get {
        val id = call.parameters["id"]?.toInt() ?: 0

        try {
            val billEntity = transaction {
                BillEntity.findById(id) ?: throw NotFoundException()
            }

            val bill = billToApi(billEntity)
            call.respond(HttpStatusCode.OK, bill)
        } catch (e: NotFoundException) {
            call.respond(HttpStatusCode.NotFound, "Unable to find the bill with id $id")
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "There was a problem getting the bill.")
            println(e.message)
        }
    }
}

fun Route.createBill() {
    post {
        try {
            val createBillRequest = call.receive<CreateBillRequest>()

            val billEntity = transaction {
                BillEntity.new {
                    name = createBillRequest.name
                    description = createBillRequest.description
                    voteClosed = createBillRequest.voteClosed
                    groupId = createBillRequest.groupId
                    created = Instant.now()
                    updated = Instant.now()
                }
            }

            call.respond(HttpStatusCode.Created, billToApi(billEntity))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "There was a problem creating the bill.")
            println(e.message)
        }
    }
}

fun Route.updateBill() {
    patch {
        try {
            val updateBillRequest = call.receive<UpdateBillRequest>()
            transaction {
                val billEntity = BillEntity.findById(updateBillRequest.id) ?: throw NotFoundException()
                billEntity.name = updateBillRequest.name ?: billEntity.name
                billEntity.description = updateBillRequest.description ?: billEntity.description
                billEntity.voteClosed = updateBillRequest.voteClosed ?: billEntity.voteClosed
                billEntity.groupId = updateBillRequest.groupId ?: billEntity.groupId
                billEntity.updated = Instant.now()
            }

            val bill = transaction {
                Bills.select {
                    Bills.id eq updateBillRequest.id
                }
                    .limit(1)
                    .single()
                    .let {
                        BillResponse(
                            it[Bills.id].toString().toInt(),
                            it[Bills.name],
                            it[Bills.description],
                            it[Bills.voteClosed],
                            it[Bills.groupId],
                            it[Bills.created],
                            it[Bills.updated]
                        )
                    }
            }

            call.respond(HttpStatusCode.OK, bill)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "There was a problem updating the bill.")
            println(e.message)
        }
    }
}

fun Route.closeBillVote() {
    patch {
        try {
            val closeBillVoteRequest = call.receive<CloseBillVoteRequest>()
            transaction {
                val billEntity = BillEntity.findById(closeBillVoteRequest.id) ?: throw NotFoundException()
                billEntity.voteClosed = true
            }

            val bill = transaction {
                Bills.select {
                    Bills.id eq closeBillVoteRequest.id
                }
                    .limit(1)
                    .single()
                    .let {
                        BillResponse(
                            it[Bills.id].toString().toInt(),
                            it[Bills.name],
                            it[Bills.description],
                            it[Bills.voteClosed],
                            it[Bills.groupId],
                            it[Bills.created],
                            it[Bills.updated]
                        )
                    }
            }

            // Replace with call to the user table
            //TODO: Need to figure out proper way to add votes
            val users = transaction {
                Users.join(UserGroups, JoinType.INNER, additionalConstraint = {Users.id eq UserGroups.userId})
                    .slice(Users.id)
                    .select {
                        UserGroups.groupId eq bill.groupId
                    }.map {
                        it[Users.id].value
                    }
            }

            users.forEach {
                transaction {
                    val vote = VoteEntity.find {
                        (Votes.userId eq it) and (Votes.billId eq bill.id)
                    }.firstOrNull()
                    if (vote == null) {
                        VoteEntity.new {
                            billId = bill.id
                            userId = it
                            voteDetailId = null
                            created = Instant.now()
                            updated = Instant.now()
                        }
                    }
                }
            }

            call.respond(HttpStatusCode.OK, bill)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "There was a problem closing the bill vote.")
            println(e.message)
        }
    }
}

fun Route.deleteBill() {
    delete {
        try {
            val id = call.parameters["id"]?.toInt() ?: throw NotFoundException()
            transaction {
                val billEntity = BillEntity.findById(id) ?: throw NotFoundException()
                if (billEntity.voteClosed) {
                    throw NotFoundException()
                }
                billEntity.delete()
            }

            call.respond(HttpStatusCode.OK, "Deleted")
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "There was a problem deleting the bill.")
            println(e.message)
        }
    }
}

fun Route.resetBill() {
    patch {
        try {
            val updateBillRequest = call.receive<UpdateBillRequest>()
            transaction {
                val billEntity = BillEntity.findById(updateBillRequest.id) ?: throw NotFoundException()
                billEntity.name = updateBillRequest.name ?: billEntity.name
                billEntity.description = updateBillRequest.description ?: billEntity.description
                billEntity.voteClosed = updateBillRequest.voteClosed ?: billEntity.voteClosed
                billEntity.updated = Instant.now()
            }

            val bill = transaction {
                Bills.select {
                    Bills.id eq updateBillRequest.id
                }
                    .limit(1)
                    .single()
                    .let {
                        BillResponse(
                            it[Bills.id].toString().toInt(),
                            it[Bills.name],
                            it[Bills.description],
                            it[Bills.voteClosed],
                            it[Bills.groupId],
                            it[Bills.created],
                            it[Bills.updated]
                        )
                    }
            }

            transaction {
                Votes.update({ Votes.billId eq bill.id }) {
                    it[voteDetailId] = null
                }
            }

            val voteDetailIdsToDelete = transaction {
                Votes.innerJoin(VoteDetails, { Votes.id }, { voteId })
                    .slice(VoteDetails.id)
                    .select {
                        Votes.voteDetailId eq null
                    }
                    .map {
                        it[VoteDetails.id].toString().toInt()
                    }
            }

            voteDetailIdsToDelete.forEach {
                transaction {
                    val voteDetailEntity = VoteDetailEntity.findById(it)
                    voteDetailEntity?.delete()
                }
            }

            val voteIdsToDelete = transaction {
                Votes.select {
                    Votes.billId eq bill.id
                }
                    .map {
                        it[Votes.id].toString().toInt()
                    }
            }

            voteIdsToDelete.forEach {
                transaction {
                    val voteEntity = VoteEntity.findById(it)
                    voteEntity?.delete()
                }
            }

            call.respond(HttpStatusCode.OK, bill)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Unable to reset the bill data.")
            println(e.message)
        }
    }
}

@Serializable
data class CreateBillRequest(
    val name: String,
    val description: String,
    val voteClosed: Boolean,
    val groupId: Int
)

@Serializable
data class UpdateBillRequest(
    val id: Int,
    val name: String? = null,
    val description: String? = null,
    val voteClosed: Boolean? = null,
    val groupId: Int? = null
)

@Serializable
data class CloseBillVoteRequest(
    val id: Int
)
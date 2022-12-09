package com.constituentconnect.routes

import com.constituentconnect.database.BillEntity
import com.constituentconnect.database.billToApi
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

fun Route.billRouting() {
    route("/bill") {
        getBills()
        createBill()
    }

    route("/bill/{id}") {
        getBillById()
        deleteBill()
    }
}

fun Route.getBills() {
    get {
        try {
            val bills = transaction {
                BillEntity.all().map { it ->
                    billToApi(it)
                }
            }
            call.respond(HttpStatusCode.OK, bills)
        } catch (e: Exception) {
            e.printStackTrace()
            //TODO: Need to implement logging
            throw ServiceUnavailableException()
        }
    }
}

fun Route.getBillById() {
    get {
        try {
            val id = call.parameters["id"]?.toInt() ?: 0
            val billEntity = transaction {
                BillEntity.findById(id) ?: throw NotFoundException()
            }

            val bill = billToApi(billEntity)
            call.respond(HttpStatusCode.OK, bill)
        } catch (e: NotFoundException) {
            call.respond(HttpStatusCode.NotFound)
        } catch (e: Exception) {
            e.printStackTrace()
            throw ServiceUnavailableException()
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
                    created = Instant.now()
                    updated = Instant.now()
                }
            }

            call.respond(HttpStatusCode.Created, billToApi(billEntity))
        } catch (e: Exception) {
            e.printStackTrace()
            throw ServiceUnavailableException()
        }
    }
}

fun Route.deleteBill() {
    delete {
        try {
            val id = call.parameters["id"]?.toInt() ?: 0
            transaction {
                val billEntity = BillEntity.findById(id) ?: throw NotFoundException()
                billEntity.delete()
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
data class CreateBillRequest(
    val name: String,
    val description: String,
    val voteClosed: Boolean
)
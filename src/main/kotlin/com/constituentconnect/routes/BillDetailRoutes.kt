package com.constituentconnect.routes

import com.constituentconnect.database.Bills
import com.constituentconnect.models.BillDetailResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import javax.naming.ServiceUnavailableException

fun Route.billDetailRouting() {
    route("/bill-detail/{id}") {
        getBillDetail()
    }
}

private fun Route.getBillDetail() {
    get {
        val id = call.parameters["id"]?.toInt() ?: throw NotFoundException()

        try {
            val billDetail = transaction {
                Bills.slice(
                    Bills.id,
                    Bills.name,
                    Bills.description,
                    Bills.voteClosed,
                    Bills.groupId,
                    Bills.created,
                    Bills.updated
                ).select {
                    Bills.id eq id
                }
                    .limit(1)
                    .single()
                    .let {
                        BillDetailResponse(
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

            call.respond(HttpStatusCode.OK, billDetail)
        } catch (e: NotFoundException) {
            call.respond(HttpStatusCode.NotFound, "Unable to find the bill detail")
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "There was a problem getting the bill detail for id $id")
            println(e.message)
        }
    }
}
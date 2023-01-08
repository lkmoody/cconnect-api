package com.constituentconnect.routes

import com.constituentconnect.database.Groups
import com.constituentconnect.models.Group
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.groupRoutes() {
    route("/groups") {
        getGroups()
    }
}

fun Route.getGroups() {
    get {
        try {
            val groups = transaction {
                Groups.selectAll().map {
                    Group(
                        it[Groups.id].value,
                        it[Groups.name],
                        it[Groups.displayName],
                        it[Groups.created],
                        it[Groups.updated]

                    )
                }
            }
            call.respond(HttpStatusCode.OK, groups)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "There was a problem getting groups.")
        }
    }
}
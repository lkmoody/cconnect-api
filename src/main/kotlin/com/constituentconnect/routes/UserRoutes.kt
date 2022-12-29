package com.constituentconnect.routes

import com.constituentconnect.database.createNewUser
import com.constituentconnect.database.getCurrentUserByAuthId
import com.constituentconnect.database.updateUserInfo
import com.constituentconnect.models.UpdateUserRequest
import com.constituentconnect.plugins.getCurrentUserEmail
import com.constituentconnect.plugins.getCurrentUsername
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRouting() {
    route("/user") {
        getUser()
        updateUser()
    }
}

fun Route.getUser() {
    get {
        try {
            val email = call.getCurrentUserEmail()
            val username = call.getCurrentUsername()
            var user = getCurrentUserByAuthId(username)

            if (user == null) {
                user = createNewUser(username, email)
            }

            call.respond(HttpStatusCode.OK, user)
        } catch (e: ParameterMissingError) {

        } catch (e: Error) {

        }
    }
}

fun Route.updateUser() {
    patch {
        try {
            val result = call.receive<UpdateUserRequest>()
            updateUserInfo(result)
            call.respond(HttpStatusCode.OK, result)
        } catch (e: Error) {

        }
    }
}

class ParameterMissingError : Error()
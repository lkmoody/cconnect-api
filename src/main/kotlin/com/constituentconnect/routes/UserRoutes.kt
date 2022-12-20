package com.constituentconnect.routes

import com.constituentconnect.database.createNewUser
import com.constituentconnect.database.getCurrentUser
import com.constituentconnect.database.updateUserInfo
import com.constituentconnect.models.UpdateUserRequest
import com.constituentconnect.plugins.AuthenticationError
import com.constituentconnect.plugins.getCurrentUsernameOrNull
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Route.userRouting() {
    route("/user") {
        getUser()
        updateUser()
    }
}

fun Route.getUser() {
    get {
        try {
            val email = call.request.queryParameters["email"] ?: throw ParameterMissingError()
            val username = call.getCurrentUsernameOrNull() ?: throw AuthenticationError()
            var user = getCurrentUser(username)

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
            val username = call.getCurrentUsernameOrNull() ?: throw AuthenticationError()
            val result = call.receive<UpdateUserRequest>()
            updateUserInfo(username, result)
            call.respond(HttpStatusCode.OK, result)
        } catch (e: Error) {

        }
    }
}

class ParameterMissingError: Error()
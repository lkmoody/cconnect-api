package com.constituentconnect.routes

import com.constituentconnect.database.*
import com.constituentconnect.plugins.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.apache.commons.codec.binary.Base64
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

fun Route.settingsRouting() {
    route("/settings") {
        route("/account/voting-group") {
            updateUserGroup()
        }
        route("/notifications") {

        }

        route("/external-accounts") {
            route("/twitter-auth-url") {
                twitterAccessRequest()
            }
        }

        route("/user-settings") {
            getUserSettings()
        }

        route("/tweet") {
            postTweet()
        }
    }
}

fun Route.updateUserGroup() {
    patch {
        try {
            val user = call.getCurrentUser() ?: throw AuthenticationException()
            val newGroupId = call.receiveText().toInt()

            transaction {
                val userGroup = UserGroupEntity.find {
                    UserGroups.userId eq user.id
                }.firstOrNull()

                if (userGroup != null) {
                    userGroup.groupId = newGroupId
                } else {
                    UserGroupEntity.new {
                        userId = user.id
                        groupId = newGroupId
                    }
                }
            }

            call.respond(HttpStatusCode.OK)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "There was a problem updating the users group.")
        }
    }
}

fun Route.postTweet() {
    post {
        call.postTweet(call.getNonce())
        call.respond(HttpStatusCode.OK)
    }
}

fun Route.twitterAccessRequest() {
    get {
        val clientErrorMessage = "There was an issue trying to authorize with Twitter"
        try {
            val user = call.getCurrentUser() ?: throw AuthenticationException()

            val oauthSignatureMethod = "HMAC-SHA1"
            val oauthConsumerKey = call.getConsumerKey()
            val oauthConsumerSecretKey = call.getConsumerSecretKey()
            val twitterApiUrl = call.getApiUrl()
            val oauthNonce = call.getNonce()
            var oauthTimestamp = (System.currentTimeMillis() / 1000).toString()
            val callbackUrl = call.getCallbackUrl()// + "?userId=${user.id}"
            println(callbackUrl)
            val oauthCallback = URLEncoder.encode(callbackUrl, StandardCharsets.UTF_8.toString())

            // Create the parameter string used to create the signature. Be careful modifying this. The string needs to be constructed in a very particular manner.
            var parameterString = URLEncoder.encode("oauth_callback=$oauthCallback", StandardCharsets.UTF_8.toString())
            parameterString += URLEncoder.encode(
                "&oauth_consumer_key=$oauthConsumerKey",
                StandardCharsets.UTF_8.toString()
            )
            parameterString += URLEncoder.encode("&oauth_nonce=$oauthNonce", StandardCharsets.UTF_8.toString())
            parameterString += URLEncoder.encode(
                "&oauth_signature_method=$oauthSignatureMethod",
                StandardCharsets.UTF_8.toString()
            )
            parameterString += URLEncoder.encode("&oauth_timestamp=$oauthTimestamp", StandardCharsets.UTF_8.toString())
            parameterString += URLEncoder.encode("&oauth_version=1.0", StandardCharsets.UTF_8.toString())

            val signatureBaseString = "POST&" + URLEncoder.encode(
                "$twitterApiUrl/oauth/request_token",
                StandardCharsets.UTF_8.toString()
            ) + "&$parameterString"

            var oauthSignature = ""
            oauthSignature = computeSignature(
                signatureBaseString,
                "$oauthConsumerSecretKey&"
            ) // The "&" symbol needs to be at the end. Something to do with a possible auth token you can add to the end. We are not doing that but leave the symbol

            val urlEncodedOauthSignature = URLEncoder.encode(oauthSignature, StandardCharsets.UTF_8.toString())

            val authorizationHeader =
                "OAuth oauth_consumer_key=\"$oauthConsumerKey\",oauth_signature_method=\"$oauthSignatureMethod\",oauth_timestamp=\"$oauthTimestamp\",oauth_nonce=\"$oauthNonce\",oauth_version=\"1.0\",oauth_callback=\"$oauthCallback\",oauth_signature=\"$urlEncodedOauthSignature\""

            val client = HttpClient(CIO)
            val response = client.request("$twitterApiUrl/oauth/request_token") {
                method = HttpMethod.Post
                headers {
                    append(HttpHeaders.Authorization, authorizationHeader)
                }
            }

            if (response.status == HttpStatusCode.OK) {
                var oauthResults = HashMap<String, String>()
                println(response.bodyAsText())
                val splitValues = response.bodyAsText().split("&")
                for (value in splitValues) {
                    val keyValue = value.split("=")
                    oauthResults[keyValue[0]] = keyValue[1]
                }
                val oauthToken = oauthResults["oauth_token"]
                val oauthTokenSecret = oauthResults["oauth_token_secret"]
                val oauthCallbackConfirmed = oauthResults["oauth_callback_confirmed"].toBoolean()

                if (oauthCallbackConfirmed) {
                    transaction {
                        val userTwitter = UserTwitterEntity.find { UserTwitters.userId eq user.id }.firstOrNull()
                        if (userTwitter == null) {
                            UserTwitterEntity.new {
                                userId = user.id
                                requestAccessToken = oauthToken ?: ""
                                requestAccessTokenSecret = oauthTokenSecret ?: ""
                            }
                        } else {
                            userTwitter.requestAccessToken = oauthToken ?: ""
                            userTwitter.requestAccessTokenSecret = oauthTokenSecret ?: ""
                        }
                    }

                    val authUrl = "$twitterApiUrl/oauth/authorize?oauth_token=$oauthToken"

                    call.respond(HttpStatusCode.OK, authUrl)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, clientErrorMessage)
                    println("Twitter callback confirm was false.")
                }
            } else {
                call.respond(HttpStatusCode.InternalServerError, clientErrorMessage)
                println(response.status)
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, clientErrorMessage)
            println(e.message)
        }
    }
}

fun Route.getUserSettings() {
    get {
        val clientErrorMessage = "There was a problem getting the user settings."
        try {
            val user = call.getCurrentUser() ?: throw AuthenticationException()

            val userSettings = transaction {
                UserSettingEntity.find { UserSettings.userID eq user.id }.first()
            }
            call.respond(HttpStatusCode.OK, userSettings)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, clientErrorMessage)
            println(e.message)
        }
    }
}

fun computeSignature(baseString: String, keyString: String): String {
    val mac = Mac.getInstance("HmacSHA1")
    val secret = SecretKeySpec(keyString.toByteArray(), mac.algorithm)
    mac.init(secret)
    val digest = mac.doFinal(baseString.toByteArray())
    return Base64.encodeBase64String(digest)
}

data class UpdateUserGroupRequest(
    val groupId: Int
)
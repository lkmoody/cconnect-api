package com.constituentconnect.routes

import com.constituentconnect.database.getCurrentUserByAuthId
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.apache.commons.codec.binary.Base64
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import com.constituentconnect.plugins.*

fun Route.settingsRouting() {
    route("/settings") {
        route("/twitter-auth-url") {
            twitterAccessRequest()
        }

        route("/tweet") {
            postTweet()
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
        try {
            val username = call.getCurrentUsername()
            var user = getCurrentUserByAuthId(username)

            val oauthSignatureMethod = "HMAC-SHA1"
            val oauthConsumerKey = call.getConsumerKey()
            val oauthConsumerSecretKey = call.getConsumerSecretKey()
            val twitterApiUrl = call.getApiUrl()
            val oauthNonce = call.getNonce()
            var oauthTimestamp = (System.currentTimeMillis() / 1000).toString()
            val callbackUrl = call.getCallbackUrl() + "?userId=${user.id}"
            println(callbackUrl)
            val oauthCallback = URLEncoder.encode(callbackUrl, StandardCharsets.UTF_8.toString())

            // Create the parameter string used to create the signature. Be careful modifying this. The string needs to be constructed in a very particular manner.
            var parameterString = URLEncoder.encode("oauth_callback=$oauthCallback", StandardCharsets.UTF_8.toString())
            parameterString += URLEncoder.encode("&oauth_consumer_key=$oauthConsumerKey", StandardCharsets.UTF_8.toString())
            parameterString += URLEncoder.encode("&oauth_nonce=$oauthNonce", StandardCharsets.UTF_8.toString())
            parameterString += URLEncoder.encode("&oauth_signature_method=$oauthSignatureMethod", StandardCharsets.UTF_8.toString())
            parameterString += URLEncoder.encode("&oauth_timestamp=$oauthTimestamp", StandardCharsets.UTF_8.toString())
            parameterString += URLEncoder.encode("&oauth_version=1.0", StandardCharsets.UTF_8.toString())
            println(parameterString)

            val signatureBaseString = "POST&" + URLEncoder.encode("$twitterApiUrl/oauth/request_token", StandardCharsets.UTF_8.toString()) + "&$parameterString"

            println(signatureBaseString)
            var oauthSignature = ""
            try {
                oauthSignature =
                    computeSignature(signatureBaseString, "$oauthConsumerSecretKey&") // The "&" symbol needs to be at the end. Something to do with a possible auth token you can add to the end. We are not doing that but leave the symbol
                println(oauthSignature)
            } catch (e: GeneralSecurityException) {

            }

            val urlEncodedOauthSignature = URLEncoder.encode(oauthSignature, StandardCharsets.UTF_8.toString())
            println(urlEncodedOauthSignature)

            val authorizationHeader = "OAuth oauth_consumer_key=\"$oauthConsumerKey\",oauth_signature_method=\"$oauthSignatureMethod\",oauth_timestamp=\"$oauthTimestamp\",oauth_nonce=\"$oauthNonce\",oauth_version=\"1.0\",oauth_callback=\"$oauthCallback\",oauth_signature=\"$urlEncodedOauthSignature\""
            println(authorizationHeader)

            val client = HttpClient(CIO)
            val response = client.request("$twitterApiUrl/oauth/request_token") {
                method = HttpMethod.Post
                headers {
                    append(HttpHeaders.Authorization, authorizationHeader)
                }
            }

            var oauthResults = HashMap<String, String>()
            println(response.bodyAsText())
            val splitValues = response.bodyAsText().split("&")
            for(value in splitValues) {
                val keyValue = value.split("=")
                oauthResults.put(keyValue[0], keyValue[1])
            }
            val oauthToken = oauthResults["oauth_token"]

            val authUrl = "$twitterApiUrl/oauth/authorize?oauth_token=$oauthToken"

            call.respond(HttpStatusCode.OK, authUrl)
        } catch (e: Error) {

        } finally {
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
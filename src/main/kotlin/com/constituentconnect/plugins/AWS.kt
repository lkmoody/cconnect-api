package com.constituentconnect.plugins

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.model.MessageAttributeValue
import com.amazonaws.services.sns.model.PublishRequest
import io.ktor.server.application.*

fun Application.sendSms(phoneNumber: String, message: String) {
    try {
        val credentials: AWSCredentials = BasicAWSCredentials(
            environment.config.propertyOrNull("aws.access_key")?.getString() ?: "",
            environment.config.propertyOrNull("aws.secret_key")?.getString() ?: ""
        )

        val snsClient = AmazonSNSClient.builder()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.US_WEST_2)
            .build()

        // The time for request/response round trip to aws in milliseconds
        // The time for request/response round trip to aws in milliseconds
        val requestTimeout = 3000
        val smsAttributes: MutableMap<String, MessageAttributeValue> = HashMap()
        smsAttributes["AWS.SNS.SMS.SMSType"] = MessageAttributeValue()
            .withStringValue("Transactional")
            .withDataType("String")
        smsAttributes["AWS.SNS.SMS.SenderID"] = MessageAttributeValue()
            .withStringValue("Constituent Connect")
            .withDataType("String")

        val request = snsClient.publish(
            PublishRequest()
                .withMessage(message)
                .withPhoneNumber(phoneNumber)
                .withMessageAttributes(smsAttributes)
                .withSdkRequestTimeout(requestTimeout)
        )

        println(request)
    } catch (e: Error) {

    }
}
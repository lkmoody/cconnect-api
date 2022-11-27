package com.example.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime

@Serializable
data class Bill(
    val id: Int,
    val name: String)

//object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
//    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)
//
//    override fun deserialize(decoder: Decoder): LocalDateTime {
//        return LocalDateTime.parse(decoder.decodeString())
//    }
//
//    override fun serialize(encoder: Encoder, value: LocalDateTime) {
//        encoder.encodeString(value.toString())
//    }
//}
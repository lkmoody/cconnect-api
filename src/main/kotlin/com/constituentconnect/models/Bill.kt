package com.constituentconnect.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.util.UUID

@Serializable
data class Bill(
    @Serializable(UUIDSerializer::class)
    val id: UUID,
    val name: String,
    val description: String,
    val voteClosed: Boolean,
    @Serializable(UUIDSerializer::class)
    val groupId: UUID,
    @Serializable(InstantSerializer::class)
    val created: Instant,
    @Serializable(InstantSerializer::class)
    val updated: Instant
)

@Serializable
data class BillResponse(
    @Serializable(UUIDSerializer::class)
    val id: UUID,
    val name: String,
    val description: String,
    val voteClosed: Boolean,
    @Serializable(UUIDSerializer::class)
    val groupId: UUID,
    @Serializable(InstantSerializer::class)
    val created: Instant,
    @Serializable(InstantSerializer::class)
    val updated: Instant
)

@Serializable
data class BillListResponse(
    val items: List<BillResponse>,
    val page: Int,
    val totalPages: Int
)

@Serializable
data class BillDetailResponse(
    @Serializable(UUIDSerializer::class)
    val id: UUID,
    val name: String,
    val description: String,
    val voteClosed: Boolean,
    @Serializable(UUIDSerializer::class)
    val groupId: UUID,
    @Serializable(InstantSerializer::class)
    val created: Instant,
    @Serializable(InstantSerializer::class)
    val updated: Instant
)

@Serializable
data class BillDetailListResponse(
    val items: List<BillDetailResponse>,
    val page: Int,
    val totalPages: Int
)

object InstantSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }
}

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}
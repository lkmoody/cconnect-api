package com.constituentconnect.models

import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.*

@Serializable
data class Vote(
    @Serializable(UUIDSerializer::class)
    val id: UUID,
    @Serializable(UUIDSerializer::class)
    val billId: UUID,
    @Serializable(UUIDSerializer::class)
    val userId: UUID,
    @Serializable(UUIDSerializer::class)
    val voteDetailId: UUID?,
    @Serializable(InstantSerializer::class)
    val created: Instant,
    @Serializable(InstantSerializer::class)
    val updated: Instant
)

@Serializable
data class VoteResponse(
    @Serializable(UUIDSerializer::class)
    val id: UUID,
    @Serializable(UUIDSerializer::class)
    val billId: UUID,
    @Serializable(UUIDSerializer::class)
    val userId: UUID,
    val voteSubmitted: Boolean,
    val billName: String,
    val billDescription: String,
    @Serializable(InstantSerializer::class)
    val updated: Instant
)

@Serializable
data class VoteListResponse(
    val items: List<VoteResponse>,
    val page: Int,
    val totalPages: Int
)
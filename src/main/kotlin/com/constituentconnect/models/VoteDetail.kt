package com.constituentconnect.models

import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.*

@Serializable
data class VoteDetail(
    @Serializable(UUIDSerializer::class)
    val id: UUID,
    @Serializable(UUIDSerializer::class)
    val voteId: UUID,
    val vote: String,
    val pros: String,
    val cons: String,
    val reasoning: String,
    @Serializable(InstantSerializer::class)
    val created: Instant,
    @Serializable(InstantSerializer::class)
    val updated: Instant
)

@Serializable
data class CreateVoteDetailRequest(
    @Serializable(UUIDSerializer::class)
    val voteId: UUID,
    val vote: String,
    val pros: String?,
    val cons: String?,
    val reasoning: String
)

@Serializable
data class VoteDetailResponse(
    @Serializable(UUIDSerializer::class)
    val voteId: UUID,
    @Serializable(UUIDSerializer::class)
    val voteDetailId: UUID?,
    val voteSubmitted: Boolean,
    val billName: String,
    val billDescription: String,
    val vote: String?,
    val pros: String?,
    val cons: String?,
    val reasoning: String?,
    @Serializable(InstantSerializer::class)
    val created: Instant?,
    @Serializable(InstantSerializer::class)
    val updated: Instant?
)
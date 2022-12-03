package com.example.models

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class VoteDetail(
    val id: Int,
    val voteId: Int,
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
    val voteId: Int,
    val vote: String,
    val pros: String?,
    val cons: String?,
    val reasoning: String
)
package com.constituentconnect.models

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Vote(
    val id: Int,
    val billId: Int,
    val userId: String,
    val voteDetailId: Int,
    @Serializable(InstantSerializer::class)
    val created: Instant,
    @Serializable(InstantSerializer::class)
    val updated: Instant
)

@Serializable
data class VoteResponse(
    val id: Int,
    val billId: Int,
    val userId: String,
    val voteDetailId: Int,
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
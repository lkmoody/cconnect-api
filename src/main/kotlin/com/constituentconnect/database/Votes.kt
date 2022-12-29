package com.constituentconnect.database

import com.constituentconnect.models.Vote
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

class VoteEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<VoteEntity>(Votes)

    var billId by Votes.billId
    var userId by Votes.userId
    var voteDetailId by Votes.voteDetailId
    var created by Votes.created
    var updated by Votes.updated
}

object Votes : IntIdTable("core.votes") {
    val billId = integer("billId")
    val userId = integer("userId")
    val voteDetailId = integer("voteDetailId").nullable()
    val created = timestamp("created")
    val updated = timestamp("updated")
}

fun voteToApi(voteEntity: VoteEntity): Vote = Vote(
    voteEntity.id.value,
    voteEntity.billId,
    voteEntity.userId,
    voteEntity.voteDetailId,
    voteEntity.created,
    voteEntity.updated
)
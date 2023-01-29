package com.constituentconnect.database

import com.constituentconnect.models.VoteDetail
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

class VoteDetailEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<VoteDetailEntity>(VoteDetails)

    var voteId by VoteDetails.voteId
    var vote by VoteDetails.vote
    var pros by VoteDetails.pros
    var cons by VoteDetails.cons
    var reasoning by VoteDetails.reasoning
    var created by VoteDetails.created
    var updated by VoteDetails.updated
}

object VoteDetails : UUIDTable("core.vote-details") {
    val voteId = uuid("voteId")
    val vote = text("vote")
    val pros = text("pros")
    val cons = text("cons")
    val reasoning = text("reasoning")
    val created = timestamp("created")
    val updated = timestamp("updated")
}

fun voteDetailToApi(voteDetailEntity: VoteDetailEntity): VoteDetail = VoteDetail(
    voteDetailEntity.id.value,
    voteDetailEntity.voteId,
    voteDetailEntity.vote,
    voteDetailEntity.pros,
    voteDetailEntity.cons,
    voteDetailEntity.reasoning,
    voteDetailEntity.created,
    voteDetailEntity.updated
)
package com.chatsapp.www.storage

import androidx.room.Embedded
import androidx.room.Relation
import com.chatsapp.www.Models.ModMessage
import com.chatsapp.www.Models.User

data class UserAndModMessage(
    @Embedded
    val user: User,
    @Relation(
        parentColumn = "uid",
        entityColumn = "chatRoomId"
    )
    val modMessage: ModMessage
)
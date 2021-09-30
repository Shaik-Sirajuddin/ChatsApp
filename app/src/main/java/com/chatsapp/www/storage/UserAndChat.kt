package com.chatsapp.www.storage

import androidx.room.Embedded
import androidx.room.Relation
import com.chatsapp.www.Models.Chat
import com.chatsapp.www.Models.User

data class UserAndChat(
    @Embedded
    val user:User,
    @Relation(
        parentColumn = "uid",
        entityColumn = "userId"
    )
    val chat: Chat
)
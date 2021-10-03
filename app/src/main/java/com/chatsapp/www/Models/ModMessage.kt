package com.chatsapp.www.Models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mod_message_table")
data class ModMessage(
    @PrimaryKey(autoGenerate = false)
    val chatRoomId:String,
    var isCurrentUser: Boolean,
    var message: Message
)
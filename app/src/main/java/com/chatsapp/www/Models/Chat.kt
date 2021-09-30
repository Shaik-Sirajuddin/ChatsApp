package com.chatsapp.www.Models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Chat(
    @PrimaryKey(autoGenerate = true)
    var key:Int = 0,
    var messages:ArrayList<Message>?=null,
    var userId:String? = null
)

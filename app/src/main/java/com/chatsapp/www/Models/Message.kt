package com.chatsapp.www.Models

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    var message:String = "",
    var timeStamp:Long = 0,
    var senderId:String = "",
    var messageId:String = "",
    var imageUri:String?=null
)
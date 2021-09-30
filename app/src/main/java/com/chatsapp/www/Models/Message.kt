package com.chatsapp.www.Models

data class Message(
    var msgPos:Int = -1,
    var message:String = "",
    var timeStamp:Long = 0,
    var senderId:String = "",
    var messageId:String = "",
    var feeling:Int = -1,
    var imageUri:String?=null
)
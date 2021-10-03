package com.chatsapp.www.Models

import com.google.firebase.database.DatabaseReference

class Constants {
    companion object{
        var currentRoom:DatabaseReference? = null
        var friendsRoom:DatabaseReference? = null
        var allMessagesRoom:DatabaseReference? = null
        var type = 0
    }
}
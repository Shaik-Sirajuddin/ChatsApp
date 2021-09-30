package com.chatsapp.www.Models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = false)
    var uid:String? = null,
    var phoneNumber:String? = null,
    var profileImage:String? = null,
    var userName:String? = null,
    var token:String? = null,
)
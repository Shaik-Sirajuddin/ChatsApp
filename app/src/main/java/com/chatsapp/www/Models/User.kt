package com.chatsapp.www.Models

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = false)
    @NonNull
    var uid:String = "",
    var phoneNumber:String? = null,
    var profileImage:String? = null,
    var userName:String? = null,
    var token:String? = null,
){

    override fun hashCode(): Int {
        return Objects.hash(uid)
    }
    override fun equals(other: Any?): Boolean {
        return when {
            this===other -> true
            other !is User -> false
            other.uid == this.uid -> true
            else -> false
        }
    }
}
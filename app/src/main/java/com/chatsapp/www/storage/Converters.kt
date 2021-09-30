package com.chatsapp.www.storage

import androidx.room.TypeConverter
import com.chatsapp.www.Models.Message
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {

    @TypeConverter
    fun fromList(value : ArrayList<Message>) = Json.encodeToString(value)

    @TypeConverter
    fun toList(value: String) = Json.decodeFromString<ArrayList<Message>>(value)
}
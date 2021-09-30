package com.chatsapp.www.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.chatsapp.www.Models.Chat
import com.chatsapp.www.Models.User

@Database(entities = [User::class,Chat::class],version = 1,exportSchema = false)
@TypeConverters(Converters::class)
abstract class UserDatabase:RoomDatabase(){

    abstract fun getDao():UserDao

    companion object {
        @Volatile
        private var INSTANCE: UserDatabase? = null

        fun getDatabase(context: Context): UserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "user_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
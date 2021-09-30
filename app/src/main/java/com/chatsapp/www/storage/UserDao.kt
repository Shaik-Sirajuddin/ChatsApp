package com.chatsapp.www.storage

import androidx.room.*
import com.chatsapp.www.Models.Chat
import com.chatsapp.www.Models.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT )
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertChat(chat: Chat)

    @Update
    suspend fun updateUser(user:User)

    @Update
    suspend fun updateChat(chat: Chat)

    @Query("SELECT EXISTS(SELECT * FROM user_table WHERE uid = :id)")
    suspend fun checkUser(id:String):Boolean

    @Query("SELECT * FROM user_table")
    suspend fun getAllUsers():List<User>

    @Query("SELECT 1 FROM user_table WHERE uid = :id LIMIT 1")
    suspend fun getChatWithUserId(id:String):UserAndChat

}
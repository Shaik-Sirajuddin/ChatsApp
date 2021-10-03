package com.chatsapp.www.storage

import androidx.room.*
import com.chatsapp.www.Models.Chat
import com.chatsapp.www.Models.ModMessage
import com.chatsapp.www.Models.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE )
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: Chat)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModMessage(msg:ModMessage)

    @Update
    suspend fun updateModMessage(msg: ModMessage)

    @Update
    suspend fun updateUser(user:User)

    @Update
    suspend fun updateChat(chat: Chat)

    @Query("SELECT EXISTS(SELECT * FROM user_table WHERE uid = :id)")
    suspend fun checkUser(id:String):Boolean

    @Query("SELECT * FROM user_table")
    suspend fun getAllUsers():List<User>

    @Transaction
    @Query("SELECT * FROM user_table WHERE uid = :id LIMIT 1")
    suspend fun getChatWithUserId(id:String):UserAndChat

    @Transaction
    @Query("SELECT * FROM user_table WHERE uid = :id LIMIT 1")
    suspend fun  getModMessageByUser(id: String):UserAndModMessage

}
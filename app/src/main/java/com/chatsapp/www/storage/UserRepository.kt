package com.chatsapp.www.storage

import com.chatsapp.www.Models.Chat
import com.chatsapp.www.Models.User

class UserRepository(private val userDao: UserDao){

    suspend fun insertUser(user: User){
        userDao.insertUser(user)
    }
    suspend fun insertChat(chat: Chat){
        userDao.insertChat(chat)
    }
    suspend fun updateUser(user: User){
        userDao.updateUser(user)
    }
    suspend fun updateChat(chat: Chat){
        userDao.updateChat(chat)
    }
    suspend fun getAllUsers():List<User> = userDao.getAllUsers()

    suspend fun getChatOfUser(userId:String):UserAndChat = userDao.getChatWithUserId(userId)

    suspend fun checkUserExistence(userId:String):Boolean = userDao.checkUser(userId)
}
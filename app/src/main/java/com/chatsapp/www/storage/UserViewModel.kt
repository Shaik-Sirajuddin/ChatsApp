package com.chatsapp.www.storage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chatsapp.www.Models.Chat
import com.chatsapp.www.Models.Constants
import com.chatsapp.www.Models.ModMessage
import com.chatsapp.www.Models.User
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.launch

class UserViewModel(application: Application): AndroidViewModel(application) {
    private val database:UserDatabase = UserDatabase.getDatabase(application)
    private val dao:UserDao = database.getDao()
    private val repository = UserRepository(dao)
    private var liveData:FirebaseQueryLiveData? = null
    private var messagesData:FirebaseQueryLiveData? = null
    fun getUsersLiveDataFromOnline():FirebaseQueryLiveData?{
        if(liveData==null) {
            liveData = FirebaseQueryLiveData(Constants.friendsRoom,0)
        }
        return liveData
    }
    fun getRepository(): UserRepository {
        return repository
    }

    fun getLastMsgdata(ref:DatabaseReference?):FirebaseQueryLiveData{
          return FirebaseQueryLiveData(ref, 1)
    }
    fun getMessagesLiveData(ref: DatabaseReference?):FirebaseQueryLiveData?{
        if(messagesData==null){
            messagesData = FirebaseQueryLiveData(ref,0)
        }
        return messagesData
    }
    fun insertUser(user: User){
        viewModelScope.launch {
            repository.insertUser(user)
        }
    }
    fun insertChat(chat: Chat){
         viewModelScope.launch {
             repository.insertChat(chat)
         }
    }
    fun insertModMessage(msg: ModMessage){
        viewModelScope.launch {
            repository.insertModMessage(msg)
        }
    }
    fun updateUser(user: User){
        viewModelScope.launch {
            repository.updateUser(user)
        }
    }
    fun updateChat(chat: Chat){
        viewModelScope.launch {
            repository.updateChat(chat)
        }
    }
    fun updateModMessage(msg: ModMessage){
        viewModelScope.launch {
            repository.updateModMessage(msg)
        }
    }
}

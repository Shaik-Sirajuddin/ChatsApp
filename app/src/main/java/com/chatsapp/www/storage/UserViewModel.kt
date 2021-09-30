package com.chatsapp.www.storage

import android.content.Context
import androidx.lifecycle.ViewModel

class UserViewModel(context: Context):ViewModel() {
    init {
        val database = UserDatabase.getDatabase(context)
        val dao = database.getDao()
        val repository = UserRepository(dao)
    }
}
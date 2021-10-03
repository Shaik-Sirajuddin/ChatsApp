package com.chatsapp.www

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chatsapp.www.Adapters.TopStatusAdapter
import com.chatsapp.www.Models.*
import com.chatsapp.www.databinding.ActivityMainBinding
import com.chatsapp.www.storage.UserAndModMessage
import com.chatsapp.www.storage.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import androidx.lifecycle.ViewModelProvider
import com.chatsapp.www.storage.FirebaseQueryLiveData

class MainActivity : AppCompatActivity(){
    private lateinit var database: FirebaseDatabase
    private lateinit var binding:ActivityMainBinding
    private lateinit var statusAdapter: TopStatusAdapter
    private lateinit var storage: FirebaseStorage
    private lateinit var mAuth:FirebaseAuth
    private lateinit var user:User
    private lateinit var adapter:UsersAdapter
    private lateinit var viewModel: UserViewModel
    private var usersList = ArrayList<User>()
    private var userWithMsgList= ArrayList<UserAndModMessage>()
    private val listenerList = ArrayList<FirebaseQueryLiveData>()
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setTheme(R.style.Theme_ChatsApp)
            setContentView(binding.root)
            database = FirebaseDatabase.getInstance()
            adapter = UsersAdapter(this, userWithMsgList)
            storage = FirebaseStorage.getInstance()
            mAuth = FirebaseAuth.getInstance()
            binding.shimmerViewContainer.startShimmer()
            binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
            GlobalScope.launch {
                getToken()
            }
            isStopped = false
            database.reference.child("Users").child(mAuth.uid!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            if (snapshot.exists()) {
                                user = snapshot.getValue<User>()!!
                                CurrentUser.user = user
                            }
                        } catch (e: Exception) {
                            Log.e("currentuser", e.message.toString())
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            statusAdapter = TopStatusAdapter(this)
            binding.topStatusList.adapter = statusAdapter
            val layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = RecyclerView.HORIZONTAL
            binding.topStatusList.layoutManager = layoutManager
            binding.bottomNav.setOnItemSelectedListener {
                return@setOnItemSelectedListener itemSelected(it)
            }
            //Loading Users From Offline Database
            viewModel = ViewModelProvider(this).get(UserViewModel::class.java)
            GlobalScope.launch {
                usersList = viewModel.getRepository().getAllUsers() as ArrayList<User>
                usersList.forEach {
                    if(it.uid !=mAuth.uid!!){
                        var modMsg = viewModel.getRepository().getModMessageWithUser(it.uid)
                        userWithMsgList.add(modMsg)
                    }
                }
                withContext(Dispatchers.Main) {
                    binding.shimmerViewContainer.stopShimmer()
                    binding.shimmerViewContainer.visibility = View.GONE
                    adapter.notifyDataSetChanged()
                }
            }
            //Loading Users From Online
            // Constants.friendsRoom = database.reference.child("Friends").child(mAuth.uid!!)
            Constants.friendsRoom = database.reference.child("Users")
            val liveData = viewModel.getUsersLiveDataFromOnline()
            if (liveData != null) {
                listenerList.add(liveData)
            }
            liveData?.observe(this, { snapshot ->
                if (snapshot == null) return@observe
                val userId = snapshot.getValue<User>() ?: return@observe
                Log.e("main","onlineUsers")
                addUserToRecyclerView(userId)
                val liveDt = viewModel.getLastMsgdata(
                    database.reference.child("Chats").child(mAuth.uid!!).child(userId.uid)
                        .child("lastMsg")
                )
                liveDt.observe(this, {
                    if (it == null) return@observe
                    val lm = it.getValue<Message>() ?: return@observe
                    updateLastMsg(lm, userId)
                })
                listenerList.add(liveDt)
            })

            database.reference.child("Stories").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshots: DataSnapshot) {
                    if (snapshots.exists()) {
                        val list = ArrayList<UserStatus>()
                        for (snapshot in snapshots.children) {
                            if (snapshot.exists()) {
                                val userStatus = UserStatus()
                                userStatus.userName = snapshot.child("name").getValue<String>()
                                userStatus.userProfileImage =
                                    snapshot.child("profileImage").getValue<String>()
                                userStatus.lastUpdate =
                                    snapshot.child("lastUpdate").getValue<Long>()

                                val statuses = ArrayList<Status>()
                                for (snap in snapshot.child("Statuses").children) {
                                    snap.getValue<Status>()?.let { statuses.add(it) }
                                }
                                if (statuses.isEmpty()) return
                                userStatus.statuses = statuses
                                list.add(userStatus)
                            }
                        }
                        statusAdapter.addStatus(list)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        }catch(e:Exception){
            e.printStackTrace()
            Log.e("re",e.message.toString())
        }
    }

    private fun updateLastMsg(lastM: Message,user1: User?) {
        if(user1==null)return
        val flag = lastM.senderId==mAuth.uid
        val modMessage = ModMessage(user1.uid,flag,lastM)
        val ind = usersList.indexOf(user1)
        userWithMsgList[ind] = UserAndModMessage(user1,modMessage)
        adapter.notifyItemChanged(ind)
        viewModel.updateModMessage(modMessage)
    }

    @DelicateCoroutinesApi
    private fun addUserToRecyclerView(userId:User?){
        if (userId != null) {
            if(usersList.contains(userId)){
                val ind =  usersList.indexOf(userId)
                usersList[ind] = userId
                GlobalScope.launch(Dispatchers.IO){
                    viewModel.getRepository().updateUser(userId)
                    userWithMsgList[ind] =
                        viewModel.getRepository().getModMessageWithUser(userId.uid)
                    withContext(Dispatchers.Main) {
                        adapter.notifyItemChanged(ind)
                    }
                }
            }
            else{
                usersList.add(0,userId)
                GlobalScope.launch(Dispatchers.IO) {
                    viewModel.getRepository().insertUser(userId)
                    viewModel.getRepository().insertModMessage(ModMessage(userId.uid,false,Message(message = "Tap to Chat",Date().time)))
                    userWithMsgList.add(0,
                        viewModel.getRepository().getModMessageWithUser(userId.uid)
                    )
                    withContext(Dispatchers.Main) {
                        adapter.notifyItemInserted(0)
                    }
                }
            }
        }
    }
    private suspend fun getToken(){
        val token = FirebaseMessaging.getInstance().token.await()
        val map = HashMap<String,Any>()
        map["token"] = token
        database.reference.child("Users").child(mAuth.uid!!).updateChildren(map)
    }
    override fun onResume() {
        super.onResume()
        val map = HashMap<String,Any>()
        map["status"] = "Online"
        database.reference.child("Presence").child(mAuth.uid!!).updateChildren(map)
    }

    override fun onPause() {
        super.onPause()
        val map = HashMap<String,Any>()
        map["status"] = ""
        database.reference.child("Presence").child(mAuth.uid!!).updateChildren(map)
    }

    override fun onStop() {
        super.onStop()
        isStopped = true
    }
    override fun onDestroy() {
        super.onDestroy()
        listenerList.forEach {
            it.destroy()
        }
        val map = HashMap<String,Any>()
        map["status"] = ""
        database.reference.child("Presence").child(mAuth.uid!!).updateChildren(map)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.topmenu,menu)
        return super.onCreateOptionsMenu(menu)
    }
    private fun itemSelected(item: MenuItem):Boolean{
        when(item.itemId){
            R.id.status->{
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                resultLauncher.launch(intent)
            }
            R.id.chats->{

            }
            else->{

            }
        }

        return true
    }
    private suspend fun putStatus(uri: Uri?,date:String):Boolean{
        if(uri==null){
            return false
        }
        val userStatus = UserStatus()
        userStatus.userName = user.userName
        userStatus.userProfileImage = user.profileImage
        userStatus.lastUpdate = date.toLong()
        val map = HashMap<String,Any>()
        map["name"] = userStatus.userName.toString()
        map["profileImage"] = userStatus.userProfileImage.toString()
        map["lastUpdate"] = userStatus.lastUpdate!!
        val status = Status(uri.toString(),userStatus.lastUpdate)
        database.reference
            .child("Stories")
            .child(mAuth.uid!!)
            .child("Statuses")
            .push()
            .setValue(status).await()
        database.reference
            .child("Stories")
            .child(mAuth.uid!!)
            .updateChildren(map)

        return true
    }
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        result.data?.let{intent->
            intent.data?.let{uri->
                Toast.makeText(this,"Uploading Image",Toast.LENGTH_SHORT).show()
                val date = Date().time.toString()
                val reference = storage.reference.child("Status").child(mAuth.uid + date)

                GlobalScope.launch {
                    reference.putFile(uri).await()
                    val downloadUri =  reference.downloadUrl.await()
                    val  flag =  putStatus(downloadUri,date)
                    withContext(Dispatchers.Main){
                        if(flag){
                            Toast.makeText(applicationContext,"Upload Successful",Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Toast.makeText(applicationContext,"Upload Failed",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        }
    }
    companion object{
       var isStopped = false
    }
}
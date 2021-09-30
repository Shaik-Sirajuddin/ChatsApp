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
import com.chatsapp.www.Models.CurrentUser
import com.chatsapp.www.Models.Status
import com.chatsapp.www.Models.User
import com.chatsapp.www.Models.UserStatus
import com.chatsapp.www.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var binding:ActivityMainBinding
    private lateinit var statusAdapter: TopStatusAdapter
    private lateinit var storage: FirebaseStorage
    private lateinit var mAuth:FirebaseAuth
    private lateinit var user:User
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setTheme(R.style.Theme_ChatsApp)
        setContentView(binding.root)
        database = FirebaseDatabase.getInstance()
        val adapter = UsersAdapter(this)
        storage = FirebaseStorage.getInstance()
        mAuth = FirebaseAuth.getInstance()
        binding.shimmerViewContainer.startShimmer()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        GlobalScope.launch {
               getToken()
        }
        database.reference.child("Users").child(mAuth.uid!!).addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
               try {
                   if (snapshot.exists()) {
                       user = snapshot.getValue<User>()!!
                       CurrentUser.user = user
                   }
               }catch(e:Exception){
                   Log.e("here",e.message.toString())
               }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        database.reference.child("Users").addChildEventListener(object:ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val user = snapshot.getValue<User>()
                if (user != null && user.uid!= mAuth.uid) {
                    adapter.addData(user)
                }
                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = View.GONE
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

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
        database.reference.child("Stories").addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshots: DataSnapshot) {
               if(snapshots.exists()) {
                   Log.e("dataChange", "Yes")
                   val list = ArrayList<UserStatus>()
                   for (snapshot in snapshots.children) {
                       if (snapshot.exists()) {
                           val userStatus = UserStatus()
                           userStatus.userName = snapshot.child("name").getValue<String>()
                           userStatus.userProfileImage =
                               snapshot.child("profileImage").getValue<String>()
                           userStatus.lastUpdate = snapshot.child("lastUpdate").getValue<Long>()

                           val statuses = ArrayList<Status>()
                           for (snap in snapshot.child("Statuses").children) {
                               snap.getValue<Status>()?.let { statuses.add(it) }
                           }
                           if(statuses.isEmpty())return
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
    override fun onDestroy() {
        super.onDestroy()
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
}
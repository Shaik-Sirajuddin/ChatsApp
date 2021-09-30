package com.chatsapp.www

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.chatsapp.www.Models.Message
import com.chatsapp.www.Adapters.MessagesAdapter
import com.chatsapp.www.Models.MySingleton
import com.chatsapp.www.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import com.android.volley.VolleyError
import com.chatsapp.www.Models.CurrentUser


class ChatActivity : AppCompatActivity() {
    private lateinit var binding:ActivityChatBinding
    private lateinit var adapter: MessagesAdapter
    private lateinit var senderRoom:String
    private lateinit var receiverRoom:String
    private lateinit var dataBase:FirebaseDatabase
    private lateinit var mAuth:FirebaseAuth
    private lateinit var storage:FirebaseStorage
    private  var receiverUid:String? = null
    private var name:String? = null
    private var token:String? = null
    private var flag:Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val intent = intent
        name = intent.getStringExtra("name")
        receiverUid = intent.getStringExtra("uid")
        val userImage = intent.getStringExtra("userImage")
        token = intent.getStringExtra("token")
        binding.chatName.text = name
        if(userImage!=null){
            Glide.with(this).load(userImage).placeholder(R.drawable.profile).into(binding.chatImage)
        }
        mAuth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        val senderUid:String = mAuth.uid!!
        supportActionBar?.setDisplayShowTitleEnabled(false)
        senderRoom = senderUid+receiverUid
        receiverRoom = receiverUid+senderUid
        adapter = MessagesAdapter(this,senderRoom,receiverRoom)
        val layout = LinearLayoutManager(this)
        binding.chatRecycle.layoutManager = layout
        binding.chatRecycle.adapter = adapter
        dataBase = FirebaseDatabase.getInstance()
        binding.sendButton.setOnClickListener{
            val msg = binding.editText.editableText.toString()
            if(msg.isEmpty())return@setOnClickListener
            val message = Message(adapter.getSize(),msg,Date().time,senderUid)
            GlobalScope.launch {
                sendMessage(message)
            }
            binding.editText.setText("")
        }
      dataBase.reference
           .child("Chats")
           .child(senderRoom)
           .child("Messages")
           .addChildEventListener(object:ChildEventListener{
             override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                 try {
                     val msg = snapshot.getValue<Message>()
                     if (msg != null) {
                         adapter.addData(msg,msg.msgPos)
                         binding.chatRecycle.smoothScrollToPosition(msg.msgPos)
                     }
                 }catch (e:Exception){
                     Log.e("childAdded",e.message.toString())
                 }
             }

             override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                 try {
                     val msg = snapshot.getValue<Message>()
                     if (msg != null) {
                         adapter.messageChanged(msg)
                     }
                 }catch (e:Exception){
                     Log.e("childChanged",e.message.toString())
                 }
             }

             override fun onChildRemoved(snapshot: DataSnapshot) {

             }

             override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

             }

             override fun onCancelled(error: DatabaseError) {

             }

         })
        binding.fileLink.setOnClickListener {
            val inte = Intent(Intent.ACTION_GET_CONTENT)
            inte.type = "image/*"
            resultLauncher.launch(inte)
        }
        binding.chatBackButton.setOnClickListener {
            finish()
        }
        val handler = Handler(Looper.getMainLooper())
        binding.editText.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                val map = HashMap<String,Any>()
                map["status"] = "typing..."
                map["room"] = receiverUid!!
                dataBase.reference.child("Presence").child(mAuth.uid!!).updateChildren(map)
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({
                    val map = HashMap<String,Any>()
                    map["status"] = "Online"
                    map["room"] = receiverUid!!
                    dataBase.reference.child("Presence").child(mAuth.uid!!).updateChildren(map)
                },1500)
            }

        })
        dataBase.reference.child("Presence").child(receiverUid!!).addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    var i = 1
                    var status  = ""
                     flag = false
                    for(snap in snapshot.children){
                      val value = snap.getValue<String>()
                        if(snap.key == "status"){
                            status = value.toString()
                        }else{
                            flag = value == senderUid
                        }
                        i++
                    }
                    if(status =="typing..."){
                        if(flag){
                            binding.chatStatus.text = status
                        }else{
                            binding.chatStatus.text = "Online"
                        }
                    }
                    else{
                        binding.chatStatus.text = status
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    override fun onResume() {
        super.onResume()
        val map = HashMap<String,Any>()
        map["status"] = "Online"
        map["room"] = receiverUid!!
        dataBase.reference.child("Presence").child(mAuth.uid!!).updateChildren(map)

    }

    override fun onPause() {
        super.onPause()
        val map = HashMap<String,Any>()
        map["status"] = ""
        map["room"] = ""
        dataBase.reference.child("Presence").child(mAuth.uid!!).updateChildren(map)
    }

    private  fun sendMessage(message: Message){
        val map = HashMap<String,String>()
        map["lastMsg"] = message.message
        map["time"] = message.timeStamp.toString()
        dataBase.reference.child("Chats").child(senderRoom).updateChildren(map as Map<String, Any>)
        dataBase.reference.child("Chats").child(receiverRoom).updateChildren(map as Map<String, Any>)
       val randomKey = dataBase.reference.push().key
            message.messageId = randomKey.toString()

        dataBase.reference
            .child("Chats")
            .child(senderRoom)
            .child("Messages")
            .child(randomKey.toString())
            .setValue(message)

        dataBase.reference
            .child("Chats")
            .child(receiverRoom)
            .child("Messages")
            .child(randomKey.toString())
            .setValue(message)
        if(!flag) {
            if (CurrentUser.user != null) {
                sendNotification(CurrentUser.user!!.userName, message.message)
            } else {
                sendNotification(name, message.message)
            }
        }
    }

    private fun sendNotification(name: String?, message: String) {
         val queue = MySingleton.getInstance(this.applicationContext).requestQueue
        val url = "https://fcm.googleapis.com/fcm/send"
        val data = JSONObject()
        data.put("title",name)
        data.put("body",message)
        val notificationData = JSONObject()
        notificationData.put("notification",data)
        notificationData.put("to",token)
        val request= object : JsonObjectRequest(Method.POST,url, notificationData,
            Response.Listener{ response ->

            },
            Response.ErrorListener { error ->
                    Log.e("errorPost",error.message.toString())
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String,String> {
                val headers = HashMap<String, String>()
                val key = "Key=AAAArXphxhU:APA91bHwOxltVo8yC2r6SkSS-ujIZYbxCrbls81fN0VH9FUQEBuWY8bKCUecgodcnDHbeE0h7LH5flR77NLJU1J4sR6x9nBMV9vRHYhSfX7T0LF7I5gylZVzYFXYDVheVb17gVjhw1MV"
                headers["Authorization"] = key
                headers["Content-Type"] = "application/json"
                return headers
            }
        }
         queue.add(request)
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        result.data?.let{intent->
            intent.data?.let{uri->
                Toast.makeText(this,"Uploading Image", Toast.LENGTH_SHORT).show()
                val date = Date().time.toString()
                val reference = storage.reference.child("Chats").child(mAuth.uid + date)
                GlobalScope.launch {
                    reference.putFile(uri).await()
                    val downloadUri =  reference.downloadUrl.await()
                    val  flag =  putStatus(downloadUri,date)
                    withContext(Dispatchers.Main){
                        if(flag){
                            Toast.makeText(applicationContext,"Upload Successful", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Toast.makeText(applicationContext,"Upload Failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        }
    }

    private fun putStatus(downloadUri: Uri?, date: String): Boolean {
      if(downloadUri==null)return false
        val message = Message(adapter.getSize(),"",date.toLong(),mAuth.uid!!,imageUri = downloadUri.toString())
        sendMessage(message)
          return true
    }

}
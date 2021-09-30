package com.chatsapp.www

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chatsapp.www.Models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UsersAdapter(private val context:Context): RecyclerView.Adapter<UsersViewHolder>() {

    private val data = ArrayList<User>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.chats_layout_item,parent,false)
        val holder = UsersViewHolder(itemView)
        return holder
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        holder.chatName.text = data[position].userName
        Glide.with(context).load(Uri.parse(data[position].profileImage)).placeholder(R.drawable.profile).into(holder.chatImage)
        holder.itemView.setOnClickListener{
           val intent = Intent(context,ChatActivity::class.java)
           intent.putExtra("uid",data[position].uid)
           intent.putExtra("name",data[position].userName)
            intent.putExtra("userImage",data[position].profileImage)
            intent.putExtra("token",data[position].token)
           context.startActivity(intent)
       }
        val userRoom = FirebaseAuth.getInstance().uid+data[position].uid
        FirebaseDatabase.getInstance().reference.child("Chats").child(userRoom).addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val lastMsg = snapshot.child("lastMsg").getValue<String>()
                    val lastMsgTime = snapshot.child("time").getValue<String>()?.toLong()
                    holder.recentChat.text = lastMsg
                    if(lastMsg?.isEmpty() == true){
                        holder.recentChat.text = "photo"
                    }
                    holder.chatTime.text = SimpleDateFormat("hh:mm a").format(Date(lastMsgTime!!)).toString()
                }
                else{
                    holder.recentChat.text = "Tap to chat"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun getItemCount(): Int {
        return data.size
    }
    fun addData(user:User){
        data.add(user)
        notifyItemInserted(data.size-1)
    }
    fun updateData(newData:ArrayList<User>){
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }
}

class UsersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
     val chatImage:CircleImageView = itemView.findViewById(R.id.circleImageView)
     val chatName: TextView  = itemView.findViewById(R.id.chatName)
     val recentChat:TextView = itemView.findViewById(R.id.lastChat)
     val chatTime:TextView   = itemView.findViewById(R.id.chatTime)
}

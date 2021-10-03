package com.chatsapp.www

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chatsapp.www.storage.UserAndModMessage
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UsersAdapter(private val context:Context, private var data: ArrayList<UserAndModMessage>): RecyclerView.Adapter<UsersViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val itemView =
            LayoutInflater.from(context).inflate(R.layout.chats_layout_item, parent, false)
        return UsersViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        try {
            holder.chatName.text = data[position].user.userName
            Glide.with(context).load(Uri.parse(data[position].user.profileImage))
                .placeholder(R.drawable.profile).into(holder.chatImage)
            holder.itemView.setOnClickListener {
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("uid", data[position].user.uid)
                intent.putExtra("name", data[position].user.userName)
                intent.putExtra("userImage", data[position].user.profileImage)
                intent.putExtra("token", data[position].user.token)
                context.startActivity(intent)
            }
            holder.recentChat.text = data[position].modMessage.message.message
            val time = data[position].modMessage.message.timeStamp
            holder.chatTime.text = SimpleDateFormat("hh:mm a").format(Date(time)).toString()
        }catch (e:Exception){
            Log.e("er",e.message.toString())
            e.printStackTrace()
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }
}

class UsersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
     val chatImage:CircleImageView = itemView.findViewById(R.id.circleImageView)
     val chatName: TextView  = itemView.findViewById(R.id.chatName)
     val recentChat:TextView = itemView.findViewById(R.id.lastChat)
     val chatTime:TextView   = itemView.findViewById(R.id.chatTime)
}

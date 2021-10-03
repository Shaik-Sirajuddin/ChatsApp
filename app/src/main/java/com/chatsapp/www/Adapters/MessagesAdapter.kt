package com.chatsapp.www.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chatsapp.www.Models.Message
import com.chatsapp.www.R
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MessagesAdapter(val context: Context,private val data:ArrayList<Message>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val MSG_SENT = 1
    val MSG_RECEIVE = 2
    var dataSize = data.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType==MSG_SENT){
            val view = LayoutInflater.from(context).inflate(R.layout.sender_item,parent,false)
            SendViewHolder(view)
        } else{
            val view = LayoutInflater.from(context).inflate(R.layout.receiver_item,parent,false)
            ReceiveViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(FirebaseAuth.getInstance().uid == data[position].senderId){
            MSG_SENT
        } else{
            MSG_RECEIVE
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder.javaClass == SendViewHolder::class.java){
            val senderHolder = holder as SendViewHolder
            senderHolder.message.text = data[position].message
            senderHolder.time.text = SimpleDateFormat("hh:mm a").format(Date(data[position].timeStamp)).toString()
            if(data[position].imageUri!=null){
                Glide.with(context).load(data[position].imageUri)
                                   .placeholder(R.drawable.profile)
                                   .into(senderHolder.image)
                senderHolder.image.visibility = View.VISIBLE
            }
        }else{
            val receiverHolder = holder as ReceiveViewHolder
            receiverHolder.message.text = data[position].message
            receiverHolder.time.text =SimpleDateFormat("hh:mm a").format(Date(data[position].timeStamp)).toString()
            if(data[position].imageUri!=null){
                Glide.with(context).load(data[position].imageUri)
                    .placeholder(R.drawable.profile)
                    .into(receiverHolder.image)
                receiverHolder.image.visibility = View.VISIBLE
            }
        }
    }
    override fun getItemCount(): Int {
       return data.size
    }
}
class SendViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
    val message:TextView = itemView.findViewById(R.id.sender_msg)
    val time:TextView = itemView.findViewById(R.id.msg_time)
    val image:ImageView = itemView.findViewById(R.id.Image)
}
class ReceiveViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
    val message:TextView = itemView.findViewById(R.id.receive_msg)
    val time:TextView = itemView.findViewById(R.id.msg_time)
    val image:ImageView = itemView.findViewById(R.id.Image)
}
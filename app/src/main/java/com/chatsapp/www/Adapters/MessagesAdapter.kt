package com.chatsapp.www.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chatsapp.www.Models.Message
import com.chatsapp.www.R
import com.github.pgreze.reactions.ReactionPopup
import com.github.pgreze.reactions.dsl.reactionConfig
import com.github.pgreze.reactions.dsl.reactions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MessagesAdapter(val context: Context,val senderRoom:String,val receiverRoom:String): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val data = ArrayList<Message>()
    val MSG_SENT = 1
    val MSG_RECEIVE = 2
    var dataSize = data.size
    val list = intArrayOf(
        R.drawable.ic_fb_like,
        R.drawable.ic_fb_love,
        R.drawable.ic_fb_laugh,
        R.drawable.ic_fb_wow,
        R.drawable.ic_fb_sad,
        R.drawable.ic_fb_angry
    )
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
        val config = reactionConfig(context) {
            reactions {
                resId    { R.drawable.ic_fb_like }
                resId    { R.drawable.ic_fb_love }
                resId    { R.drawable.ic_fb_laugh }
                reaction { R.drawable.ic_fb_wow scale ImageView.ScaleType.FIT_XY }
                reaction { R.drawable.ic_fb_sad scale ImageView.ScaleType.FIT_XY }
                reaction { R.drawable.ic_fb_angry scale ImageView.ScaleType.FIT_XY }
            }
        }
        val popup = ReactionPopup(context, config, { pos: Int? ->
            if(holder.javaClass == SendViewHolder::class.java){
                if(pos!=null && pos>=0) {
                    val senderHolder = holder as SendViewHolder
                    senderHolder.reaction.setImageResource(list[pos])
                    senderHolder.reaction.visibility = View.VISIBLE
                    val mess = data[position]
                    mess.feeling = pos
                    GlobalScope.launch {
                        updateMessage(mess)
                    }
                }
            }
            else{
                if(pos!=null && pos>=0) {
                    val receiverHolder = holder as ReceiveViewHolder
                    receiverHolder.reaction.setImageResource(list[pos])
                    receiverHolder.reaction.visibility = View.VISIBLE
                    val mess = data[position]
                    mess.feeling = pos
                    GlobalScope.launch {
                        updateMessage(mess)
                    }
                }
            }
            true
        })
        if(holder.javaClass == SendViewHolder::class.java){
            val senderHolder = holder as SendViewHolder
            senderHolder.message.text = data[position].message
            senderHolder.time.text = SimpleDateFormat("hh:mm a").format(Date(data[position].timeStamp)).toString()
            if(data[position].feeling>=0){
                holder.reaction.setImageResource(list[data[position].feeling])
                holder.reaction.visibility = View.VISIBLE
            }
            if(data[position].imageUri!=null){
                Glide.with(context).load(data[position].imageUri)
                                   .placeholder(R.drawable.profile)
                                   .into(senderHolder.image)
                senderHolder.image.visibility = View.VISIBLE
            }
            holder.message.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
                    popup.onTouch(view, motionEvent)

                return@OnTouchListener false
            })
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
            receiverHolder.message.setOnTouchListener(View.OnTouchListener { view, motionEvent ->

                    popup.onTouch(view, motionEvent)

                return@OnTouchListener false
            })
            if(data[position].feeling>=0){
               receiverHolder.reaction.setImageResource(list[data[position].feeling])
            }
        }
    }
    override fun getItemCount(): Int {
       return data.size
    }
    fun getSize():Int {
        if(dataSize<data.size)
            dataSize = data.size
        return dataSize++
    }

    fun addData(msg:Message,position: Int){
        while(data.size<=position){
            data.add(Message())
        }
        data[position] =msg
        notifyItemRangeChanged(position,1)
    }
    fun messageChanged(message: Message){
        data[message.msgPos] = message
        notifyItemChanged(message.msgPos)
    }
    private suspend fun updateMessage(message: Message){
        val dataBase = FirebaseDatabase.getInstance()
        val a =  dataBase.reference
            .child("Chats")
            .child(senderRoom)
            .child("Messages")
            .child(message.messageId)
            .setValue(message).await()
        dataBase.reference
            .child("Chats")
            .child(receiverRoom)
            .child("Messages")
            .child(message.messageId)
            .setValue(message).await()
    }
}
class SendViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
    val message:TextView = itemView.findViewById(R.id.sender_msg)
    val reaction:ImageView = itemView.findViewById(R.id.emoji)
    val time:TextView = itemView.findViewById(R.id.msg_time)
    val image:ImageView = itemView.findViewById(R.id.Image)
}
class ReceiveViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
    val message:TextView = itemView.findViewById(R.id.receive_msg)
    val reaction:ImageView = itemView.findViewById(R.id.emoji)
    val time:TextView = itemView.findViewById(R.id.msg_time)
    val image:ImageView = itemView.findViewById(R.id.Image)
}
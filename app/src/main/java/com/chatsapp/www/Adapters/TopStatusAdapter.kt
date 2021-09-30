package com.chatsapp.www.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chatsapp.www.MainActivity
import com.chatsapp.www.Models.UserStatus
import com.chatsapp.www.R
import com.devlomi.circularstatusview.CircularStatusView
import de.hdodenhof.circleimageview.CircleImageView
import omari.hamza.storyview.model.MyStory
import java.util.*
import kotlin.collections.ArrayList
import omari.hamza.storyview.callback.StoryClickListeners

import omari.hamza.storyview.StoryView




class TopStatusAdapter(val context: Context): RecyclerView.Adapter<TopStatusViewHolder>() {

    private val data = ArrayList<UserStatus>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopStatusViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.top_status_item,parent,false)
        return TopStatusViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopStatusViewHolder, position: Int) {
        val list = data[position].statuses
        list?.let {dList->
            if(dList.size==0)return@let
            val img =dList[dList.size - 1].imageUrl
            Glide.with(context).load(img).placeholder(R.drawable.profile).into(holder.image)
            holder.imageView.setPortionsCount(dList.size)
        }
        holder.imageView.setOnClickListener {
           val myStories: ArrayList<MyStory> = ArrayList()
           data[position].statuses?.forEach { status ->
               myStories.add(MyStory(status.imageUrl, status.uploadDate?.let { it1 -> Date(it1) }))
           }
           val activity = context as MainActivity
           StoryView.Builder(activity.supportFragmentManager)
               .setStoriesList(myStories)
               .setStoryDuration(5000)
               .setTitleText(data[position].userName)
               .setTitleLogoUrl(data[position].userProfileImage)
               .build()
               .show()
       }
    }
    override fun getItemCount(): Int {
        return data.size
    }
    fun addStatus(status: ArrayList<UserStatus>){
        data.clear()
        data.addAll(status)
        notifyItemRangeChanged(0,data.size)
    }
}

class TopStatusViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
  val imageView:CircularStatusView = itemView.findViewById(R.id.circular_status_view)
    val image:CircleImageView = itemView.findViewById(R.id.circleImage)
}

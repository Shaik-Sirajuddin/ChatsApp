package com.chatsapp.www.storage


import androidx.lifecycle.LiveData
import com.google.firebase.database.*


class FirebaseQueryLiveData : LiveData<DataSnapshot?> {
    private val query: Query?
    val childListener = MyChildValueEventListener()
    val valueListener = MyValueEventListener()
    private val type:Int
    private var isActive = true
    private var snap:DataSnapshot? = null
    constructor(query: Query,type: Int) {
        this.query = query
        this.type = type
    }
    constructor(ref: DatabaseReference?,type:Int) {
        query = ref
        this.type = type
        when(type){
            0->{
                query?.addChildEventListener(childListener)
            }
            1->{
                query?.addValueEventListener(valueListener)
            }
            2->{
                query?.addListenerForSingleValueEvent(valueListener)
            }
        }
    }

    override fun onActive() {
       isActive = true
        if(snap!=null){
            value = snap
            snap = null
        }
    }
    override fun onInactive() {
        isActive = false
    }

    inner class MyChildValueEventListener : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            if(isActive) {
                value = snapshot
            }
            else{
                snap = snapshot
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            if(isActive) {
                value = snapshot
            }
            else{
                snap = snapshot
            }
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
        }

        override fun onCancelled(error: DatabaseError) {
        }

    }
    inner class MyValueEventListener: ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
            if(isActive) {
                value = snapshot
            }
            else{
                snap = snapshot
            }
        }

        override fun onCancelled(error: DatabaseError) {
        }

    }
   fun destroy(){
       when(type){
           0->{
               query?.removeEventListener(childListener)
           }
           1->{
               query?.removeEventListener(valueListener)
           }
           2->{
               query?.removeEventListener(valueListener)
           }
       }
   }
}
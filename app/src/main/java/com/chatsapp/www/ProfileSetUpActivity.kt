package com.chatsapp.www

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.chatsapp.www.Models.User
import com.chatsapp.www.databinding.ActivityProfileSetUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.InputStream

class ProfileSetUpActivity : AppCompatActivity() {
    private lateinit var binding:ActivityProfileSetUpBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database:FirebaseDatabase
    private lateinit var storage:FirebaseStorage
    private var selectedImage: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSetUpBinding.inflate(layoutInflater)
        setTheme(R.style.Theme_ChatsApp)
        setContentView(binding.root)
        supportActionBar?.hide()
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        selectedImage = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.drawable.profile))
            .appendPath(resources.getResourceTypeName(R.drawable.profile))
            .appendPath(resources.getResourceEntryName(R.drawable.profile))
            .build()
        binding.profileImg.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            launcher.launch(intent)
        }
        binding.createBut.setOnClickListener {
            val name = binding.userName.editableText.toString()
            if(name.isEmpty()){
                binding.userName.error = "Please Enter Your Name"
                return@setOnClickListener
            }
            if(selectedImage==null){
                Toast.makeText(this,"Please Select An Image",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            selectedImage?.let {img->
                binding.createBut.isEnabled = false
                binding.profileImg.isEnabled = false
                binding.loadBar.visibility = View.VISIBLE
               GlobalScope.launch {
                   createUser(img)
               }
            }
        }
    }
    private suspend fun createUser(img:Uri){
        val reference = storage.reference.child("Profiles").child(mAuth.uid.toString())
        reference.putFile(img).await()
        val image = reference.downloadUrl.await().toString()
        val user = User()
        user.profileImage = image
        user.phoneNumber = mAuth.currentUser?.phoneNumber
        user.uid = mAuth.uid!!
        user.userName = binding.userName.editableText.toString()
        database.reference.child("Users").child(mAuth.uid.toString()).setValue(user).await()
        withContext(Dispatchers.Main) {
            binding.loadBar.visibility = View.GONE
            val intent = Intent(this@ProfileSetUpActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
          result.data?.let {
              it.data.let{uri->
                  binding.profileImg.setImageURI(uri)
                  selectedImage = uri
              }
          }
    }
}

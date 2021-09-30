package com.chatsapp.www

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.chatsapp.www.databinding.ActivityPhoneNumberBinding
import com.google.firebase.auth.FirebaseAuth

class PhoneNumberActivity : AppCompatActivity() {
    private lateinit var binding:ActivityPhoneNumberBinding
    private  var toast: Toast? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneNumberBinding.inflate(layoutInflater)
        setTheme(R.style.Theme_ChatsApp)
        setContentView(binding.root)
        if(FirebaseAuth.getInstance().currentUser!=null){
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        supportActionBar?.hide()
        binding.phoneNumber.requestFocus()
        binding.continueButton.setOnClickListener {
            val number = binding.phoneNumber.editableText.toString().trim()
            if(number.length<10){
                toast?.cancel()
                toast =  Toast.makeText(this,"Enter A Valid Phone Number",Toast.LENGTH_SHORT)
                toast?.show()
            }
            else{
                val intent = Intent(this,OTPActivity::class.java)
                intent.putExtra("phoneNumber",number)
                startActivity(intent)
            }
        }
    }
}
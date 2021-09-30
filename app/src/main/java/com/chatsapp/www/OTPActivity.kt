package com.chatsapp.www

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import com.chatsapp.www.databinding.ActivityOtpactivityBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class OTPActivity : AppCompatActivity() {
    private lateinit var binding:ActivityOtpactivityBinding
    private lateinit var  mAuth:FirebaseAuth
    private var sendOtp:String? = null
    private var token:PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var number:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setTheme(R.style.Theme_ChatsApp)
        setContentView(binding.root)
        supportActionBar?.hide()
        binding.otpVw.requestFocus()
         number = intent.getStringExtra("phoneNumber").toString()
        binding.textVerify.text = "Verify $number"
        mAuth = FirebaseAuth.getInstance()
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber("+91$number")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        binding.otpVw.setOtpCompletionListener { otp->
            sendOtp?.let{ sendOtp->
                binding.progressBar.visibility = View.VISIBLE
                val credential = PhoneAuthProvider.getCredential(sendOtp, otp)
                signIn(credential)
            }
        }
        binding.resend.setOnClickListener {
            token?.let{ tk->
                number.let { it1 -> resendOTP(it1,tk)
                }
            }
        }
        binding.continueBut.setOnClickListener {
            finish()
        }
    }

    private fun signIn(credential: PhoneAuthCredential) {
        binding.progressBar.visibility = View.VISIBLE
         mAuth.signInWithCredential(credential).addOnCompleteListener { task->
              if(task.isSuccessful){
                  val user:FirebaseUser = task.result?.user!!
                  val creationTimestamp: Long? = user.metadata?.creationTimestamp
                  val lastSignInTimestamp: Long? = user.metadata?.lastSignInTimestamp
                  if (creationTimestamp != lastSignInTimestamp) {
                     checkUser()
                      return@addOnCompleteListener
                  }
                  binding.progressBar.visibility = View.GONE
                  timer.cancel()
                  val intent = Intent(this,ProfileSetUpActivity::class.java)
                  intent.putExtra("phoneNumber",number)
                  startActivity(intent)
                  finishAffinity()
              }
              else{
                  binding.progressBar.visibility = View.GONE
                  Toast.makeText(this,"Verification Failed",Toast.LENGTH_SHORT).show()
              }
         }
    }

    private fun checkUser() {
        binding.progressBar.visibility = View.GONE
        timer.cancel()
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }

    private val callbacks = object:PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
               signIn(credential)
        }
        override fun onVerificationFailed(p0: FirebaseException){
           Toast.makeText(applicationContext,"Invalid Phone Number",Toast.LENGTH_LONG).show()
            binding.continueBut.visibility = View.VISIBLE
        }
        override fun onCodeSent(verifyId: String, p1: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(verifyId, p1)
            timer.start()
            sendOtp = verifyId
            token = p1
        }
    }
    private var timer =object:CountDownTimer(60000,1000){
        override fun onTick(p0: Long) {
            val min:Int = (p0/1000).toInt()
            binding.resend.text = "Please Wait:$min"
            binding.resend.isClickable  = false
        }
        override fun onFinish() {
            binding.resend.text = getString(R.string.resend_otp)
            binding.resend.isClickable  = true
        }
    }
    private fun resendOTP(mobileNo: String, token:PhoneAuthProvider.ForceResendingToken){
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber("+91$mobileNo")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .setForceResendingToken(token)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}
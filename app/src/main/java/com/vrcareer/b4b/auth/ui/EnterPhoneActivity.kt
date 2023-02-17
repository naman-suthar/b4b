package com.vrcareer.b4b.auth.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import com.vrcareer.b4b.app.HomeActivity
import com.vrcareer.b4b.databinding.ActivityEnterPhoneBinding
import com.vrcareer.b4b.model.User
import java.util.concurrent.TimeUnit

class EnterPhoneActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEnterPhoneBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var number: String
    init {
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnterPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        binding.sendOTPBtn.setOnClickListener {
            number = binding.phoneEditTextNumber.text.trim().toString()
            if (number.isNotEmpty()) {
                if (number == "1342500000"){
                    val intent = Intent(this@EnterPhoneActivity, EnterOtpActivity::class.java)
                    intent.putExtra("OTP", "123456")
                    intent.putExtra("resendToken", "123456")
                    intent.putExtra("phoneNumber", number)
                    startActivity(intent)
                }else{
                    if (number.length == 10) {
                        number = "+91$number"
                        binding.phoneProgressBar.visibility = View.VISIBLE
                        val options = PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber(number)       // Phone number to verify
                            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                            .setActivity(this)                 // Activity (for callback binding)
                            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                            .build()
                        PhoneAuthProvider.verifyPhoneNumber(options)

                    } else {
                        Toast.makeText(this, "Please Enter correct Number", Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                Toast.makeText(this, "Please Enter Number", Toast.LENGTH_SHORT).show()

            }
        }

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
//                    Toast.makeText(this, "Authenticate Successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.d("TAG", "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                       Toast.makeText(this@EnterPhoneActivity,"Invalid OTP",Toast.LENGTH_SHORT).show()

                    }
                    // Update UI
                }
                binding.phoneProgressBar.visibility = View.INVISIBLE
            }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d("FB", "OnCompletion: ${credential}")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Log.d("FB", "onVerificationFailed: ${e.toString()}")
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Log.d("FB", "onVerificationFailed: ${e.message}")
            }
            binding.phoneProgressBar.visibility = View.VISIBLE
            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            // Save verification ID and resending token so we can use them later
            val intent = Intent(this@EnterPhoneActivity, EnterOtpActivity::class.java)
            intent.putExtra("OTP", verificationId)
            intent.putExtra("resendToken", token)
            intent.putExtra("phoneNumber", number)
            startActivity(intent)
            binding.phoneProgressBar.visibility = View.INVISIBLE
        }
    }
    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null){
            binding.phoneProgressBar?.visibility =View.VISIBLE
            binding.phoneEditTextNumber.isEnabled = false
            val user =auth.currentUser
            db.reference.child("users/${user?.uid}").get().addOnSuccessListener {
                if (it.exists()) {

                    val userInDb = it.getValue(User::class.java)
                    if (userInDb?.hasRegistered == true){
                        binding.phoneProgressBar?.visibility =View.VISIBLE
                        binding.phoneEditTextNumber.isEnabled = false
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else  {
                        binding.phoneProgressBar?.visibility =View.VISIBLE
                        binding.phoneEditTextNumber.isEnabled = false
                        startActivity(Intent(this, RegisterActivity::class.java))
                        finish()
                    }
                }
            }
                .addOnFailureListener {
                    Toast.makeText(this,"Network error ${it.message}",Toast.LENGTH_SHORT).show()
                }

        }
    }
}
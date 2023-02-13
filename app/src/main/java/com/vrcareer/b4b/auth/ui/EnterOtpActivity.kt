package com.vrcareer.b4b.auth.ui

import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import com.vrcareer.b4b.MyApplication
import com.vrcareer.b4b.OtpBroadCastReceiver
import com.vrcareer.b4b.R
import com.vrcareer.b4b.databinding.ActivityEnterOtpBinding
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import com.vrcareer.b4b.app.HomeActivity
import com.vrcareer.b4b.model.User

class EnterOtpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEnterOtpBinding
    private lateinit var OTP: String
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var phoneNumber: String
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase

    private val REQ_USER_CONSENT = 200
    var otpBroadCastReceiver: OtpBroadCastReceiver? =null

    override fun onStart() {
        super.onStart()
        registerBroadCast()
        startSmartUserConsent()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnterOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        OTP = intent.getStringExtra("OTP").toString()
        resendToken = intent.getParcelableExtra("resendToken")
        phoneNumber = intent.getStringExtra("phoneNumber")!!
        binding.otpProgressBar.visibility = View.INVISIBLE
        addTextChangeListener()
        resendOTPTvVisibility()

        binding.resendTextView.setOnClickListener {
            resendVerificationCode()
            resendOTPTvVisibility()
        }

        binding.verifyOTPBtn.setOnClickListener {
            //collect otp from all the edit texts
            val typedOTP =
                (binding.otpEditText1.text.toString() + binding.otpEditText2.text.toString() + binding.otpEditText3.text.toString()
                        + binding.otpEditText4.text.toString() + binding.otpEditText5.text.toString() + binding.otpEditText6.text.toString())

            if (typedOTP.isNotEmpty()) {
                if (typedOTP.length == 6) {
                    if (typedOTP== "123456"&& phoneNumber == "1342500000"){
                        auth.signInWithEmailAndPassword("namansuthar12345@gmail.com","123456").addOnSuccessListener {

                        }
                        val userID = "6nMkjqTOBqeAj7tQP2Qhjvnobpm1"
                        Toast.makeText(this, "Authenticate Successfully", Toast.LENGTH_SHORT).show()
                        val createUserInDB = User(
                            id = userID,
                            /* qualification = null,
                             additionalInfo = null,
                             approved_jobs = null,
                             hasRegistered = false*/
                        )
                        var isUserCreated:Boolean = false
                        db.reference.child("users/${userID}").get().addOnSuccessListener {
                            if (it.exists()) {
                                isUserCreated = true
//                                val userInDb = it.getValue(User::class.java)
//                                if (userInDb?.hasRegistered == true){
                                    startActivity(Intent(this, HomeActivity::class.java))
                                    finish()
                               /* } else  {
                                    startActivity(Intent(this, RegisterActivity::class.java))
                                    finish()
                                }*/
                            }

                        }.continueWith {
                            if (!isUserCreated){
                              db.reference.child("users").child(userID).setValue(createUserInDB)
                                startActivity(Intent(this, RegisterActivity::class.java))
                                finish()
                            }
                        }
                    }
                    else{
                        val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                            OTP, typedOTP
                        )
                        binding.otpProgressBar.visibility = View.VISIBLE
                        signInWithPhoneAuthCredential(credential)
                    }

                } else {
                    Toast.makeText(this, "Please Enter Correct OTP", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please Enter OTP", Toast.LENGTH_SHORT).show()
            }


        }
    }

    private fun startSmartUserConsent() {
        val client = SmsRetriever.getClient(this)
        client.startSmsUserConsent(null).addOnCompleteListener {task->
           /* if (task.isComplete){
                Toast.makeText(this, "Listenning", Toast.LENGTH_SHORT).show()
            } else{
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()

            }*/
        }
    }

    private fun registerBroadCast(){
        otpBroadCastReceiver = OtpBroadCastReceiver()

        otpBroadCastReceiver!!.smsBroadCastReceiverListener = object :
            OtpBroadCastReceiver.OtpBroadCastReceiverListener {
            override fun onSuccess(intent: Intent) {
                startActivityForResult(intent,REQ_USER_CONSENT)
            }

            override fun onFailure() {

            }

        }
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(otpBroadCastReceiver,intentFilter,SmsRetriever.SEND_PERMISSION,null)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_USER_CONSENT){
            if (resultCode == RESULT_OK && data != null){
                val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                getOTPfromMessage(message)
            }
        }
    }

    private fun getOTPfromMessage(message: String?) {
        val otpPattern = Pattern.compile("(|^)\\d{6}")
        val matcher = otpPattern.matcher(message)
        if (matcher.find()){
            val typedOTP = matcher.group(0)
            val credential: PhoneAuthCredential? = typedOTP?.let {
                PhoneAuthProvider.getCredential(
                    OTP, it
                )
            }
            binding.otpProgressBar.visibility = View.VISIBLE
            if (credential != null) {
                signInWithPhoneAuthCredential(credential)
            }
        }
    }

    /*override fun onStart() {
        super.onStart()

    }*/

    override fun onStop() {
        super.onStop()
        unregisterReceiver(otpBroadCastReceiver)
    }
    private fun resendOTPTvVisibility() {
        binding.otpEditText1.setText("")
        binding.otpEditText2.setText("")
        binding.otpEditText3.setText("")
        binding.otpEditText4.setText("")
        binding.otpEditText5.setText("")
        binding.otpEditText6.setText("")
        binding.resendTextView.visibility = View.INVISIBLE
        binding.resendTextView.isEnabled = false

        Handler(Looper.myLooper()!!).postDelayed(Runnable {
            binding.resendTextView.visibility = View.VISIBLE
            binding.resendTextView.isEnabled = true
        }, 60000)
    }
    private fun resendVerificationCode() {
        if (OTP == "123456"){
            Toast.makeText(this,"This is test Account Enter 123456 as OTP",Toast.LENGTH_SHORT).show()
            return
        }
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken!!)// OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            }
            binding.otpProgressBar.visibility = View.VISIBLE
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
            OTP = verificationId
            resendToken = token
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this@EnterOtpActivity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information

                    Toast.makeText(this, "Authenticate Successfully", Toast.LENGTH_SHORT).show()
                    val user =auth.currentUser
                    val createUserInDB = User(
                        id = user?.uid,
                       /* qualification = null,
                        additionalInfo = null,
                        approved_jobs = null,
                        hasRegistered = false*/
                    )
                    var isUserCreated:Boolean = false
                    db.reference.child("users/${user?.uid}").get().addOnSuccessListener {
                        if (it.exists()) {
                            isUserCreated = true
                            val userInDb = it.getValue(User::class.java)
                            if (userInDb?.hasRegistered == true){
                                startActivity(Intent(this, HomeActivity::class.java))
                                finish()
                            } else  {
                                startActivity(Intent(this, RegisterActivity::class.java))
                                finish()
                            }
                        }

                    }.continueWith {
                        if (!isUserCreated){
                            user?.uid?.let { db.reference.child("users").child(it).setValue(createUserInDB) }
                            startActivity(Intent(this, RegisterActivity::class.java))
                            finish()
                        }
                    }



                } else {
                    // Sign in failed, display a message and update the UI
                    Log.d("TAG", "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this,"The Code is Invalid",Toast.LENGTH_SHORT).show()
                        binding.otpProgressBar.visibility = View.INVISIBLE
                    }
                    // Update UI
                }
                binding.otpProgressBar.visibility = View.INVISIBLE
            }
    }



    private fun addTextChangeListener() {
        binding.otpEditText1.addTextChangedListener(EditTextWatcher(binding.otpEditText1))
        binding.otpEditText2.addTextChangedListener(EditTextWatcher(binding.otpEditText2))
        binding.otpEditText3.addTextChangedListener(EditTextWatcher(binding.otpEditText3))
        binding.otpEditText4.addTextChangedListener(EditTextWatcher(binding.otpEditText4))
        binding.otpEditText5.addTextChangedListener(EditTextWatcher(binding.otpEditText5))
        binding.otpEditText6.addTextChangedListener(EditTextWatcher(binding.otpEditText6))
    }

    inner class EditTextWatcher(private val view: View) : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun afterTextChanged(p0: Editable?) {

            val text = p0.toString()
            when (view.id) {
                R.id.otpEditText1 -> if (text.length == 1) binding.otpEditText2.requestFocus()
                R.id.otpEditText2 -> if (text.length == 1) binding.otpEditText3.requestFocus() else if (text.isEmpty()) binding.otpEditText1.requestFocus()
                R.id.otpEditText3 -> if (text.length == 1) binding.otpEditText4.requestFocus() else if (text.isEmpty()) binding.otpEditText2.requestFocus()
                R.id.otpEditText4 -> if (text.length == 1) binding.otpEditText5.requestFocus() else if (text.isEmpty()) binding.otpEditText3.requestFocus()
                R.id.otpEditText5 -> if (text.length == 1) binding.otpEditText6.requestFocus() else if (text.isEmpty()) binding.otpEditText4.requestFocus()
                R.id.otpEditText6 -> if (text.isEmpty()) binding.otpEditText1.requestFocus()

            }
        }

    }
}
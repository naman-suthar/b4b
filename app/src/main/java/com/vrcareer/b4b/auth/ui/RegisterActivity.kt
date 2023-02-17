package com.vrcareer.b4b.auth.ui

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vrcareer.b4b.R
import com.vrcareer.b4b.app.HomeActivity
import com.vrcareer.b4b.databinding.ActivityRegisterBinding
import com.vrcareer.b4b.model.EarningDTO
import com.vrcareer.b4b.model.User
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private var cal = Calendar.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding?.etUserMobileNumber?.setText(
            auth?.currentUser?.phoneNumber)
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }
        binding?.datePicker?.setOnClickListener {
            DatePickerDialog(this@RegisterActivity,
                dateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }
        val genders = resources.getStringArray(R.array.Genders)
        val adapter = ArrayAdapter(
            this@RegisterActivity,
            android.R.layout.simple_spinner_dropdown_item, genders
        )
        binding.spinnerGender.adapter = adapter
        var gender = genders[0]
        binding?.spinnerGender?.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                gender = genders[p2]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
        binding.btnRegister.setOnClickListener {
            val name = binding?.etUserName?.text.toString()
            val mobileNo = binding?.etUserMobileNumber?.text.toString()
            val dob = binding?.etUserDob?.text.toString()
            val email = binding?.etUserEmail?.text.toString()

            if (!name.isNullOrEmpty()  && !dob.isNullOrEmpty() && !email.isNullOrEmpty() && gender != genders[0]){
                val user = auth.currentUser
                val updateUser = User(
                    user?.uid,
                    name = name,
                    phoneNo = mobileNo,
                    dob = dob,
                    email = email,
                    gender= gender,
                    hasRegistered = true,
                    network = mutableListOf()
                )

                db.reference.child("users/${user?.uid}").setValue(updateUser).addOnCompleteListener {
                    if (it.isSuccessful){
                        val blankEarning = EarningDTO(
                            userid = user?.uid,
                            balance = 0,
                            total_earning = 0,
                            total_pending = 0,
                            total_withdrawal = 0,
                            pending_withdrawal = 0
                        )
                        db.reference.child("earnings").child(user?.uid.toString()).setValue(blankEarning)

                        startActivity(Intent(this,HomeActivity::class.java))
                        finish()
                    } else{
                        Toast.makeText(this,"Please Try after Some time",Toast.LENGTH_SHORT).show()


                    }
                }
            }else{
                if (name.isNullOrEmpty()){
                    binding.etUserName.error = "Empty"
                    binding.etUserName.requestFocus()
                }
              /*  if (mobileNo.isNullOrEmpty()){
                    binding.etUserMobileNumber.error = "Empty"
                    binding.etUserMobileNumber.requestFocus()
                }*/
                if (dob.isNullOrEmpty()){
                    binding.etUserDob.error = "Empty"
                    binding.etUserDob.requestFocus()
                }
                if (email.isNullOrEmpty()){
                    binding.etUserEmail.error = "Empty"
                    binding.etUserEmail.requestFocus()
                }
                if (gender == genders[0]){
                    binding?.spinnerGender?.requestFocus()
                    Toast.makeText(this,"Select Gender Please",Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
    private fun updateDateInView() {
        val myFormat = "MM/dd/yyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding.etUserDob.setText(sdf.format(cal.time))
    }
}
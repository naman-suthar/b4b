package com.vrcareer.b4b.app.profile

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vrcareer.b4b.R
import com.vrcareer.b4b.model.KYCDetails
import com.vrcareer.b4b.databinding.FragmentAddKycDetailsBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adding KYC Details here*/
class AddKycDetailsFragment : Fragment() {

    private var binding: FragmentAddKycDetailsBinding? = null
    private var cal = Calendar.getInstance()
    private var gender = "Gender"
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var existedKycDetails: KYCDetails? = null

    init {
        db.reference.child("KYC/${auth.currentUser?.uid}").get().addOnSuccessListener {
            if (it.exists()) {

                val kycDetail = it.getValue(KYCDetails::class.java)
                existedKycDetails = kycDetail
                Log.d("Existed:", "$existedKycDetails")
                binding?.let { b ->
                    b.etUserName.setText(existedKycDetails?.user_name)
                    b.etUserDob.setText(existedKycDetails?.user_dob)
                    b.etUserPanCard.setText(existedKycDetails?.pan_card)
                    b.etUserAdhaarCard.setText(existedKycDetails?.adhaar_card)
                    b.etUserDrivingLicense.setText(existedKycDetails?.driving_license_no)
                    //Spinner remaining
                    when (existedKycDetails?.user_gender) {
                        "Male" -> b.spinnerGender.setSelection(1)
                        "Female" -> b.spinnerGender.setSelection(2)
//                        "Others" -> b.spinnerGender.setSelection(3)
                        "Transgender" -> b.spinnerGender.setSelection(3)
                        "Prefer not to say" -> b.spinnerGender.setSelection(4)
                        else -> b.spinnerGender.setSelection(0)
                    }

                }
            }
        }
            .addOnFailureListener {e->
                Toast.makeText(context,"Network error ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddKycDetailsBinding.inflate(inflater, container, false)

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }
        binding?.datePicker?.setOnClickListener {
            DatePickerDialog(
                requireActivity(),
                dateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        binding?.topAppBarLocation?.setNavigationOnClickListener {
            val action =
                AddKycDetailsFragmentDirections.actionAddKycDetailsFragmentToProfileHomeFragment()
            findNavController().navigate(action)
        }
        val genders = resources.getStringArray(R.array.Genders)
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item, genders
        )
        binding?.spinnerGender?.adapter = adapter
        binding?.spinnerGender?.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                gender = genders[p2]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }


        binding?.btnAddKyc?.setOnClickListener {
            val user_name = binding?.etUserName?.text.toString()
            val user_dob = binding?.etUserDob?.text.toString()
            val pan_Card = binding?.etUserPanCard?.text.toString()
            val adhaar_card_no = binding?.etUserAdhaarCard?.text.toString()
            val driving_license_no = binding?.etUserDrivingLicense?.text.toString()
            val user_gender = gender

            //Check For the empty and validation
            val userId = auth.currentUser?.uid

            if (user_name.isEmpty()) {
                binding?.etUserName?.requestFocus()
                binding?.etUserName?.error = "Field can't be empty"
            } else if (user_dob.isEmpty()) {
                binding?.etUserDob?.requestFocus()
                binding?.etUserDob?.error = "Field can't be empty"
            } else if (pan_Card.isEmpty()) {
                binding?.etUserPanCard?.requestFocus()
                binding?.etUserPanCard?.error = "Field can't be empty"
            }else if(adhaar_card_no.isEmpty()){
                binding?.etUserAdhaarCard?.requestFocus()
                binding?.etUserAdhaarCard?.error = "Field can't be empty"
            } else if(driving_license_no.isEmpty()){
                binding?.etUserDrivingLicense?.requestFocus()
                binding?.etUserDrivingLicense?.error = "Field can't be empty"
            }
            else if (user_gender == "Gender") {
                binding?.spinnerGender?.requestFocus()
                Toast.makeText(this.requireContext(),"Please Select Gender",Toast.LENGTH_SHORT).show()
            } else {
                val kycDetails = KYCDetails(
                    id = userId,
                    user_name = user_name,
                    user_dob = user_dob,
                    user_gender = user_gender,
                    pan_card = pan_Card,
                    driving_license_no = driving_license_no,
                    adhaar_card = adhaar_card_no
                )
                addKYCDetailsInFireBase(kycDetails, userId)
            }


        }
        return binding?.root
    }

    private fun addKYCDetailsInFireBase(kycDetails: KYCDetails, userId: String?) {
        db.reference.child("KYC/${userId}").setValue(kycDetails).addOnSuccessListener {
            Toast.makeText(this.requireContext(), "KYC Successfully Added", Toast.LENGTH_SHORT)
                .show()
        }.addOnFailureListener {
            Toast.makeText(this.requireContext(), "Something went wrong", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun updateDateInView() {
        val myFormat = "MM/dd/yyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding?.etUserDob?.setText(sdf.format(cal.time))
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(
            true // default to enabled
        ) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,  // LifecycleOwner
            callback
        )
    }
}
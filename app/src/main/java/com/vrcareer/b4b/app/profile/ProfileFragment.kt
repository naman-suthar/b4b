package com.vrcareer.b4b.app.profile

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.navigation.fragment.findNavController
import coil.load
import coil.request.SuccessResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.vrcareer.b4b.R
import com.vrcareer.b4b.databinding.FragmentProfileBinding
import com.vrcareer.b4b.model.AdditionalInfo
import com.vrcareer.b4b.model.Qualification
import com.vrcareer.b4b.model.User
import java.text.SimpleDateFormat
import java.util.*

val indianLanguages = listOf(
    "Hindi",
    "English",
    "Bengali",
    "Telugu",
    "Marathi",
    "Tamil",
    "Urdu",
    "Gujarati",
    "Punjabi",
    "Malayalam",
    "Kannada",
    "Oriya",
    "Sanskrit",
    "Kashamiri",
    "Sindhi",
    "Assamese"
)
val indianSpecializations = listOf(
    "Computer Science",
    "Commerce",
    "Arts",
    "Journalism and Mass Communication",
    "Economics",
    "History",
    "Sociology",
    "Political Science",
    "Psychology",
    "English Literature",
    "Philosophy",
    "Law",
    "Management",
    "Finance",
    "Marketing",
    "Human Resource Management",
    "Mechanical Engineering",
    "Electrical Engineering",
    "Civil Engineering",
    "Electronics and Communication Engineering",
    "Chemical Engineering",
    "Biomedical Engineering",
    "Aerospace Engineering",
    "Information Technology",
    "Petroleum Engineering",
    "Mining Engineering",
    "Agricultural Engineering",
    "Food Technology",
    "Textile Technology"
)
val degreePrograms = listOf(
    "Bachelor of Technology (B.Tech)",
    "Bachelor of Architecture (B.Arch)",
    "Bachelor of Computer Applications (BCA)",
    "Bachelor of Business Administration (BBA)",
    "Bachelor of Medicine and Bachelor of Surgery (MBBS)",
    "Bachelor of Dental Surgery (BDS)",
    "Bachelor of Law (LLB)",
    "Bachelor of Arts (BA)",
    "Bachelor of Science (B.Sc)",
    "Bachelor of Education (B.Ed)"
)
class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private var storage = Firebase.storage
    private var auth = FirebaseAuth.getInstance()
    private var storageReference = storage.reference
    private var db = FirebaseDatabase.getInstance()
    private var imgUri: Uri? = null
    private var progressBar: ProgressDialog? = null
    private var userNow: User? = null
    private var alertDialog: AlertDialog? = null
    private var cal = Calendar.getInstance()
    private val userRef: DatabaseReference =
        db.reference.child("users").child(auth?.currentUser!!.uid)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        progressBar = ProgressDialog(requireContext())
        progressBar?.setTitle("Fetching...")
        progressBar?.show()
        db.reference.child("users").child(auth?.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {

                        userNow = snapshot.getValue(User::class.java)
                        Log.d("User::", "$userNow")
                        setProfilePicture(userNow?.profile_picture)
                        binding?.let { b ->
                            b.tvUserName.text = userNow?.name
                            b.tvUserId.text = userNow?.id
                            b.tvUserDob.text = userNow?.dob
                            b.tvUserEmail.text = userNow?.email
                            b.tvUserPhone.text = userNow?.phoneNo
                            b.tvAltNumber.text = userNow?.additionalInfo?.alternateNo ?: "NA"
                            b.tvUserAddress.text = userNow?.additionalInfo?.address ?: "NA"
                            b.tvUserLanguage.text = userNow?.additionalInfo?.language ?: "NA"
                            b.tvUserPicode.text = userNow?.additionalInfo?.pinCode ?: "NA"
                            b.tvUserInstituteName.text =
                                userNow?.qualification?.institute_name ?: "NA"
                            b.tvUserDegreeGrade.text = userNow?.qualification?.degree ?: "NA"
                            b.tvUserSpecialization.text =
                                userNow?.qualification?.specialization ?: "NA"
                            b.tvUserCourseStarts.text = userNow?.qualification?.starts_from ?: "NA"
                            b.tvUserCourseEnds.text = userNow?.qualification?.ends_on ?: "NA"
                        }



                        progressBar?.dismiss()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "${error.message}", Toast.LENGTH_SHORT).show()
                    progressBar?.dismiss()
                }
            }
            )

        val galleryImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {

                binding?.profileImage?.setImageURI(it)
                imgUri = it

                auth.currentUser?.uid?.let { uid ->
                    imgUri?.let { uri ->
                        progressBar = ProgressDialog(requireContext())
                        progressBar?.setTitle("Uploading")
                        progressBar?.show()
                        storageReference.child("Images/ProfilePics").child(
                            uid
                        ).putFile(uri).addOnSuccessListener { task ->
                            task.metadata!!.reference!!.downloadUrl.addOnSuccessListener { urifs ->
                                Log.d("Firebase", "StorageUri $urifs")
                                db.reference.child("users").child(uid).get()
                                    .addOnSuccessListener { snapshot ->
                                        if (snapshot.exists()) {
                                            val myUser = snapshot.getValue(User::class.java)
                                            val userWithProfile = myUser?.copy(
                                                profile_picture = urifs.toString()
                                            )
                                            db.reference.child("users").child(uid)
                                                .setValue(userWithProfile).addOnSuccessListener {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "Uploaded",
                                                        Toast.LENGTH_SHORT
                                                    ).show()

                                                }
                                            progressBar?.dismiss()
                                        }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            requireContext(),
                                            "Database Error",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                progressBar?.dismiss()
                                    }
                            }
                        }
                    }
                }

            }
        )
        binding?.btnCloseProfile?.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToProfileHomeFragment()
            findNavController().navigate(action)
        }
        binding?.btnEditAdditionalInfo?.setOnClickListener {
            val dialog = MaterialAlertDialogBuilder(requireContext())
            dialog.setTitle("Edit Additional Info")
            val view =
                LayoutInflater.from(requireContext())
                    .inflate(R.layout.dialog_edit_additional_info, null, false)
            dialog.setView(view)
            val alternateNoET: EditText = view.findViewById(R.id.et_dialog_alternate_number)
            val addressET: EditText = view.findViewById(R.id.et_dialog_address)
            val pincodeET: EditText = view.findViewById(R.id.et_dialog_pincode)
            val languageAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, indianLanguages)
            val languageEt: TextInputLayout = view.findViewById(R.id.et_dialog_language)

            alternateNoET.setText(userNow?.additionalInfo?.alternateNo ?: "")
            addressET.setText(userNow?.additionalInfo?.address ?: "")
            pincodeET.setText(userNow?.additionalInfo?.pinCode ?: "")
            languageEt.editText?.setText(userNow?.additionalInfo?.language ?: "")
            (languageEt.editText as? AutoCompleteTextView)?.setAdapter(languageAdapter)
            dialog.setCancelable(false)
            dialog.setPositiveButton("Confirm", null)
                .setNegativeButton("Cancel") { d, _ ->
                    d.dismiss()
                }

            alertDialog = dialog.create()
            alertDialog?.show()
            val positiveButton = alertDialog?.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
            positiveButton?.setOnClickListener {
                val alternateNo = alternateNoET.text.toString()
                val address = addressET.text.toString()
                val pincode = pincodeET.text.toString()
                val userLanguage = languageEt.editText?.text.toString()
                Toast.makeText(
                    requireContext(),
                    "$userLanguage $alternateNo $address $pincode",
                    Toast.LENGTH_SHORT
                ).show()
                val currentAdditionInfo = userNow?.additionalInfo ?: AdditionalInfo()
                if (alternateNo.isNotEmpty()) {
                    currentAdditionInfo?.alternateNo = alternateNo
                }
                if (address.isNotEmpty()) {
                    currentAdditionInfo?.address = address
                }
                if (pincode.isNotEmpty()) {
                    currentAdditionInfo?.pinCode = pincode
                }
                if (userLanguage.isNotEmpty()) {
                    currentAdditionInfo?.language = userLanguage
                }

                val userUpdated = userNow?.copy(
                    additionalInfo = currentAdditionInfo
                )

                userRef.setValue(userUpdated).addOnSuccessListener {
                    Toast.makeText(requireContext(), "UserUpdated", Toast.LENGTH_SHORT).show()
                    alertDialog?.dismiss()
                }
            }

//            Toast.makeText(requireContext(), "Working On this", Toast.LENGTH_SHORT).show()
        }

        binding?.btnEditQualification?.setOnClickListener {


            val dialog = MaterialAlertDialogBuilder(requireContext())
            dialog.setTitle("Edit Qualification Info")
            val view =
                LayoutInflater.from(requireContext())
                    .inflate(R.layout.dialog_edit_qualification_info, null, false)
            dialog.setView(view)

            val instituteNameET: EditText = view.findViewById(R.id.et_dialog_institute_name)
            val degreeGradeET: EditText = view.findViewById(R.id.et_dialog_degree_grade)
            val startDatePicker: ImageView = view.findViewById(R.id.date_picker_starts_from)
            val endsDatePicker: ImageView = view.findViewById(R.id.date_picker_ends_on)
            val startsDateEt: EditText = view.findViewById(R.id.et_dialog_starts_from)
            val endsDate: EditText = view.findViewById(R.id.et_dialog_ends_on)

            val specializationAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                indianSpecializations
            )
            val specializationEt: TextInputLayout = view.findViewById(R.id.et_dialog_specialization)
            (specializationEt.editText as? AutoCompleteTextView)?.setAdapter(specializationAdapter)
            val dateSetListener =
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, monthOfYear)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val myFormat = "MM/dd/yyyy" // mention the format you need
                    val sdf = SimpleDateFormat(myFormat, Locale.US)
                    startsDateEt.setText(sdf.format(cal.time))
                }
            startDatePicker.setOnClickListener {
                DatePickerDialog(
                    requireContext(),
                    dateSetListener,
                    // set DatePickerDialog to point to today's date when it loads up
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            val dateSetListenerEnds =
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, monthOfYear)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val myFormat = "MM/dd/yyyy" // mention the format you need
                    val sdf = SimpleDateFormat(myFormat, Locale.US)
                    endsDate.setText(sdf.format(cal.time))
                }
            endsDatePicker.setOnClickListener {
                DatePickerDialog(
                    requireContext(),
                    dateSetListenerEnds,
                    // set DatePickerDialog to point to today's date when it loads up
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            dialog.setCancelable(false)
            dialog.setPositiveButton("Confirm", null)
                .setNegativeButton("Cancel") { d, _ ->
                    d.dismiss()
                }

            alertDialog = dialog.create()
            alertDialog?.show()
            val positiveButton = alertDialog?.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
            positiveButton?.setOnClickListener {
                val instituteName = instituteNameET.text.toString()
                val degreeGrade = degreeGradeET.text.toString()
                val startsFrom = startsDateEt.text.toString()
                val endsOn = endsDate.text.toString()
                val specialization = specializationEt.editText?.text.toString()
                val currentQualificationInfo = userNow?.qualification ?: Qualification()
                if (instituteName.isEmpty()){
                    instituteNameET.requestFocus()
                    instituteNameET.error = "Institute Name is mandatory"
                } else if (degreeGrade.isEmpty()){
                    degreeGradeET.requestFocus()
                    degreeGradeET.error = "Mandatory Field"
                }else if(specialization.isEmpty()){
                   specializationEt.requestFocus()
                    specializationEt.error = "Please enter Valid value here"
                }else if(startsFrom.isEmpty()){
                    Toast.makeText(requireContext(), "Enter Valid Course Starts Date", Toast.LENGTH_SHORT).show()
                }else if(endsOn.isEmpty()){
                    Toast.makeText(requireContext(), "Enter Valid Course Ends Date", Toast.LENGTH_SHORT).show()
                }else {
                    currentQualificationInfo.institute_name = instituteName
                    currentQualificationInfo.degree = degreeGrade
                    currentQualificationInfo.starts_from = startsFrom
                    currentQualificationInfo.ends_on = endsOn
                    currentQualificationInfo.specialization = specialization

                    val userUpdated = userNow?.copy(
                        qualification = currentQualificationInfo
                    )

                    userRef.setValue(userUpdated).addOnSuccessListener {
                        alertDialog?.dismiss()
                    }
                }

            }

        }






        binding?.btnUploadProfile?.setOnClickListener {


            galleryImage.launch("image/*")


        }
        return binding.root
    }

    private fun setProfilePicture(profilePicture: String?) {
        val imageUrl = profilePicture?.toUri()
        imageUrl?.let {
            binding?.profileImage?.load(it)
        }

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
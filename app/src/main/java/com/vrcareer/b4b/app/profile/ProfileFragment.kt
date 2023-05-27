package com.vrcareer.b4b.app.profile

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.media.Image
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
import coil.Coil
import coil.ImageLoader
import coil.load
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
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
import kotlinx.coroutines.job
import okhttp3.internal.notify
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

/**
 * This is Visit Profile Fragment
 * here we display User's Profile*/
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
//                            b.tvUserId.text = userNow?.id
                            b.tvUserDob.text = userNow?.dob
                            b.tvUserEmail.text = userNow?.email
                            b.tvUserPhone.text = userNow?.phoneNo
                            b.tvUserGender.text = userNow?.gender
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
                                                .addOnFailureListener {e->
                                                    Toast.makeText(context,"Network error ${e.message}",Toast.LENGTH_SHORT).show()
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
                                .addOnFailureListener {e->
                                    Toast.makeText(context,"Image Url error ${e.message}",Toast.LENGTH_SHORT).show()
                                }
                        }
                            .addOnFailureListener {e->
                                Toast.makeText(context,"Storage Network error ${e.message}",Toast.LENGTH_SHORT).show()
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
                }   .addOnFailureListener {e->
                    Toast.makeText(context,"Network error ${e.message}",Toast.LENGTH_SHORT).show()
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
                    }   .addOnFailureListener {e->
                        Toast.makeText(context,"Network error ${e.message}",Toast.LENGTH_SHORT).show()
                    }
                }

            }

        }


        binding?.btnEditPrimaryInfo?.setOnClickListener {
            val dialog = MaterialAlertDialogBuilder(requireContext())
            dialog.setTitle("Edit Primary Info")
            val view =
                LayoutInflater.from(requireContext())
                    .inflate(R.layout.dialog_edit_primary_info, null, false)
            dialog.setView(view)
            val emailEt: EditText = view.findViewById(R.id.et_dialog_email_primary_edit)
            val dobET: EditText = view.findViewById(R.id.et_dob_primary_edit)
            val dobPicker: ImageView = view.findViewById(R.id.date_picker_primary_edit)
            val gender: Spinner = view.findViewById(R.id.spinner_gender_primary_edit)
            val genders = resources.getStringArray(R.array.Genders)
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item, genders
            )
            gender.adapter = adapter
            var genderSelected = userNow?.gender
            emailEt.setText(userNow?.email ?: "")
            dobET.setText(userNow?.dob ?: "")
            val dateSetListener =
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, monthOfYear)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val myFormat = "MM/dd/yyyy" // mention the format you need
                    val sdf = SimpleDateFormat(myFormat, Locale.US)
                    dobET.setText(sdf.format(cal.time))
                }
            dobPicker.setOnClickListener {
                DatePickerDialog(requireContext(),
                    dateSetListener,
                    // set DatePickerDialog to point to today's date when it loads up
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }
            when(userNow?.gender){
                genders[1]-> gender.setSelection(1)
                genders[2] -> gender.setSelection(2)
                genders[3] -> gender.setSelection(3)
            }
            gender.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    genderSelected = genders[p2]
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
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
                val email = emailEt.text.toString()
                val dob = dobET.text.toString()

                val currentUser = userNow?.copy()
                if (email.isNotEmpty()) {
                    currentUser?.email = email
                }
                if (dob.isNotEmpty()) {
                    currentUser?.dob = dob
                }
                if (genderSelected != genders[0]){
                    currentUser?.gender = genderSelected
                }else{
                    Toast.makeText(requireContext(),"Select Gender please",Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }


                userRef.setValue(currentUser).addOnSuccessListener {
                    alertDialog?.dismiss()
                }   .addOnFailureListener {e->
                    Toast.makeText(context,"Network error ${e.message}",Toast.LENGTH_SHORT).show()
                }
            }

//            Toast.makeText(requireContext(), "Working On this", Toast.LENGTH_SHORT).show()
        }



        binding?.btnUploadProfile?.setOnClickListener {

            val dialog = BottomSheetDialog(requireContext())
            val view=layoutInflater.inflate(R.layout.bottom_sheet_profile_options,null)

            val btnEdit: MaterialCardView = view.findViewById(R.id.btn_edit_profile_pic)
            val btnDelete: MaterialCardView = view.findViewById(R.id.btn_delete_profile_pic)
            btnEdit.setOnClickListener {
                galleryImage.launch("image/*")
                dialog.dismiss()
            }
            btnDelete.setOnClickListener {
                storageReference.child("Images/ProfilePics").child(
                    auth.currentUser!!.uid
                ).delete().addOnSuccessListener {
                    Toast.makeText(requireContext(),"Profile Deleted",Toast.LENGTH_SHORT).show()
                }
            }

            dialog.setContentView(view)
            dialog.show()



        }
        return binding.root
    }

    private fun setProfilePicture(profilePicture: String?) {
        val imageUrl = profilePicture?.toUri()
        imageUrl?.let {
            binding?.profileImage?.load(it){
                this.listener(
                    onSuccess = {_,success ->
                        Log.d("ImageOnSuccess","$success")
                    },
                    onError = {_,err ->
                        Log.d("ImageOnSuccess","$err")
                        binding?.profileImage?.setImageDrawable(resources.getDrawable(R.drawable.profile_placeholder))
                    },
                    onStart = {
                        binding?.profileImage?.setImageDrawable(resources.getDrawable(R.drawable.profile_placeholder))
                    }

                )
            }


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
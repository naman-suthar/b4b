package com.vrcareer.b4b.app.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vrcareer.b4b.R
import com.vrcareer.b4b.app.Constants
import com.vrcareer.b4b.databinding.ActivityExploreJobDetailBinding
import com.vrcareer.b4b.model.Job
import com.vrcareer.b4b.model.JobApplication
import com.vrcareer.b4b.utils.ApplicationResponse

/**
 * This is Job Details Activity
 * */
class ExploreJobDetail : AppCompatActivity() {

private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private lateinit var binding: ActivityExploreJobDetailBinding
    private var JOB: Job? = null
    private var JOB_ID: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityExploreJobDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent!=null){
            JOB = intent.getSerializableExtra("job") as Job
            JOB_ID = JOB?.job_id
            val job_description = JOB?.job_description
            binding?.btnFillApplicationForm?.isEnabled = JOB?.status != "Paused"
            binding?.jobDescription?.text = job_description?.replace("""\\n""","\n")
            /**
             * Checking if already our application is in pending status
             * */
            Log.d("MyJobApplicationJOB","$JOB")
            JOB_ID?.let {
                db.reference.child(Constants.USER_JOB_APPLICATION_PATH_ROOT.trim()).child(it.trim())
                    .child(auth.currentUser?.uid.toString().trim())
                    .get().addOnSuccessListener {ds->

                        if (ds.exists()){
                            /**
                            * Check for status and there UI callbacks */
                            val myApplication = ds.getValue(JobApplication::class.java)
                            myApplication?.let {application ->
                                Log.d("MyJobApplication","${myApplication.status}")
                                when(application.status){
                                    ApplicationResponse.Approved.name-> {
                                        binding.btnRejectionMessage.visibility = View.GONE
                                        binding?.btnFillApplicationForm?.let { btn->
                                            btn.isEnabled = false
                                            btn.isClickable = false
                                            btn.text = "Accepted"
                                        }
                                    }
                                    ApplicationResponse.Rejected.name -> {
                                        binding.btnRejectionMessage.visibility = View.VISIBLE
                                        binding.btnRejectionMessage.setOnClickListener {
                                            openDialogForMessage(application.rejection_message)
                                        }
                                        binding.btnFillApplicationForm.let { btn->
                                            btn.isEnabled = true
                                            btn.isClickable = true
                                            btn.text = "Apply Again"
                                        }
                                    }
                                    ApplicationResponse.Pending.name -> {
                                        binding.btnRejectionMessage.visibility = View.GONE
                                        binding?.btnFillApplicationForm?.let { btn->
                                            btn.isEnabled = false
                                            btn.isClickable = false
                                            btn.text = "Already Applied"
                                        }
                                    }

                                    else -> {
                                        binding.btnRejectionMessage.visibility = View.GONE
                                        binding?.btnFillApplicationForm?.let { btn->
                                            btn.isEnabled = true
                                            btn.isClickable = true
                                            btn.text = "Apply Now"
                                        }
                                    }
                                }
                            }

                        }
                        else{
                            binding.btnFillApplicationForm?.let { btn->
                                btn.isEnabled = true
                                btn.isClickable = true
                                btn.text = "Apply Now"
                            }
                        }
                    }
                    .addOnFailureListener {e->
                        Toast.makeText(this,"Network error ${e.message}",Toast.LENGTH_SHORT).show()
                    }
            }

        }
        /**
         * Checking If Job is already Approved
         * */
       /* auth.currentUser?.uid?.let {
            db.reference.child(Constants.USER_FIREBASE_PATH_ROOT).child(it)
                .child("approved_jobs")
                .get()
                .addOnSuccessListener { ds->
                    if (ds.exists()){
                        for (job in ds.children){
                            if (job.value == JOB_ID){
                                binding?.btnFillApplicationForm?.let { btn->
                                    btn.isEnabled = false
                                    btn.isClickable = false
                                    btn.text = "Already Applied"
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener {e->
                    Toast.makeText(this,"Network error ${e.message}",Toast.LENGTH_SHORT).show()
                }

        }*/

        binding.btnFillApplicationForm.setOnClickListener {
            val intent = Intent(this,ExploreJobApplicatonFormActivity::class.java)
            intent.putExtra("job",JOB)
            startActivity(intent)
        }
   }

    private fun openDialogForMessage(rejectionMessage: String?) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle("Message")
        //set message for alert dialog
        builder.setMessage(rejectionMessage)
        builder.setIcon(R.drawable.ic_baseline_message_24)

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(true)
        alertDialog.show()
    }

}


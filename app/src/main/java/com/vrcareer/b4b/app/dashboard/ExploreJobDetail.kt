package com.vrcareer.b4b.app.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vrcareer.b4b.app.Constants
import com.vrcareer.b4b.databinding.ActivityExploreJobDetailBinding
import com.vrcareer.b4b.model.Job

class ExploreJobDetail : AppCompatActivity() {

//    private lateinit var appBarConfiguration: AppBarConfiguration
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
            binding?.jobDescription?.text = job_description?.replace("""\\n""","\n")
            JOB_ID?.let {
                db.reference.child(Constants.USER_JOB_APPLICATION_PATH_ROOT).child(it)
                    .child("pending").child("${auth.currentUser?.uid}")
                    .get().addOnSuccessListener {ds->

                        if (ds.exists()){
                            binding?.btnFillApplicationForm?.let { btn->
                                btn.isEnabled = false
                                btn.isClickable = false
                                btn.text = "Already Applied"
                            }
                        }
                    }
                    .addOnFailureListener {e->
                        Toast.makeText(this,"Network error ${e.message}",Toast.LENGTH_SHORT).show()
                    }
            }

        }
//        Toast.makeText(this, "$JOB_ID", Toast.LENGTH_SHORT).show()
        auth.currentUser?.uid?.let {
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

        }

        binding.btnFillApplicationForm.setOnClickListener {
            val intent = Intent(this,ExploreJobApplicatonFormActivity::class.java)
            intent.putExtra("job",JOB)
            startActivity(intent)
        }
        /*setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_explore_job_detail)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }*/
    }

   /* override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_explore_job_detail)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }*/

}
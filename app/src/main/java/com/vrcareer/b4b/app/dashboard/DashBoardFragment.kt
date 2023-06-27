package com.vrcareer.b4b.app.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vrcareer.b4b.app.Constants
import com.vrcareer.b4b.databinding.FragmentDashBoardBinding
import com.vrcareer.b4b.model.Job
import kotlinx.coroutines.awaitAll

/**
 * It is Jobs List Fragment in Explore Tab
 * It shows all available Jobs */
class DashBoardFragment : Fragment() {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var db: FirebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var binding: FragmentDashBoardBinding
    private var adapter: ExploreJobsAdapter? = null
    private val Job_List = mutableListOf<Job>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDashBoardBinding.inflate(inflater, container, false)

        fetchJobListandShowItToRV()

        return binding.root
    }

    /**
     * Fetching Jobs from firebase database and updating Recycler VIew*/
    private fun fetchJobListandShowItToRV() {
        db.reference.child(Constants.JOBS_FIREBASE_PATH_ROOT).get().addOnSuccessListener {
            if (it.exists()) {
                binding?.frameLoading?.visibility = View.GONE
                for (job in it.children) {
                    val jobDB = job.getValue(Job::class.java)
                    if (jobDB != null) {
//                        val ans = fetchJobIsAppliedOrNot(jobDB.job_id)
                        Log.d("ResultSuccess:","True")
                        Job_List.add(jobDB)
                    }
                }
            }
            adapter = context?.let { it1 ->
                ExploreJobsAdapter(it1, joblist = Job_List) { job ->
                    Log.d("MyJobClicked", "Clicked $job")
                    val intent = Intent(requireActivity(), ExploreJobDetail::class.java)
                    intent.putExtra("job", job)
                    requireActivity().startActivity(intent)
                }
            }
            binding?.rvExploreJobs?.let { rv ->
                rv.layoutManager = LinearLayoutManager(context)
                rv.adapter = adapter
            }
        }
            .addOnFailureListener {e->
                Toast.makeText(context,"Network error ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun fetchJobIsAppliedOrNot(jobId: String?): Boolean {
        var res = false
        if (jobId != null) {

            db.reference.child(Constants.USER_JOB_APPLICATION_PATH_ROOT).child(jobId)
                .child("pending").child("${auth.currentUser?.uid}")
                .get().addOnSuccessListener {

                    if (it.exists()){
                        res = true
                        Log.d("ResultSuccess:","True")
                    }
                }
        }

        return res
    }

}
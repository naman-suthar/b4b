package com.vrcareer.b4b.app.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vrcareer.b4b.app.Constants
import com.vrcareer.b4b.databinding.FragmentDashBoardBinding
import com.vrcareer.b4b.model.Job
import kotlinx.coroutines.awaitAll


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


        /*binding?.btnUploadJobs?.setOnClickListener {
            for (jobs in Job_List){
                jobs.job_id?.let { id ->
                    db.reference.child(Constants.JOBS_FIREBASE_PATH_ROOT).child(
                        id
                    ).setValue(jobs).addOnSuccessListener {
                        Toast.makeText(requireContext(),"${jobs.job_id} successfully uploaded",Toast.LENGTH_SHORT).show()
                    }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(),"${jobs.job_id} failed to upload",Toast.LENGTH_SHORT).show()

                        }
                }
            }

        }*/
        return binding.root
    }

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
                    val intent = Intent(requireActivity(), ExploreJobDetail::class.java)
                    intent.putExtra("job_id", job.job_id)
                    requireActivity().startActivity(intent)
                }
            }
            binding?.rvExploreJobs?.let { rv ->
                rv.layoutManager = LinearLayoutManager(context)
                rv.adapter = adapter
            }
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
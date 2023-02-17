package com.vrcareer.b4b.app.dashboard

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vrcareer.b4b.R
import com.vrcareer.b4b.app.Constants
import com.vrcareer.b4b.model.Job

class ExploreJobsAdapter(
    context: Context,
    var joblist: MutableList<Job>,
    val onCardClicked: (Job) -> Unit
) :
    RecyclerView.Adapter<ExploreJobsAdapter.JobItemViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_job_item_dashboard, parent, false)
        return JobItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobItemViewHolder, position: Int) {
        Log.d("RVJOBS:", "IT is called for ${joblist[position]}")
        val currJob = joblist[position]
        holder.jobTitle.text = currJob.job_title
        holder.jobTagline.text = currJob.job_tagline
        holder.jobIcon.load(currJob.job_icon)
        holder.btnView.setOnClickListener {
            onCardClicked(currJob)
        }

    }

    override fun getItemCount(): Int = joblist.size

    class JobItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val jobTitle: TextView = view.findViewById(R.id.txt_job_title)
        val jobTagline: TextView = view.findViewById(R.id.txt_tagline)
        val btnView: Button = view.findViewById(R.id.btn_view_job)
        val mcJob: MaterialCardView = view.findViewById(R.id.mc_explore_job)
        val jobIcon: ImageView = view.findViewById(R.id.job_icon)
    }

   /* fun fetchJobIsAppliedOrNot(jobId: String?): Boolean {
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
    }*/
}
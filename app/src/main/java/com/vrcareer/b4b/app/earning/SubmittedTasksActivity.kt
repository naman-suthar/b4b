package com.vrcareer.b4b.app.earning

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import coil.size.ViewSizeResolver
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vrcareer.b4b.R
import com.vrcareer.b4b.databinding.ActivitySubmittedTasksBinding
import com.vrcareer.b4b.model.SubmittedTask
import okhttp3.internal.filterList

/**
 * This is Submitted Tasks Activity
 * and we are fetching the tasks and filtering them on Tabs item clicked
 * */
class SubmittedTasksActivity : AppCompatActivity() {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var db: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val taskList = mutableListOf<SubmittedTask>()
    private var adapter: RvEarningHistoryAdapter? = null
    private var binding: ActivitySubmittedTasksBinding? = null


    init {
        db.reference.child("submitted_task").child(auth.currentUser?.uid.toString())
            .addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            taskList.clear()
                            for (snap in snapshot.children) {
                                val taskItem = snap.getValue(SubmittedTask::class.java)

                                if (taskItem != null) {
                                    taskList.add(taskItem)
                                }
                            }
                            if (taskList.isEmpty()){
                                binding?.txtNoHistory?.visibility = View.VISIBLE
                            }else
                            {
                                binding?.txtNoHistory?.visibility = View.GONE
                            }
                        }
                        else{
                            binding?.txtNoHistory?.visibility = View.VISIBLE
                        }


                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                }
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubmittedTasksBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        adapter = RvEarningHistoryAdapter(this, taskList)
        binding?.rvTabSubmissions?.let {
            it.layoutManager =
                LinearLayoutManager(this@SubmittedTasksActivity)
            it.adapter = adapter
        }


        binding?.tabSubmission?.addOnTabSelectedListener(object :
            OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        adapter?.updateaList(taskList)

                    }
                    1 -> {

                        val newList =
                            taskList.filterList { this.status == "approved" }
                        if (newList.isNotEmpty()) {
                            adapter?.updateaList(newList as MutableList<SubmittedTask>)

                        } else {
                            adapter?.updateaList(mutableListOf())

                        }
                    }
                    2 -> {
                        val newList =
                            taskList.filterList { this.status == "pending" }
                        if (newList.isNotEmpty()) {
                            adapter?.updateaList(newList as MutableList<SubmittedTask>)


                        } else {
                            adapter?.updateaList(mutableListOf())

                        }
                    }
                    3 -> {
                        val newList =
                            taskList.filterList { this.status == "rejected" }
                        if (newList.isNotEmpty()) {
                            adapter?.updateaList(newList as MutableList<SubmittedTask>)

                        } else {
                            adapter?.updateaList(mutableListOf())

                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
              /*  val newList = taskList.filterList { this.status == "approved" }
                adapter?.updateaList(newList as MutableList<SubmittedTask>)*/
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })
        /**
         * Submission -> UserId ->
         *                  submitted Task (filterred by status)
         * */
    }
}
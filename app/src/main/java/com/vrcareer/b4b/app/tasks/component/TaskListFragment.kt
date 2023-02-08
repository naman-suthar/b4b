package com.vrcareer.b4b.app.tasks.component

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vrcareer.b4b.R
import com.vrcareer.b4b.app.Constants
import com.vrcareer.b4b.app.Constants.ASSOCIATE_USER_JOBS_FIREBASE_PATH_ROOT
import com.vrcareer.b4b.app.Constants.TASKS_FIREBASE_PATH
import com.vrcareer.b4b.app.Constants.USER_FIREBASE_PATH_ROOT
import com.vrcareer.b4b.databinding.FragmentTaskListBinding
import com.vrcareer.b4b.model.*


class TaskListFragment : Fragment() {
    private var binding: FragmentTaskListBinding? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTaskListBinding.inflate(inflater, container, false)
        getApprovedJobs()
        binding?.btnExploreJob?.setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)
                .selectedItemId = R.id.tab_dashboard
        }
        /*  val adapter = ActiveTasksListAdapter(requireContext(), taskList) {
              val action = TaskListFragmentDirections.actionTaskListFragmentToTaskDetailsFragment()
              findNavController().navigate(action)
          }
          binding?.rvActiveJobs?.let {
              it.layoutManager = LinearLayoutManager(requireContext())
              it.adapter = adapter
          }*/
      /*  binding?.btnTaskDetail?.setOnClickListener {
            var earningDoc = EarningDTO(
                userid = auth.currentUser?.uid,
                balance = "2000",
                pending = "100",
                withdrawalHistory = listOf(
                    Transaction(
                        tid = "W101",
                        userid = auth.currentUser?.uid,
                        transaction_type = "withdraw",
                        amount = "250",
                        time_of_request = System.currentTimeMillis(),
                    ),
                    Transaction(
                        tid = "W102",
                        userid = auth.currentUser?.uid,
                        transaction_type = "withdraw",
                        amount = "450",
                        time_of_request = System.currentTimeMillis(),
                    )
                ),
                earningHistory = listOf(
                    Transaction(
                        tid = "C101",
                        userid = auth.currentUser?.uid,
                        transaction_type = "credit",
                        amount = "250",
                        time_of_request = System.currentTimeMillis(),
                        associated_task_id = "TSKA1"
                    ),
                    Transaction(
                        tid = "C102",
                        userid = auth.currentUser?.uid,
                        transaction_type = "credit",
                        amount = "450",
                        time_of_request = System.currentTimeMillis(),
                        associated_task_id = "TSKA2"
                    )
                ),
                submissionHistory = listOf(
                    Transaction(
                        tid = "S101",
                        userid = auth.currentUser?.uid,
                        transaction_type = "submission",
                        amount = "250",
                        time_of_request = System.currentTimeMillis(),
                        associated_task_id = "TSKA1"
                    ),
                    Transaction(
                        tid = "S102",
                        userid = auth.currentUser?.uid,
                        transaction_type = "submission",
                        amount = "450",
                        time_of_request = System.currentTimeMillis(),
                        associated_task_id = "TSKA2"
                    )
                )
            )
            db.reference.child("earnings").child(auth.currentUser?.uid.toString())
                .setValue(
                    earningDoc
                )
                .addOnSuccessListener {
                    Log.d("Tasks", "Added")
                }

        }*/
        return binding?.root
    }

    private fun getApprovedJobs() {
        db.reference.child(USER_FIREBASE_PATH_ROOT).child(auth.currentUser?.uid.toString())
            .child("approved_jobs").get()
            .addOnSuccessListener {
                if (it.exists()){
                    val jobIdList = mutableListOf<String>()
                    val taskList = mutableListOf<TaskItem>()
                    for (jobId in it.children){
                        val jobId = jobId.value
                        db.reference.child(TASKS_FIREBASE_PATH).child(jobId.toString()).get()
                            .addOnSuccessListener { ds->
                                if (ds.exists()){
                                    for (task in ds.children){
                                        val taskDB = task.getValue(TaskItem::class.java)
                                        if (taskDB != null) {
                                            taskList.add(taskDB)
                                        }
                                    }
                                    Log.d("Tasks:","$taskList")
                                    binding?.frameLoadingTask?.visibility = View.GONE
                                    binding?.frameNoActiveTasks?.visibility = View.GONE
                                    binding?.rvActiveJobs?.let { rv->
                                        rv.layoutManager = LinearLayoutManager(context)
                                        rv.adapter = context?.let { it1 ->
                                            ActiveTasksListAdapter(
                                                it1,
                                                taskList
                                            ){taskItem->
                                                val intent = Intent(
                                                    requireActivity(),
                                                    TaskDetail::class.java
                                                )
                                                intent.putExtra("task",taskItem)
                                                requireActivity().startActivity(intent)
                                            }
                                        }
                                    }
                                }

                            }

                    }


                }else{
                    binding?.frameLoadingTask?.visibility = View.GONE

                }
            }
            .addOnFailureListener {
                binding?.frameLoadingTask?.visibility = View.GONE
                Toast.makeText(requireContext(),"Error Loading content",Toast.LENGTH_SHORT).show()
            }
    }


}
package com.vrcareer.b4b.app.tasks.component

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vrcareer.b4b.R
import com.vrcareer.b4b.databinding.ActivityTaskDetailBinding
import com.vrcareer.b4b.model.Assessment
import com.vrcareer.b4b.model.TaskItem

class TaskDetail : AppCompatActivity() {
    private var binding: ActivityTaskDetailBinding? = null
    private var task: TaskItem? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        if (intent!=null){
            task = intent.getSerializableExtra("task") as TaskItem
            binding?.txtTaskSteps?.text = task?.task_steps_to_follow?.replace("""\\n""","\n")
            binding?.txtTaskGuidelines?.text = task?.task_guidelines?.replace("""\\n""","\n")
            binding?.txtTaskNote?.text = task?.task_note?.replace("""\\n""","\n")
        }
        db.reference.child("trainings").child(auth.currentUser!!.uid).child(task?.taskId!!).addValueEventListener(
            object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val currAssessment = snapshot.getValue(Assessment::class.java)
                        binding?.btnSubmitTask?.isEnabled = currAssessment != null && currAssessment.status == "approved"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            }
        )
        binding?.btnSubmitTask?.setOnClickListener {
            val intent =Intent(this,SubmitTaskActivity::class.java)
            task?.let {it->
                intent.putExtra("task",it)
            }
            startActivity(intent)
        }
        binding?.btnGetTrained?.setOnClickListener {
            val intent =Intent(this,TrainingsTaskActivity::class.java)
            task?.let {
                intent.putExtra("task",it)
            }
            startActivity(intent)
        }
    }
}
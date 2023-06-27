package com.vrcareer.b4b.app.tasks.component

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import coil.size.ViewSizeResolver
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.vrcareer.b4b.R
import com.vrcareer.b4b.databinding.ActivityTrainingsTaskBinding
import com.vrcareer.b4b.model.Assessment
import com.vrcareer.b4b.model.TaskItem
import com.vrcareer.b4b.utils.ApplicationResponse


class TrainingsTaskActivity : AppCompatActivity() {

    //    private lateinit var youtubePlayerView: YouTubePlayerView
    private var binding: ActivityTrainingsTaskBinding? = null
    private val db = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    //    lateinit var youtubePlayInit: YouTubePlayer.OnInitializedListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTrainingsTaskBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.youtubePlayerView?.let { lifecycle.addObserver(it) }


        if (intent != null){
            val task = intent.getSerializableExtra("task") as TaskItem
            binding?.txtTrainingMessage?.text = task.training_note
           binding?.youtubePlayerView?.enableAutomaticInitialization = false

            binding?.youtubePlayerView?.initialize(object : AbstractYouTubePlayerListener(){
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    val videoId = task.training_video_ID
                    if (videoId != null) {
                        youTubePlayer.loadVideo(videoId, 0F)
                    }

                }
            })
            task.task_qr_url?.let {
                Log.d("Task", "${task.task_qr_url}")
//                binding?.imgQr?.visibility = View.VISIBLE
                binding?.imgQr?.let { it1 -> Glide.with(this).load(it).into(it1) }
            }
            db.reference.child("trainings").child(auth.currentUser!!.uid).child(task?.taskId!!).addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()){
                            val currAssessment = snapshot.getValue(Assessment::class.java)
                            Log.d("TaskAssessment","$currAssessment")
                            currAssessment?.let {
                                when(currAssessment.status){
                                    ApplicationResponse.Approved.name -> {
                                        binding?.btnRejectionMessage?.visibility = View.GONE
                                        binding?.btnStartAssessment?.isEnabled = false
                                        binding?.btnStartAssessment?.text = "Approved"
                                    }
                                    ApplicationResponse.Pending.name -> {
                                        binding?.btnRejectionMessage?.visibility = View.GONE
                                        binding?.btnStartAssessment?.isEnabled = false
                                        binding?.btnStartAssessment?.text = "Pending"
                                    }
                                    ApplicationResponse.Rejected.name -> {
                                        binding?.btnRejectionMessage?.visibility = View.VISIBLE
                                        binding?.btnRejectionMessage?.setOnClickListener {
                                            openDialogForMessage(currAssessment.rejected_message)
                                        }
                                        binding?.btnStartAssessment?.isEnabled = true
                                        binding?.btnStartAssessment?.text = "Retry Assessment"
                                    }
                                }
                            }


                        }
                        else{
                            binding?.btnStartAssessment?.isEnabled = true
                            binding?.btnRejectionMessage?.visibility = View.GONE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                }
            )
            binding?.btnStartAssessment?.setOnClickListener {
                val intent = Intent(this,AssessmentActivity::class.java)
                intent.putExtra("task",task)
                startActivity(intent)
            }
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
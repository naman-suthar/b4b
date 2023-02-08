package com.vrcareer.b4b.app.tasks.component

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.vrcareer.b4b.databinding.ActivityTrainingsTaskBinding
import com.vrcareer.b4b.model.Assessment
import com.vrcareer.b4b.model.TaskItem


class TrainingsTaskActivity : AppCompatActivity() {

    //    private lateinit var youtubePlayerView: YouTubePlayerView
    private var binding: ActivityTrainingsTaskBinding? = null
    private var API_KEYS = "AIzaSyCjoZxgdElW05f3CpnCUh_-9G_oyFTacsk"
    private var VIDEO_ID = "vLe9sN-FNeA"
    private val db = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    //    lateinit var youtubePlayInit: YouTubePlayer.OnInitializedListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTrainingsTaskBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        binding?.youtubePlayerView?.let { lifecycle.addObserver(it) }

        binding?.youtubePlayerView?.addYouTubePlayerListener(object :
            AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                val videoId = "vLe9sN-FNeA"
                youTubePlayer.loadVideo(videoId, 0F)

            }
        })
        if (intent != null){
            val task = intent.getSerializableExtra("task") as TaskItem
            db.reference.child("trainings").child(auth.currentUser!!.uid).child(task?.taskId!!).addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()){
                            val currAssessment = snapshot.getValue(Assessment::class.java)
                            if (currAssessment!=null && currAssessment.status == "pending" ){
                                binding?.btnStartAssessment?.isEnabled = false
                                binding?.btnStartAssessment?.text = "Pending"
                            }else if(currAssessment!=null && currAssessment?.status =="approved"){
                                binding?.btnStartAssessment?.isEnabled = false
                                binding?.btnStartAssessment?.text = "Approved"
                            } else{
                                binding?.btnStartAssessment?.isEnabled = true
                                binding?.btnStartAssessment?.text = "Retry Assessment"
                            }


                        }
                        else{
                            binding?.btnStartAssessment?.isEnabled = true
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


}
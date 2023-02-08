package com.vrcareer.b4b.app.tasks.component

import android.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.core.view.forEachIndexed
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vrcareer.b4b.app.Constants
import com.vrcareer.b4b.app.HomeActivity
import com.vrcareer.b4b.databinding.ActivityAssessmentBinding
import com.vrcareer.b4b.model.Answer
import com.vrcareer.b4b.model.Assessment
import com.vrcareer.b4b.model.TaskItem

class AssessmentActivity : AppCompatActivity() {
    private var binding: ActivityAssessmentBinding? = null
    private var auth = FirebaseAuth.getInstance()
    private var db = FirebaseDatabase.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssessmentBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        val ans3options = listOf("Check-In", "Link QR", "Onboard Merchant", "Re-KYC")
        val ans4options = listOf("True", "False")
        val ans5options = listOf("1 Rs", "5 Rs", "10 Rs", "20 Rs")
        val ans6options = listOf("Yes", "No")
        val ans7options = listOf(
            "Airtel Ranger App",
            "Brand4Brands App",
            "Airtel Merchant App",
            "None of the Above"
        )
        val ans8options = listOf("First Name", "GST Number", "Date Of Birth", "Shop Category")
        val ans9options = listOf(
            "By re-verifying the KYC",
            "By using one of the QR Codes and using the link QR Code option on the home page to scan it",
            "By using the engaging merchant option",
            "By upgrading the merchant profile"
        )


        val ans3Adap = ArrayAdapter(this, R.layout.simple_list_item_1, ans3options)
        val ans4Adap = ArrayAdapter(this, R.layout.simple_list_item_1, ans4options)
        val ans5Adap = ArrayAdapter(this, R.layout.simple_list_item_1, ans5options)
        val ans6Adap = ArrayAdapter(this, R.layout.simple_list_item_1, ans6options)
        val ans7Adap = ArrayAdapter(this, R.layout.simple_list_item_1, ans7options)
        val ans8Adap = ArrayAdapter(this, R.layout.simple_list_item_1, ans8options)
        val ans9Adap = ArrayAdapter(this, R.layout.simple_list_item_1, ans9options)

        (binding?.etAnswerQ3?.editText as AutoCompleteTextView).setAdapter(ans3Adap)
        (binding?.etAnswerQ4?.editText as AutoCompleteTextView).setAdapter(ans4Adap)
        (binding?.etAnswerQ5?.editText as AutoCompleteTextView).setAdapter(ans5Adap)
        (binding?.etAnswerQ6?.editText as AutoCompleteTextView).setAdapter(ans6Adap)
        (binding?.etAnswerQ7?.editText as AutoCompleteTextView).setAdapter(ans7Adap)
        (binding?.etAnswerQ8?.editText as AutoCompleteTextView).setAdapter(ans8Adap)
        (binding?.etAnswerQ9?.editText as AutoCompleteTextView).setAdapter(ans9Adap)

        if (intent != null) {
            val task = intent.getSerializableExtra("task") as TaskItem
            binding?.btnSubmitTest?.setOnClickListener {
                val answerList = mutableListOf<Answer>()
                binding?.llQuestions?.forEachIndexed { i, mc ->
                    (mc as? MaterialCardView)?.let {
                        if (i == 0 || i == 1) {
                            val tvQuestion =
                                mc.findViewWithTag<TextView>("tv_question")
                            val etAns = mc.findViewWithTag<EditText>("et_ans")
                            if (etAns.text.isEmpty()) {
                                etAns.requestFocus()
                                etAns.error = "Enter valid field"
                                return@setOnClickListener

                            } else {
                                val answer = Answer(
                                    question = tvQuestion.text.toString(),
                                    answer = etAns?.text.toString()
                                )
                                answerList.add(i, answer)
                            }
                        } else {
                            val tvQuestion =
                                mc.findViewWithTag<TextView>("tv_question")
                            val etAns = mc.findViewWithTag<EditText>("et_ans")
                            if (etAns.text.toString().isEmpty()) {
                                etAns.requestFocus()
                                etAns.error = "Please Select Valid Answer"

                                return@setOnClickListener

                            } else {
                                val answer = Answer(
                                    question = tvQuestion.text.toString(),
                                    answer = etAns?.text.toString()
                                )
                                answerList.add(i, answer)
                            }
                        }

                    }


                }
                Log.d("Answer", "$answerList")
                val timeNow = System.currentTimeMillis().toString()
                val assessmentId = auth.currentUser?.uid.toString() + timeNow
                val assessment = Assessment(
                    assessment_id = task.taskId,
                    user_id = auth.currentUser?.uid,
                    task_id = task.taskId,
                    job_id = task.jobId,
                    ansList = answerList,
                    status = "pending",
                    time_of_request = System.currentTimeMillis()
                )

                db.reference.child(
                    "trainings"
                ).child(auth.currentUser?.uid.toString()).child(task.taskId!!).setValue(assessment)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Submitted Successfully", Toast.LENGTH_SHORT).show()
                       /* val intent = Intent(this, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)*/
                        finish()
                    }
            }
        }


    }
}
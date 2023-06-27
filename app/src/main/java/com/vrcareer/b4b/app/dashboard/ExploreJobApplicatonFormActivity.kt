package com.vrcareer.b4b.app.dashboard

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vrcareer.b4b.R
import com.vrcareer.b4b.app.Constants
import com.vrcareer.b4b.app.HomeActivity
import com.vrcareer.b4b.databinding.ActivityExploreJobApplicatonFormBinding
import com.vrcareer.b4b.model.*
import com.vrcareer.b4b.utils.ApplicationResponse


/**
 * This is Job's Application Form Activity
 * It has List of Questions fetching from database and Dialog for Confirmation
 */
class ExploreJobApplicatonFormActivity : AppCompatActivity() {
    private var auth = FirebaseAuth.getInstance()
    private var db = FirebaseDatabase.getInstance()

    private var binding: ActivityExploreJobApplicatonFormBinding? = null
    private var JOB: Job? = null
    private var job_id: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityExploreJobApplicatonFormBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        if (intent != null) {

            JOB = intent.getSerializableExtra("job") as Job
            job_id = JOB?.job_id

            /**
             * Generating form of available Screening Questions based on their type */
            JOB?.screeningQuestions?.forEachIndexed { i, question ->
                val view = when (question.question_type) {
                    QuestionType.Text.type -> LayoutInflater.from(this)
                        .inflate(R.layout.question_item_text, binding?.root, false)
                    QuestionType.MultiLineText.type -> LayoutInflater.from(this)
                        .inflate(R.layout.question_item_multi_text, binding?.root, false)
                    QuestionType.Boolean.type -> LayoutInflater.from(this)
                        .inflate(R.layout.question_item_boolean, binding?.root, false)
                    QuestionType.Number.type -> LayoutInflater.from(this)
                        .inflate(R.layout.question_item_number, binding?.root, false)
                    QuestionType.Dropdown.type -> LayoutInflater.from(this)
                        .inflate(R.layout.question_item_drop_down, binding?.root, false)
                    QuestionType.Ratings.type -> LayoutInflater.from(this)
                        .inflate(R.layout.question_item_ratings, binding?.root, false)
                    else -> {
                        null
                    }
                }

                val tvQuestion: TextView? = view?.findViewWithTag("tv_question")
                tvQuestion?.text = question.question_statement
                when(question.question_type){
                    QuestionType.Boolean.type -> {
                        val etAnswer: EditText? = view?.findViewWithTag("et_ans")
                        val options = listOf<String>("YES", "NO")
                        etAnswer?.hint = "Select"
                        val adapter = ArrayAdapter(
                            this@ExploreJobApplicatonFormActivity,
                            android.R.layout.simple_list_item_1, options
                        )
                        (etAnswer as? AutoCompleteTextView)?.setAdapter(adapter)
                    }
                    QuestionType.Dropdown.type -> {
                        val etAnswer: EditText? = view?.findViewWithTag("et_ans")
                        val options: List<String> = question.options ?: mutableListOf()
                        etAnswer?.hint = "Select"
                        val adapter = ArrayAdapter(
                            this@ExploreJobApplicatonFormActivity,
                            android.R.layout.simple_list_item_1, options
                        )
                        (etAnswer as? AutoCompleteTextView)?.setAdapter(adapter)
                    }

                }


                binding?.llParentQuestions?.addView(view)

            }

        }

        /**
         * On Button Submit CLicked
         * */
        binding?.btnSubmitApplication?.setOnClickListener {

            val dialog = MaterialAlertDialogBuilder(this)
            dialog.setTitle("Submit Application?")
            dialog.setMessage("Form can't be edited once submitted")
            dialog.setPositiveButton(
                "Confirm"
            ) { d, _ ->
                /**
                 * This answerList holds all the questions and answers iterating over all Material Card we generated*/
                val answerList = mutableListOf<Answer>()
                binding?.llParentQuestions?.children?.forEachIndexed { i, v ->
                    (v as ViewGroup).children.forEach { mc ->
                        val tvQuestion =
                            (mc as LinearLayout).findViewWithTag<TextView>("tv_question")
                        when(JOB?.screeningQuestions?.get(i)?.question_type){

                            QuestionType.Ratings.type ->{
                                val etAnswer = (mc as LinearLayout).findViewWithTag<RatingBar>("et_ans")
                                if (etAnswer.rating == 0f) {
                                    etAnswer.requestFocus()
                                    Toast.makeText(this, "Ratings can not be zero", Toast.LENGTH_SHORT)
                                        .show()
                                    d.dismiss()
                                    return@setPositiveButton

                                } else {
                                    val answer = Answer(
                                        question = tvQuestion.text.toString(),
                                        answer = etAnswer?.rating.toString()
                                    )
                                    answerList.add(i, answer)
                                }
                            }
                            else->{
                                val etAnswer =  (mc as LinearLayout).findViewWithTag<EditText>("et_ans")
                                if (etAnswer.text.toString().isEmpty()) {
                                    etAnswer.requestFocus()
                                    etAnswer.error = "Please Select Valid Answer"
                                    d.dismiss()
                                    return@setPositiveButton

                                } else {
                                    val answer = Answer(
                                        question = tvQuestion.text.toString(),
                                        answer = etAnswer?.text.toString()
                                    )
                                    answerList.add(i, answer)
                                }
                            }
                        }

                    }

                }


                val application = JobApplication(
                    user_id = auth.currentUser?.uid,
                    job_id = job_id,
                    ansList = answerList,
                    status = ApplicationResponse.Pending.name,
                    time_of_request = System.currentTimeMillis()
                )

                /**
                 * Sending our application to DB*/
                job_id?.let { it1 ->
                    db.reference.child(Constants.USER_JOB_APPLICATION_PATH_ROOT).child(
                        it1
                    ).child(auth.currentUser?.uid.toString()).setValue(application)
                        .addOnSuccessListener {
                            Log.d("Uploading:", "$job_id ${auth.currentUser?.uid} $answerList ")
                            Toast.makeText(this, "Uploaded Successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, HomeActivity::class.java)
                            intent.flags = FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                            d.dismiss()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Network error ${e.message}", Toast.LENGTH_SHORT)
                                .show()
                        }

                }
            }

            dialog.setNegativeButton("Cancel") { d, _ ->
                d.dismiss()
            }

            dialog.show()


        }
    }


}
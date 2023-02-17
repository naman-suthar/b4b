package com.vrcareer.b4b.app.dashboard

import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vrcareer.b4b.R
import com.vrcareer.b4b.app.Constants
import com.vrcareer.b4b.app.HomeActivity
import com.vrcareer.b4b.databinding.ActivityExploreJobApplicatonFormBinding
import com.vrcareer.b4b.model.*

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
            Log.d("Job is", "$JOB")

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
        val optionsBoolean = listOf("YES", "NO")
//        val optionDomains = listOf("Domain1", "Domain2", "Domain3", "Domain4")
//        val describeYouOption = listOf("Student","Fresher","Homemaker","Part-time working professional","Full-time working professional","Currently out of work")
        val ETYesOrNoAdap = ArrayAdapter(this, android.R.layout.simple_list_item_1, optionsBoolean)
        /*val OptionDomainsAdapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, optionDomains)*/
        /* val DescribeYouAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,describeYouOption)*/
        /* val allYesNoET = listOf(
             binding?.etAnswerQ1?.editText,
             binding?.etAnswerQ2?.editText,
             binding?.etAnswerQ3?.editText,
             binding?.etAnswerQ4?.editText,
             binding?.etAnswerQ5?.editText,
             binding?.etAnswerQ6?.editText,
             binding?.etAnswerQ7?.editText,
             binding?.etAnswerQ8?.editText,
         )
         allYesNoET.forEach {
 //            it?.setText(optionsBoolean[1])
             (it as? AutoCompleteTextView)?.setAdapter(ETYesOrNoAdap)

         }*/
        /*  (binding?.etAnswerQ10?.editText as? AutoCompleteTextView)?.setText(describeYouOption[0])
          (binding?.etAnswerQ10?.editText as? AutoCompleteTextView)?.setAdapter(DescribeYouAdapter)*/


        /*       (binding?.etAnswerQ7A?.editText as? AutoCompleteTextView)?.setText(optionDomains[0])
               (binding?.etAnswerQ7A?.editText as? AutoCompleteTextView)?.setAdapter(OptionDomainsAdapter)*/
        /*binding?.etAnswerQ7?.editText?.doOnTextChanged { inputText, _, _, _ ->
            if (inputText.toString() == optionsBoolean[0]) {
                binding?.root?.findViewWithTag<LinearLayout>("sub_question")?.visibility =
                    View.VISIBLE
            } else {
                binding?.root?.findViewWithTag<LinearLayout>("sub_question")?.visibility = View.GONE
            }

        }*/


        /*val question1 = Question("001", "What is First Question", "string")
        val qusetionList = listOf<Question>(
            question1,
            question1.copy(question_statement = "What is Second Question"),
            question1.copy(
                question_statement = "What is Third Question",
                question_type = "number"
            ),
            question1.copy(question_statement = "What is Fourth Question"),
            question1.copy(question_statement = "What is Second Question"),
            question1.copy(
                question_statement = "What is Third Question",
                question_type = "number"
            ),
            question1.copy(question_statement = "What is Fourth Question")
        )

        qusetionList.forEach {
            createCardView(it)
        }*/
        binding?.btnSubmitApplication?.setOnClickListener {
            val dialog = MaterialAlertDialogBuilder(this)
            dialog.setTitle("Submit Application?")
            dialog.setMessage("Form can't be edited once submitted")
            dialog.setPositiveButton(
                "Confirm"
            ) { d, _ ->
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
                Log.d("Answers","$answerList")
                val application = JobApplication(
                    user_id = auth.currentUser?.uid,
                    job_id = job_id,
                    ansList = answerList,
                    status = "pending",
                    time_of_request = System.currentTimeMillis()
                )
                job_id?.let { it1 ->
                    db.reference.child(Constants.USER_JOB_APPLICATION_PATH_ROOT).child(
                        "$it1/pending"
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


            /*  val ansList = mutableListOf<String>()
          binding?.llParentQuestions?.forEachIndexed{ i,mc->
              val ansET = mc.findViewWithTag<EditText>("q")
              val ans = ansET.text.toString()
              Log.d("Ans:","$i $ans" )
             if(ans.isEmpty()){
                 ansET.requestFocus()
                 ansET.error = "Field Can't be Empty"
                 return@setOnClickListener
             }else{
                 ansList.add(i,ans)
             }
          }
          val application = JobApplication(
              user_id = auth.currentUser?.uid,
              job_id=job_id,
              ansList = ansList,
              status = "pending",
              time_of_request = System.currentTimeMillis()
          )
          job_id?.let { it1 ->
              db.reference.child(Constants.USER_JOB_APPLICATION_PATH_ROOT).child(
                  "$it1/pending"
              ).child(auth.currentUser?.uid.toString()).setValue(application).addOnSuccessListener {
                  Log.d("Uploading:","$job_id ${auth.currentUser?.uid} $ansList " )
                  Toast.makeText(this,"Uploaded Successfully",Toast.LENGTH_SHORT).show()
                  val intent = Intent(this,HomeActivity::class.java)
                  intent.flags = FLAG_ACTIVITY_CLEAR_TOP
                  startActivity(intent)
                  finish()
              }
          }*/

        }
    }


    private fun createCardView(questionStr: Question) {
        val cardView: MaterialCardView = MaterialCardView(

            this,
            null,
            com.google.android.material.R.style.Widget_Material3_CardView_Filled

        )
        val params = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(16, 16, 16, 16)

        cardView.layoutParams = params
        cardView.radius = 10f
        val cardLL = LinearLayout(this)
        cardLL.orientation = LinearLayout.VERTICAL
        val paramsLL = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
        paramsLL.setMargins(16, 16, 16, 16)
        cardLL.layoutParams = paramsLL
        val question = TextView(this)
        question.text = questionStr.question_statement

        val editText = EditText(this)
        editText.hint = "Enter something"
        editText.tag = "q"
        editText.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        editText.setPadding(20, 20, 20, 20)
        editText.setTextColor(Color.BLACK)
        editText.textSize = 16f
        if (questionStr.question_type == "number") {
            editText.inputType = InputType.TYPE_CLASS_NUMBER
        } else editText.inputType = InputType.TYPE_CLASS_TEXT
        cardLL.addView(question)
        cardLL.addView(editText)
        cardView.addView(cardLL)
        binding?.llParentQuestions?.addView(cardView)

    }


}
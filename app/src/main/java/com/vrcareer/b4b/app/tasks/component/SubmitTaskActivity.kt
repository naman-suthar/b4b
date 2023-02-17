package com.vrcareer.b4b.app.tasks.component

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.net.toUri
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.Transaction
import com.google.firebase.storage.FirebaseStorage
import com.vrcareer.b4b.R
import com.vrcareer.b4b.databinding.ActivitySubmitTaskBinding
import com.vrcareer.b4b.model.*

class SubmitTaskActivity : AppCompatActivity() {
    private var binding: ActivitySubmitTaskBinding? = null

    /* private val IMAGE_PICK_CODE = 1000
     private val MAX_IMAGE_SELECTION = 2*/
    private val imageList: MutableList<String> = mutableListOf()

    //    private var adapter: ImageSelectedInSubmitTaskAdapter? = null
    private val db = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var taskIntent: TaskItem? = null
    private val storage = FirebaseStorage.getInstance()
    private var storageReference = storage.reference
    private var indexer: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val progressDialog = ProgressDialog(this)
        binding = ActivitySubmitTaskBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        if (intent != null) {
            taskIntent = intent.getSerializableExtra("task") as TaskItem
            taskIntent?.screeningQuestions?.forEachIndexed { i, question ->
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
                    QuestionType.Photo.type -> LayoutInflater.from(this)
                        .inflate(R.layout.question_type_photo, binding?.root, false)
                    else -> {
                        null
                    }
                }

                val tvQuestion: TextView? = view?.findViewWithTag("tv_question")
                tvQuestion?.text = question.question_statement
                when (question.question_type) {
                    QuestionType.Boolean.type -> {
                        val etAnswer: EditText? = view?.findViewWithTag("et_ans")
                        val options = listOf<String>("YES", "NO")
                        etAnswer?.hint = "Select"
                        val adapter = ArrayAdapter(
                            this@SubmitTaskActivity,
                            android.R.layout.simple_list_item_1, options
                        )
                        (etAnswer as? AutoCompleteTextView)?.setAdapter(adapter)
                    }
                    QuestionType.Dropdown.type -> {
                        val etAnswer: EditText? = view?.findViewWithTag("et_ans")
                        val options: List<String> = question.options ?: mutableListOf()
                        etAnswer?.hint = "Select"
                        val adapter = ArrayAdapter(
                            this@SubmitTaskActivity,
                            android.R.layout.simple_list_item_1, options
                        )
                        (etAnswer as? AutoCompleteTextView)?.setAdapter(adapter)
                    }
                    QuestionType.Photo.type -> {
                        val myIndexer = indexer
                        val imagePreview: ImageView? = view?.findViewWithTag("preview_image")
                        val btnSelectPhoto: Button? = view?.findViewWithTag("btn_add_photo")

                        val galleryImage = registerForActivityResult(
                            ActivityResultContracts.GetContent(),
                            ActivityResultCallback {
                                if (myIndexer > imageList.size) {
                                    Toast.makeText(this,"Please Select Previous Image first",Toast.LENGTH_SHORT).show()
                                    return@ActivityResultCallback
                                }
                                imagePreview?.setImageURI(it)
//                imgUri = it

                                auth.currentUser?.uid?.let { uid ->
                                    it?.let { uri ->
                                        val progressBar = ProgressDialog(this)
                                        progressBar?.setTitle("Uploading")
                                        progressBar?.setCancelable(false)
                                        progressBar?.show()
                                        val timeOfNow = System.currentTimeMillis()
                                        val uniqueId = "${uid}${timeOfNow}"

                                        storageReference.child("Images/TasksProof").child(
                                            uid
                                        ).child(taskIntent!!.taskId!!).child(timeOfNow.toString())
                                            .child(tvQuestion?.text.toString())
                                            .putFile(uri).addOnSuccessListener { task ->
                                                task.metadata!!.reference!!.downloadUrl.addOnSuccessListener { urifs ->

                                                    if (myIndexer == imageList.size) {
                                                        imageList.add(myIndexer, urifs.toString())
                                                    } else {
                                                        imageList[myIndexer] = urifs.toString()
                                                    }

                                                    Log.d("Indexer", " $myIndexer $imageList")
                                                    progressBar.dismiss()
                                                }
                                                    .addOnFailureListener { e ->
                                                        progressBar.dismiss()
                                                        Toast.makeText(
                                                            this,
                                                            "Image Url error ${e.message}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                            }
                                            .addOnFailureListener { e ->
                                                progressBar.dismiss()
                                                Toast.makeText(
                                                    this,
                                                    "Storage Network error ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                }

                            }
                        )

                        btnSelectPhoto?.setOnClickListener {
                            galleryImage.launch("image/*")
                        }
                        indexer += 1
                    }

                }


                binding?.llQuestionare?.addView(view)

            }
        }
        /*  val galleryImage = registerForActivityResult(
              ActivityResultContracts.GetContent(),
              ActivityResultCallback {

  //                binding?.imgTransactionProofSubmitTask?.setImageURI(it)
  //                imgUri = it

                  auth.currentUser?.uid?.let { uid ->
                      it?.let { uri ->
                          val progressBar = ProgressDialog(this)
                          progressBar?.setTitle("Uploading")
                          progressBar?.show()
                          val timeOfNow = System.currentTimeMillis()
                          val uniqueId = "${uid}${timeOfNow}"
                          storageReference.child("Images/TasksProof").child(
                              uid
                          ).child(taskIntent!!.taskId!!).child(timeOfNow.toString()).child("0")
                              .putFile(uri).addOnSuccessListener { task ->
                              task.metadata!!.reference!!.downloadUrl.addOnSuccessListener { urifs ->
                                  imageList.add(0, urifs.toString())
                                  progressBar.dismiss()
                              }
                                  .addOnFailureListener { e ->
                                      progressBar.dismiss()
                                      Toast.makeText(
                                          this,
                                          "Image Url error ${e.message}",
                                          Toast.LENGTH_SHORT
                                      ).show()
                                  }
                          }
                              .addOnFailureListener { e ->
                                  progressBar.dismiss()
                                  Toast.makeText(
                                      this,
                                      "Storage Network error ${e.message}",
                                      Toast.LENGTH_SHORT
                                  ).show()
                              }
                      }
                  }

              }
          )
          val galleryImage2 = registerForActivityResult(
              ActivityResultContracts.GetContent(),
              ActivityResultCallback {

  //                binding?.imgQrCodeSubmitTask?.setImageURI(it)
  //                imgUri = it

                  auth.currentUser?.uid?.let { uid ->
                      it?.let { uri ->
                          val progressBar = ProgressDialog(this)
                          progressBar?.setTitle("Uploading")
                          progressBar?.show()
                          val timeOfNow = System.currentTimeMillis()
                          val uniqueId = "${uid}${timeOfNow}"
                          storageReference.child("Images/TasksProof").child(
                              uid
                          ).child(taskIntent!!.taskId!!).child(timeOfNow.toString()).child("1")
                              .putFile(uri).addOnSuccessListener { task ->
                              task.metadata!!.reference!!.downloadUrl.addOnSuccessListener { urifs ->
                                  imageList.add(1, urifs.toString())
                                  progressBar.dismiss()
                              }
                                  .addOnFailureListener { e ->
                                      progressBar.dismiss()
                                      Toast.makeText(
                                          this,
                                          "Image Url error ${e.message}",
                                          Toast.LENGTH_SHORT
                                      ).show()
                                  }
                          }
                              .addOnFailureListener { e ->
                                  progressBar.dismiss()
                                  Toast.makeText(
                                      this,
                                      "Storage Network error ${e.message}",
                                      Toast.LENGTH_SHORT
                                  ).show()
                              }
                      }
                  }

              }
          )*/
//        binding?.btnAddPhotoTransaction?.setOnClickListener {
//            galleryImage.launch("image/*")
//        }
//        binding?.btnAddPhotoQr?.setOnClickListener {
//            galleryImage2.launch("image/*")
//        }
        binding?.btnSubmitTask?.setOnClickListener {
            val answerList = mutableListOf<Answer>()
            var incrementor = 0
            binding?.llQuestionare?.children?.forEachIndexed { i, lc ->
                (lc as? MaterialCardView)?.let { qc ->
                    val question = qc.findViewWithTag<TextView>("tv_question").text.toString()
                    if (taskIntent?.screeningQuestions?.get(i)?.question_type != QuestionType.Photo.type) {
                        val answerET = qc.findViewWithTag<EditText>("et_ans")
                        if (answerET.text.toString().isEmpty()) {
                            answerET.requestFocus()
                            answerET.error = "Please Submit answer"
                            return@setOnClickListener
                        } else {
                            val answer =
                                Answer(question = question, answer = answerET.text.toString())
                            answerList.add(answer)
                        }
                    } else {
                        val answerET = imageList[incrementor]

                        val answer = Answer(question = question, answer = answerET)
                        answerList.add(answer)
                        incrementor += 1

                    }

                }
            }
            Log.d("Answers", "$answerList")
            progressDialog.setTitle("Uploading..")
            progressDialog.show()

            if (imageList.isNotEmpty()) {
                val timeOfNow = System.currentTimeMillis()

                auth.currentUser?.uid?.let { uid ->
                    val uniqueId = "${uid}${timeOfNow}"
                    val uid = auth.currentUser!!.uid
                    val newTask = SubmittedTask(
                        uniqueId = uniqueId,
                        uid = uid,
                        taskId = taskIntent!!.taskId,
                        jobId = taskIntent!!.jobId,
                        associated_amount = taskIntent!!.task_earning_price,
                        imageList = imageList,
                        time_of_submission = timeOfNow,
                        status = "pending",
                        message = null,
                        answerList = answerList,
                        client_detail = answerList[0].answer
                    )

                    db.reference.child("submitted_task").child(uid)
                        .child(uniqueId).setValue(newTask)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Submitted Task", Toast.LENGTH_SHORT).show()

                            val myRef = db.getReference("earnings/$uid")
                            var finish = false
                            myRef.runTransaction(object : Transaction.Handler {
                                override fun doTransaction(mutableData: MutableData): Transaction.Result {

                                    mutableData?.value?.let {
                                        val currentEarning = mutableData.getValue(
                                            EarningDTO::class.java
                                        )
                                        val newEarning = currentEarning?.copy(
                                            total_pending = currentEarning.total_pending?.plus(
                                                taskIntent?.task_earning_price!!
                                            )
                                        )
                                        Log.d(
                                            "mutableData:",
                                            "$mutableData \n CE $currentEarning \n NE: $newEarning"
                                        )
                                        mutableData?.value = newEarning
                                        finish = true
                                    }
                                    return Transaction.success(mutableData)

                                }

                                override fun onComplete(
                                    databaseError: DatabaseError?,
                                    committed: Boolean,
                                    currentData: DataSnapshot?
                                ) {
                                    if (databaseError != null) {
                                        Log.d("Firebase", "Transaction failed")
                                    } else {
                                        if (finish) {
                                            progressDialog.dismiss()
                                            finish()
                                        }

                                        Toast.makeText(
                                            this@SubmitTaskActivity,
                                            "Approved Earning",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            })

                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Network error ${e.message}", Toast.LENGTH_SHORT)
                                .show()
                        }

                }


            } else {
                progressDialog.dismiss()
                Toast.makeText(this, "Please Select atleast 1 photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

}


/*

class ImageSelectedInSubmitTaskAdapter(
    context: Context,
    var imageList: MutableList<String>,
    val onImageDelete: (Int) -> Unit
) : RecyclerView.Adapter<ImageSelectedInSubmitTaskAdapter.ImageSelectedViewHolder>() {

    fun updateListInAdapter(list: MutableList<String>) {
        Log.d("Called", "It is called $list")
        imageList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageSelectedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_selected_image_item_submiting_task, parent, false)
        return ImageSelectedViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageSelectedViewHolder, position: Int) {
        val currImage = imageList[position]
        val uri = currImage.toUri()
        holder.image.load(uri)
        holder.btnDelete.setOnClickListener {
            onImageDelete(position)
        }
    }

    override fun getItemCount(): Int = imageList.size

    class ImageSelectedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.img_selected_image_in_rv)
        val btnDelete: Button = view.findViewById(R.id.btn_delete_selected_image_in_rv)
    }
}
*/


/*adapter = ImageSelectedInSubmitTaskAdapter(this, imageList, onImageDelete = { index ->
          updateRv(index)
      })*/
/*binding?.rvImagesSelected?.let {
    it.layoutManager = LinearLayoutManager(this)
    it.adapter = adapter
}
binding?.btnAddPhotoProof?.setOnClickListener {
    pickImageFromGallery()
}*/

/* private fun updateRv(index: Int) {
     if (imageList.isNotEmpty()) {
         imageList.removeAt(index)
         Log.d("Called", "Update Rv $adapter")

         adapter?.updateListInAdapter(imageList)
     }
 }*/

/* fun pickImageFromGallery() {
     val intent = Intent(Intent.ACTION_PICK)
     intent.type = "image/*"
     intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
     intent.action = Intent.ACTION_GET_CONTENT
     startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_PICK_CODE)
 }*/

/* override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
     super.onActivityResult(requestCode, resultCode, data)
     if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
         if (data?.clipData != null) {
             val count = data.clipData!!.itemCount
             if (count > MAX_IMAGE_SELECTION) {
                 Toast.makeText(this, "You can select only 2 images", Toast.LENGTH_SHORT).show()
             } else {
                 imageList.clear()
                 for (i in 0 until count) {
                     val imageUri = data.clipData!!.getItemAt(i).uri
                     // do something with the image

                     imageList.add(imageUri.toString())
                 }
                 Log.d("Images", "$imageList")
                 if (imageList != null) {
                     adapter = ImageSelectedInSubmitTaskAdapter(
                         this,
                         imageList,
                         onImageDelete = { index ->
                             updateRv(index)
                         })
                     binding?.rvImagesSelected?.let {
                         it.layoutManager = LinearLayoutManager(this)
                         it.adapter = adapter
                     }
                 }

             }
         } else if (data?.data != null) {
             // handle single image
             Log.d("Image", "SingleImage ${data?.data}")
             imageList?.clear()
             data?.data?.toString()?.let { imageList?.add(0, it) }
             if (imageList != null) {
                 adapter =
                     ImageSelectedInSubmitTaskAdapter(this, imageList, onImageDelete = { index ->
                         updateRv(index)
                     })
                 binding?.rvImagesSelected?.let {
                     it.layoutManager = LinearLayoutManager(this)
                     it.adapter = adapter
                 }
             }
         }
     }
 }*/
 */
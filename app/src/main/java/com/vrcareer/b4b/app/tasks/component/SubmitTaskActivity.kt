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
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val progressDialog = ProgressDialog(this)
        binding = ActivitySubmitTaskBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        if (intent != null) {
            taskIntent = intent.getSerializableExtra("task") as TaskItem
        }
        val galleryImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {

                binding?.imgTransactionProofSubmitTask?.setImageURI(it)
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

                binding?.imgQrCodeSubmitTask?.setImageURI(it)
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
        )
        binding?.btnAddPhotoTransaction?.setOnClickListener {
            galleryImage.launch("image/*")
        }
        binding?.btnAddPhotoQr?.setOnClickListener {
            galleryImage2.launch("image/*")
        }
        binding?.btnSubmitTask?.setOnClickListener {
            val answerList = mutableListOf<Answer>()
            binding?.llQuestionare?.children?.forEach { lc ->
                (lc as? MaterialCardView)?.let { qc ->
                    val question = qc.findViewWithTag<TextView>("tv_question").text.toString()
                    val answerET = qc.findViewWithTag<EditText>("et_ans")
                    if (answerET.text.toString().isEmpty()) {
                        answerET.requestFocus()
                        answerET.error = "Please Submit answer"
                        return@setOnClickListener
                    } else {
                        val answer = Answer(question = question, answer = answerET.text.toString())
                        answerList.add(answer)
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
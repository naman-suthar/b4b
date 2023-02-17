package com.vrcareer.b4b.app.earning

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.Transaction
import com.vrcareer.b4b.MainActivity
import com.vrcareer.b4b.MyApplication
import com.vrcareer.b4b.R
import com.vrcareer.b4b.app.Constants
import com.vrcareer.b4b.databinding.FragmentEarningBinding
import com.vrcareer.b4b.model.*
import java.text.SimpleDateFormat
import java.util.*

class EarningFragment : Fragment() {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var db: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var availableToWithdraw: Int? = null
    private var binding: FragmentEarningBinding? = null
    private val taskList = mutableListOf<SubmittedTask>()
    private var adapter: RvEarningHistoryAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = FragmentEarningBinding.inflate(inflater, container, false)
        adapter = RvEarningHistoryAdapter(requireContext(),taskList)
        binding?.rvEarningHistory?.let {
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = adapter
        }

        getEarningData()
        getEarningHistory()
        db.reference.child("withdraw_request").child(auth.currentUser!!.uid).addValueEventListener(
            object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding?.btnWithdraw?.isEnabled = !snapshot.exists()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            }
        )

        binding?.btnWithdraw?.setOnClickListener {
            displayWithdrawDialog()
        }
        binding?.btnSubmissions?.setOnClickListener {
            val intent = Intent(requireActivity(),SubmittedTasksActivity::class.java)
            requireActivity().startActivity(intent)
        }
        return binding?.root
    }

    private fun getEarningHistory() {
        db.reference.child("submitted_task").child(auth.currentUser?.uid.toString()).addValueEventListener(
            object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        taskList.clear()
                      for (snap in snapshot.children){
                          val taskItem = snap.getValue(SubmittedTask::class.java)

                          if (taskItem != null && taskItem.status == "approved") {
                              taskList.add(taskItem)
                          }
                      }
                        adapter?.updateaList(taskList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            }
        )
    }

    private fun getEarningData() {
        db.reference.child("earnings").child(auth.currentUser?.uid.toString()).addValueEventListener(
            object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        binding?.frameLoading?.visibility = View.GONE
                        val earningDetails = snapshot.getValue(EarningDTO::class.java)
                        Log.d("Earning", "$earningDetails")
                        binding?.let { b->
                            b.tvApprovedAmount.text = "\u20B9${earningDetails?.balance.toString()}"
                            b.tvPendingAmount.text = "₹${earningDetails?.total_pending.toString()}"
                            availableToWithdraw = earningDetails?.balance?.toInt()
                        }
                    }else{
                        Toast.makeText(
                            context,
                            "Hello Fresher! Complete some task to get delightful earning here",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding?.frameLoading?.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "${error.message}", Toast.LENGTH_SHORT).show()
                    binding?.frameLoading?.visibility = View.GONE
                }
            }
        )
           /* .addOnSuccessListener {
                if (it.exists()) {
                    binding?.frameLoading?.visibility = View.GONE
                    val earningDetails = it.getValue(EarningDTO::class.java)
                    Log.d("Earning", "$earningDetails")
                    binding?.let { b ->
                        b.tvApprovedAmount.text = "\u20B9${earningDetails?.balance.toString()}"
                        b.tvPendingAmount.text = "₹${earningDetails?.total_pending.toString()}"
                        availableToWithdraw = earningDetails?.balance?.toInt()
                       *//* b.rvEarningHistory.let {rv->
                            rv.layoutManager = LinearLayoutManager(context)
                            if (earningDetails != null) {
                                rv.adapter = context?.let { it1 -> RvEarningHistoryAdapter(it1,
                                    earningDetails.earningHistory as MutableList<Transaction>
                                ) }
                            }
                        }*//*
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Hello Fresher! Complete some task to get delightful earning here",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding?.frameLoading?.visibility = View.GONE
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Network Error occurred", Toast.LENGTH_SHORT).show()
                binding?.frameLoading?.visibility = View.GONE
            }*/
    }

    private fun displayWithdrawDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
        dialog.setTitle("Withdraw Amount")
        val view =
            LayoutInflater.from(requireContext()).inflate(R.layout.withdraw_dialog_box, null, false)
        dialog.setView(view)
        val etAmount: EditText = view.findViewById(R.id.et_withdraw_amount)
        dialog.setCancelable(false)
        dialog.setPositiveButton("Withdraw", null)
            .setNegativeButton("Cancel") { d, _ ->
                d.dismiss()
            }

        val alertDialog = dialog.create()
        alertDialog.show()
        val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            val withdrawAmountInputText = etAmount.text.toString().trim()
            if (withdrawAmountInputText.isNotEmpty()) {
                //Do something
                if (withdrawAmountInputText.toInt() < 500) {

                    etAmount.requestFocus()
                    etAmount.error = "Minimum amount is \u20B9500"
                } else if (availableToWithdraw != null && withdrawAmountInputText.toInt() > availableToWithdraw!!) {
                    etAmount.requestFocus()
                    etAmount.error = "Maximum amount is \u20B9$availableToWithdraw"
                } else {
                    sendWithdrawRequest(withdrawAmountInputText.toInt())

                    alertDialog.dismiss()
                }
            } else {
                //Prompt error
                etAmount.requestFocus()
                etAmount.error = "Please enter valid amount"
            }
        }
    }

    private fun sendWithdrawRequest(amount: Int) {
        val request = WithdrawalRequest(
            id = "${auth.currentUser?.uid}${System.currentTimeMillis()}",
            user_id = auth.currentUser?.uid,
            amount = amount,
            time_of_request = System.currentTimeMillis(),
            status = "pending",
            userName = (context?.applicationContext as MyApplication).userUniv?.name
        )
        db.reference.child("withdraw_request")
            .child("${auth.currentUser?.uid}")
            .setValue(request).addOnSuccessListener {
                db.reference.child("earnings").child(auth.currentUser!!.uid).runTransaction(
                    object : com.google.firebase.database.Transaction.Handler{
                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                            currentData.value?.let {
                                val currentEarning = currentData.getValue(EarningDTO::class.java)
                                val newEarning = currentEarning?.copy(
                                    pending_withdrawal = currentEarning.pending_withdrawal?.plus(amount)
                                )
                                currentData?.value = newEarning

                            }

                            return Transaction.success(currentData)
                        }

                        override fun onComplete(
                            error: DatabaseError?,
                            committed: Boolean,
                            currentData: DataSnapshot?
                        ) {

                        }
                    }
                )
                Toast.makeText(requireContext(), "Withdrawal request Sent", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Network Error Occurred.. Please try after some time",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


}

class RvEarningHistoryAdapter(val context: Context, private var earningHistoryList: MutableList<SubmittedTask>) :
    RecyclerView.Adapter<RvEarningHistoryAdapter.EarningListItemViewHolder>() {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var db: FirebaseDatabase = FirebaseDatabase.getInstance()

    fun updateaList(list: MutableList<SubmittedTask>){
        earningHistoryList = list
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EarningListItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_earning_history_item, parent, false)
        return EarningListItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: EarningListItemViewHolder, position: Int) {
        val currEarningTransaction = earningHistoryList[position]
        val taskId = currEarningTransaction.taskId
        val jobId = currEarningTransaction.jobId
        if (currEarningTransaction.status == "rejected"){
            holder.btnMessage.visibility = View.VISIBLE
            holder.tvEarningPrice.visibility = View.GONE
        }else{
            holder.btnMessage.visibility = View.GONE
            holder.tvEarningPrice.visibility = View.VISIBLE
        }
        getAssociatedTask(holder, taskId, jobId)
//        val dateTimeStr = currEarningTransaction.time_of_submission?.let { convertLongToTime(it) }
        holder.tvEarningTime.text = currEarningTransaction.client_detail


        holder.btnMessage.setOnClickListener {
            val dialog = MaterialAlertDialogBuilder(context)
            dialog.setTitle("Rejection Message")
            dialog.setMessage(currEarningTransaction.message)
            dialog.show()
        }
    }

    private fun getAssociatedTask(
        holder: EarningListItemViewHolder,
        taskId: String?,
        jobId: String?
    ) {
        if (taskId != null) {
            if (jobId != null) {
                db.reference.child(Constants.TASKS_FIREBASE_PATH).child(jobId).child(taskId).get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            val task = it.getValue(TaskItem::class.java)
                            holder.tvTaskTitle.text = task?.task_title
                            holder.tvEarningPrice.text = "\u20B9${task?.task_earning_price.toString()}"
                            holder.frameLoading.visibility = View.GONE
                        }
                    }.addOnFailureListener {
                        holder.frameLoading.visibility = View.GONE

                    }
            }
        }

    }


    override fun getItemCount(): Int = earningHistoryList.size

    class EarningListItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTaskTitle: TextView = view.findViewById(R.id.tv_earning_task_title)
        val tvEarningTime: TextView = view.findViewById(R.id.tv_earning_time)
        val tvEarningPrice: TextView = view.findViewById(R.id.tv_earn_from_task_history)
        val frameLoading: FrameLayout = view.findViewById(R.id.frame_item_loading)
        val btnMessage: Button = view.findViewById(R.id.btn_see_message)
    }

}

fun convertLongToTime(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("yyyy/MM/dd HH:mm")
    return format.format(date)
}
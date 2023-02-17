package com.vrcareer.b4b.app.profile

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vrcareer.b4b.R
import com.vrcareer.b4b.app.earning.convertLongToTime
import com.vrcareer.b4b.databinding.FragmentWithdrawHistoryBinding
import com.vrcareer.b4b.model.WithdrawalRequest

class WithdrawHistoryFragment : Fragment() {

    private var binding: FragmentWithdrawHistoryBinding? = null
    private val db = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentWithdrawHistoryBinding.inflate(inflater,container,false)

        db.reference.child("earnings").child(auth.currentUser!!.uid).child("withdrawalHistory")
            .addValueEventListener(
                object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()){
                            val historyList = mutableListOf<WithdrawalRequest>()
                            for (snap in snapshot.children){
                                val historyItem = snap.getValue(WithdrawalRequest::class.java)
                                Log.d("History:","$historyItem")
                                if (historyItem != null) {
                                    historyList.add(historyItem)
                                }
                            }
                            val adapter = RvWithdrawHistoryAdapter(historyList)
                            binding?.rvWithdrawalHistory?.let { rv->
                                rv.layoutManager = LinearLayoutManager(requireContext())
                                rv.adapter = adapter
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                }
            )


        return binding?.root
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(
            true // default to enabled
        ) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,  // LifecycleOwner
            callback
        )
    }
}

class RvWithdrawHistoryAdapter(val withdrawList: List<WithdrawalRequest>)
    :RecyclerView.Adapter<RvWithdrawHistoryAdapter.RvWithdrawalViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvWithdrawalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.single_withdraw_history,parent,false)
        return RvWithdrawalViewHolder(view)
    }

    override fun onBindViewHolder(holder: RvWithdrawalViewHolder, position: Int) {
        val currWithdraw = withdrawList[position]
        holder.withdrawAmount.text = "\u20b9${currWithdraw.amount}"
        holder.withdrawTime.text = currWithdraw.time_of_request?.let { convertLongToTime(it) }
    }

    override fun getItemCount(): Int = withdrawList.size

    class RvWithdrawalViewHolder(view: View): RecyclerView.ViewHolder(view){
            val withdrawAmount: TextView = view.findViewById(R.id.tv_withdraw_amount_rv)
            val withdrawTime: TextView = view.findViewById(R.id.tv_withdraw_time_rv)
        }
}
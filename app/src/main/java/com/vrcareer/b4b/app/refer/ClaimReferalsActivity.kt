package com.vrcareer.b4b.app.refer

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.vrcareer.b4b.R
import com.vrcareer.b4b.app.NetworkUserItem
import com.vrcareer.b4b.databinding.ActivityClaimReferalsBinding
import com.vrcareer.b4b.model.EarningDTO
import com.vrcareer.b4b.model.User

class ClaimReferalsActivity : AppCompatActivity() {
    private var binding: ActivityClaimReferalsBinding? = null
    private val db = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val networkList:MutableList<NetworkUserItem> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClaimReferalsBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        db.reference.child("network").child(auth.currentUser!!.uid).addValueEventListener(
            object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        networkList.clear()
                       for (snap in snapshot.children){
                           val network = snap.getValue(NetworkUserItem::class.java)
                           Log.d("Snapshot:","$network")
                           if (network != null) {
                               networkList.add(network)
                           }
                       }
                        val adapter = NetworkRvAdapter(this@ClaimReferalsActivity,networkList){thisUser->
                            var finish1 = false
                            db.reference.child("network").child(thisUser?.reffered_by!!).child(thisUser.id!!)
                                .runTransaction(
                                    object :Transaction.Handler{
                                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                                            currentData.value?.let { cd->
                                                val currentNetwrk = currentData.getValue(NetworkUserItem::class.java)
                                                if (currentNetwrk?.status == "pro"){
                                                    val newNetworkItem = currentNetwrk?.copy(
                                                        status = "claimed"
                                                    )
                                                    currentData.value = newNetworkItem
                                                    finish1 = true
                                                }

                                            }
                                            return Transaction.success(currentData)
                                        }

                                        override fun onComplete(
                                            error: DatabaseError?,
                                            committed: Boolean,
                                            currentData: DataSnapshot?
                                        ) {
                                            if (finish1){
                                                Log.d("Finish","Claimed")
                                            }
                                        }
                                    }
                                )
                            var finish = false
                            db.reference.child("earnings").child(auth.currentUser!!.uid)
                                .runTransaction(
                                    object : Transaction.Handler{
                                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                                            currentData.value?.let {
                                                val earnings = currentData.getValue(EarningDTO::class.java)
                                                val newEarnings = earnings?.copy(
                                                    balance = earnings.balance?.plus(100),
                                                    total_earning = earnings.total_earning?.plus(100)
                                                )
                                                currentData.value = newEarnings
                                                finish = true
                                            }
                                            return Transaction.success(currentData)
                                        }

                                        override fun onComplete(
                                            error: DatabaseError?,
                                            committed: Boolean,
                                            currentData: DataSnapshot?
                                        ) {
                                            if (finish){

                                            }
                                        }
                                    }
                                )
                        }
                        binding?.rvMyNetwork?.let {
                            it.layoutManager = LinearLayoutManager(this@ClaimReferalsActivity)
                            it.adapter = adapter
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            }
        )


    }
}

class NetworkRvAdapter(context: Context, val networkList: List<NetworkUserItem>, val onClaimClicked: (User)->Unit) :
    RecyclerView.Adapter<NetworkRvAdapter.NetworkItemViewHolder>() {
    private val db = FirebaseDatabase.getInstance()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NetworkItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.single_network_item, parent, false)
        return NetworkItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: NetworkItemViewHolder, position: Int) {
        val currNetworkId = networkList[position]

        currNetworkId.id?.let {
            db.reference.child("users").child(it).get().addOnSuccessListener {snapshot->
                    if (snapshot.exists()){
                        val thisUser = snapshot.getValue(User::class.java)
                        holder.networkName.text = thisUser?.name
                        holder.indexTv.text = "${position+1}"
                        holder.frameloading.visibility = View.GONE
                        db.reference.child("earnings").child(it).get().addOnSuccessListener { snapshot->
                            if (snapshot.exists()){
                                val earning = snapshot.getValue(EarningDTO::class.java)
                                if (earning?.total_earning!! >=500){
                                    var finish = false
                                    db.reference.child("network").child(thisUser?.reffered_by!!).child(thisUser.id!!)
                                        .runTransaction(
                                            object :Transaction.Handler{
                                                override fun doTransaction(currentData: MutableData): Transaction.Result {
                                                   currentData.value?.let { cd->
                                                       val currentNetwrk = currentData.getValue(NetworkUserItem::class.java)
                                                       if (currentNetwrk?.status != "pro"){
                                                           val newNetworkItem = currentNetwrk?.copy(
                                                               status = "pro"
                                                           )
                                                           currentData.value = newNetworkItem
                                                           finish = true
                                                       }

                                                   }
                                                    return Transaction.success(currentData)
                                                }

                                                override fun onComplete(
                                                    error: DatabaseError?,
                                                    committed: Boolean,
                                                    currentData: DataSnapshot?
                                                ) {
                                                    if (finish){
                                                        Log.d("Finish","Claimed")
                                                    }
                                                }
                                            }
                                        )
                                }
                            }
                        }
                        holder.btnClaim.setOnClickListener {
                            if (thisUser != null) {
                                onClaimClicked(thisUser)
                            }
                        }
                    }
            }

        }
    }



    override fun getItemCount(): Int = networkList.size

    class NetworkItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val indexTv: TextView = view.findViewById(R.id.tv_network_item_index)
        val networkName: TextView = view.findViewById(R.id.tv_network_item_name)
        val btnClaim: Button = view.findViewById(R.id.btn_claim_network_reward)
        val frameloading: FrameLayout = view.findViewById(R.id.frame_loading_network)
    }

}
package com.vrcareer.b4b.model

import android.view.ViewDebug.FlagToString

data class EarningDTO(
    var userid: String? = null,
    var balance: Float? = null,
    var total_earning: Float? = null,
    var total_pending: Float? = null,
    var total_withdrawal: Float? = null,
    var pending_withdrawal:Float? = null,
    var withdrawalHistory: MutableList<WithdrawalRequest>? = null,

)

data class Transaction(
    var tid:String? = null,
    var transaction_type: String? = null,
    var amount: String? =null,
    var userid: String? = null,
    var time_of_request: Long? = null,
    var time_of_approval: Long? = null,
 /*   var associated_job_id: String? = null,
    var associated_task_id: String? = null*/
)
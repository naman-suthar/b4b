package com.vrcareer.b4b.model

data class WithdrawalRequest(
    var id: String? = null,
    var amount: Float? = null,
    var user_id: String? = null,
    var time_of_request: Long? = null,
    var time_of_approval: Long? = null,
    var status: String? = null,
    var userName: String? = null
)

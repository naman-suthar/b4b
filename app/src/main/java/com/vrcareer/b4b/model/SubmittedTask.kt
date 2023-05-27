package com.vrcareer.b4b.model

data class SubmittedTask(
    var uniqueId: String? = null,
    var uid: String? = null,
    var taskId: String? = null,
    var jobId:String? = null,
    var associated_amount: Float? = null,
    var imageList: List<String>? = null,
    var time_of_submission: Long? = null,
    var time_of_approval: Long? = null,
    var status: String? = null,
    var message: String? = null,
    var answerList: List<Answer>? = null,
    var client_detail: String? = null
// Details or Fields to be added
)

package com.vrcareer.b4b.model

import com.vrcareer.b4b.utils.ApplicationResponse

data class JobApplication(
    var user_id: String? = null,
    var job_id:String? = null,
    var ansList: List<Answer>? = null,
    var status: String = ApplicationResponse.Pending.name,
    var rejection_message: String? = null,
    var time_of_request: Long? = null,
    var time_of_approval: Long? = null
)
data class Assessment(
    var assessment_id: String? = null,
    var user_id: String? = null,
    var job_id:String? = null,
    var task_id: String? = null,
    var ansList: List<Answer>? = null,
    var status: String = ApplicationResponse.Pending.name,
    var rejected_message: String? = null,
    var time_of_request: Long? = null,
    var time_of_approval: Long? = null
)
data class Answer(
    val question: String? = null,
    val answer: String? = null
)
package com.vrcareer.b4b.model

data class JobApplication(
    var user_id: String? = null,
    var job_id:String? = null,
    var ansList: List<Answer>? = null,
    var status: String = "pending",
    var time_of_request: Long? = null,
    var time_of_approval: Long? = null
)
data class Assessment(
    var assessment_id: String? = null,
    var user_id: String? = null,
    var job_id:String? = null,
    var task_id: String? = null,
    var ansList: List<Answer>? = null,
    var status: String = "pending",
    var time_of_request: Long? = null,
    var time_of_approval: Long? = null
)
data class Answer(
    val question: String? = null,
    val answer: String? = null
)
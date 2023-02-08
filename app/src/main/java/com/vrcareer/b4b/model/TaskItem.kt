package com.vrcareer.b4b.model

data class TaskItem(
    var taskId: String? = null,
    var jobId: String? = null,
    var task_title: String? = null,
    var task_tagline:String? = null,
    var task_earning_price: Int? = null
):java.io.Serializable

fun getAirtelTask():TaskItem{
    return TaskItem(
        "Tsk001",
        "IDJ1",
        "Merchant Acquisition",
        "Setup QR Codes",
        50
    )
}
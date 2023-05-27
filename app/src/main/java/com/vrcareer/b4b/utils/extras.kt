package com.vrcareer.b4b.utils

sealed class TaskEarningType(val type: String){
    object Fixed : TaskEarningType("Fixed")
    object Percentage : TaskEarningType("Percentage")
}
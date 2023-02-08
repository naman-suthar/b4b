package com.vrcareer.b4b

import android.app.Application
import android.content.Intent
import com.google.android.material.color.DynamicColors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vrcareer.b4b.model.User

class MyApplication: Application() {

    var userUniv: User? = null

    override fun onCreate() {
        super.onCreate()

        /*val fAuth = FirebaseAuth.getInstance()
        val firebaseUser = fAuth.currentUser
        if (firebaseUser != null){
            //Redirect To Home Page
            startActivity(Intent(this@MyApplication,HomeActivity::class.java))
        }*/
        val db = FirebaseDatabase.getInstance()
        val auth = FirebaseAuth.getInstance()
        auth.currentUser?.uid?.let {uid->
            db.reference.child("users").child(uid).get().addOnSuccessListener {
                if (it.exists()){
                    userUniv = it.getValue(User::class.java)
                }
            }
        }
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
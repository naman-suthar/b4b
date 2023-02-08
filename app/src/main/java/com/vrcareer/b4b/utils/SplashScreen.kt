package com.vrcareer.b4b.utils

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.vrcareer.b4b.R
import com.vrcareer.b4b.auth.ui.EnterPhoneActivity


class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
   /*     FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(
                this
            ) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    Log.d("DeepLink","$deepLink")
                }


                // Handle the deep link. For example, open the linked content,
                // or apply promotional credit to the user's account.
                // ...

                // ...
            }
            .addOnFailureListener(
                this
            ) { e -> Log.w("FB:", "getDynamicLink:onFailure", e) }*/
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, EnterPhoneActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }


}
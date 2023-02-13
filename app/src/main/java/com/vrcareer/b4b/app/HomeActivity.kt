package com.vrcareer.b4b.app

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.vrcareer.b4b.R
import com.vrcareer.b4b.app.dashboard.DashBoardFragment
import com.vrcareer.b4b.app.earning.EarningFragment
import com.vrcareer.b4b.app.profile.ProfileContainerFragment
import com.vrcareer.b4b.app.refer.ReferHomeFragment
import com.vrcareer.b4b.app.tasks.TaskHomeFragment
import com.vrcareer.b4b.databinding.ActivityHomeBinding
import com.vrcareer.b4b.model.User

class HomeActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private lateinit var binding: ActivityHomeBinding
    private var VR_URI =
        "https://app.pyjamahr.com/careers?company=VRCareerz&company_uuid=60D280017E&isHeaderVisible=true&is_careers_page=true"
    private var package_name = "com.android.chrome"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        /* if ((this.application as MyApplication).userUniv == null){
             (this.application as MyApplication).userUniv =  auth.currentUser
         }*/
        handelDynamicLinks()
        loadFragment(DashBoardFragment())
        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.tab_dashboard -> {
                    loadFragment(DashBoardFragment())
                    true
                }
                R.id.tab_tasks -> {
                    loadFragment(TaskHomeFragment())
                    true
                }
                R.id.tab_refer -> {
                    loadFragment(ReferHomeFragment())
                    true
                }
                /*      R.id.tab_career -> {
      //                    loadFragment(CareerHomeFragment())
                          val builder = CustomTabsIntent.Builder()

                          // to set the toolbar color use CustomTabColorSchemeParams
                          // since CustomTabsIntent.Builder().setToolBarColor() is deprecated

                          val params = CustomTabColorSchemeParams.Builder()
                          params.setToolbarColor(
                              ContextCompat.getColor(
                                  this@HomeActivity,
                                  R.color.md_theme_dark_inversePrimary
                              )
                          )
                          builder.setDefaultColorSchemeParams(params.build())

                          // shows the title of web-page in toolbar
                          builder.setShowTitle(true)

                          // setShareState(CustomTabsIntent.SHARE_STATE_ON) will add a menu to share the web-page
                          builder.setShareState(CustomTabsIntent.SHARE_STATE_ON)

                          // To modify the close button, use
                          // builder.setCloseButtonIcon(bitmap)

                          // to set weather instant apps is enabled for the custom tab or not, use
                          builder.setInstantAppsEnabled(true)

                          //  To use animations use -
                          //  builder.setStartAnimations(this, android.R.anim.start_in_anim, android.R.anim.start_out_anim)
                          //  builder.setExitAnimations(this, android.R.anim.exit_in_anim, android.R.anim.exit_out_anim)
                          val customBuilder = builder.build()
                          customBuilder.intent.setPackage(package_name)
                          customBuilder.launchUrl(this@HomeActivity, Uri.parse(VR_URI))

                          false
                      }*/
                R.id.tab_earning -> {
                    loadFragment(EarningFragment())
                    true
                }
                R.id.tab_profile -> {
                    loadFragment(ProfileContainerFragment())
                    true
                }
                else -> {
                    false
                }
            }
        }
        binding.bottomNav.selectedItemId = R.id.tab_tasks
        setContentView(binding.root)
    }

    private fun handelDynamicLinks() {
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                }

                // Handle the deep link
                handleDeepLink(deepLink)
            }
            .addOnFailureListener(this) { e -> Log.w("DeepLink", "getDynamicLink:onFailure", e) }

    }

    private fun handleDeepLink(deepLink: Uri?) {
        if (deepLink != null) {
            val path = deepLink.path
            Log.d("Deep:", "$deepLink")

            val referredUid = deepLink.getQueryParameter("invitedby")
            Log.d("NetworkDeep:", "$referredUid")
            if (referredUid != null) {
                val currUser = auth.currentUser!!.uid
                val referalItem = NetworkUserItem(
                    id = currUser,
                    status = "noob"
                )
//                if (referredUid != currUser){


                var finish = false
                db.reference.child("users").child(currUser).runTransaction(
                    object : Transaction.Handler {
                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                            currentData.value?.let {
                                val user = currentData.getValue(User::class.java)
                                if (user?.reffered_by == null || user?.reffered_by?.isEmpty() == true){
                                    val newUser = user?.copy(reffered_by = referredUid)
                                    currentData.value = newUser
                                    db.reference.child("network").child(referredUid).child(currUser)
                                        .setValue(
                                            referalItem
                                        ).addOnSuccessListener {

                                        }.addOnFailureListener {
                                            Toast.makeText(
                                                this@HomeActivity,
                                                "Firebase Error in Adding Referral",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                        }
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
                            if (finish) {
                                Toast.makeText(
                                    this@HomeActivity,
                                    "You are in Network",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }
                    }
                )


//                }

                /*.runTransaction(
                    object : Transaction.Handler{
                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                            Log.d("NetworkDeep:","Working")
                            currentData.value?.let {
                                Log.d("NetworkDeep:","${currentData.value}")
                                val network = currentData.value as? MutableList<String> ?: mutableListOf()
                                Log.d("NetworkDeepN:","$network")
                            }

                            return Transaction.success(currentData)
                        }

                        override fun onComplete(
                            error: DatabaseError?,
                            committed: Boolean,
                            currentData: DataSnapshot?
                        ) {

                        }
                    }
                )*/
            }

            // Take appropriate action with the profile name
            Toast.makeText(this, "referred by $referredUid", Toast.LENGTH_SHORT).show()

        }

    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

}

data class NetworkUserItem(
    val id: String? = null,
    val status: String? = null
)
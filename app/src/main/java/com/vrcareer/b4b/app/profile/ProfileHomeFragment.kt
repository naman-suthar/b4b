package com.vrcareer.b4b.app.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vrcareer.b4b.R
import com.vrcareer.b4b.app.Constants
import com.vrcareer.b4b.auth.ui.EnterPhoneActivity
import com.vrcareer.b4b.databinding.FragmentProfileHomeBinding
import com.vrcareer.b4b.model.EarningDTO
import com.vrcareer.b4b.model.User
import org.w3c.dom.Text

/**
 * This is Fragment which appears first when Profile tab is clicked It contains all the Options
 * like
 * EarningDetails,
 *  KYCDetails ,
 *  Visit Profile,
 *  App content,
 *  Sign Out
 * */
class ProfileHomeFragment : Fragment() {
    private var binding: FragmentProfileHomeBinding? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private var alertDialog: AlertDialog? = null
    private var VR_URI = "https://account.solidperformers.com/leadTracking/support_form/OTk="
    private var package_name = "com.android.chrome"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileHomeBinding.inflate(inflater,container,false)
        getUserName()
        getEarningData()
        binding?.mcChoosePaymentMethod?.setOnClickListener {
            val action = ProfileHomeFragmentDirections.actionProfileHomeFragmentToAddPaymentMethodFragment()
            findNavController().navigate(action)
        }
        binding?.mcKyc?.setOnClickListener {
            val action = ProfileHomeFragmentDirections.actionProfileHomeFragmentToAddKycDetailsFragment()
            findNavController().navigate(action)
        }
        binding?.btnViewProfile?.setOnClickListener {
            val action = ProfileHomeFragmentDirections.actionProfileHomeFragmentToProfileFragment()
            findNavController().navigate(action)
        }
        binding?.btnLogOut?.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireActivity(), EnterPhoneActivity::class.java))
            requireActivity().finish()
        }
        binding?.btnAppContent?.setOnClickListener {
            val dialog =  MaterialAlertDialogBuilder(requireContext())
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_app_content,null,false)

            dialog.setView(view)
            alertDialog = dialog.create()

            view.findViewById<TextView>(R.id.tv_privacy_policy).setOnClickListener {
                alertDialog?.dismiss()
                val action = ProfileHomeFragmentDirections.actionProfileHomeFragmentToPrivacyPoliciesFragment()
                findNavController().navigate(action)
            }
            view.findViewById<TextView>(R.id.tv_disclamer).setOnClickListener {
                alertDialog?.dismiss()
                val action = ProfileHomeFragmentDirections.actionProfileHomeFragmentToDisclaimerFragment()
                findNavController().navigate(action)
            }
            view.findViewById<TextView>(R.id.tv_terms_and_condition).setOnClickListener {
                alertDialog?.dismiss()
                val action = ProfileHomeFragmentDirections.actionProfileHomeFragmentToTermsAndConditionFragment()
                findNavController().navigate(action)
            }
            alertDialog?.show()
        }

        binding?.btnSupport?.setOnClickListener {
            val builder = CustomTabsIntent.Builder()

            // to set the toolbar color use CustomTabColorSchemeParams
            // since CustomTabsIntent.Builder().setToolBarColor() is deprecated

            val params = CustomTabColorSchemeParams.Builder()
            params.setToolbarColor(
                ContextCompat.getColor(
                    requireContext(),
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
            customBuilder.launchUrl(requireContext(), Uri.parse(VR_URI))

        }
        binding?.btnWithdrawalHistory?.setOnClickListener {
            val action = ProfileHomeFragmentDirections.actionProfileHomeFragmentToWithdrawHistoryFragment()
            findNavController().navigate(action)
        }
        return binding?.root
    }

    private fun getUserName() {
        db.reference.child(Constants.USER_FIREBASE_PATH_ROOT).child(auth.currentUser?.uid.toString()).get().addOnSuccessListener {
            if (it.exists()){
                val user = it.getValue(User::class.java)
                binding?.let { b->
                    b.tvGreeting.text = "Welcome, ${user?.name}!"
                }
            }
        }
            .addOnFailureListener {e->
                Toast.makeText(context,"Network error ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }
    private fun getEarningData() {
        db.reference.child("earnings").child(auth.currentUser?.uid.toString()).get().addOnSuccessListener {
            if (it.exists()){
//                binding?.frameLoading?.visibility = View.GONE
                val earningDetails = it.getValue(EarningDTO::class.java)
                Log.d("Earning","$earningDetails")
                binding?.let{b->
                    earningDetails?.let {ed->
                        ed.balance?.let {
                            b.tvAmountApproved.text = "\u20B9${earningDetails?.total_withdrawal.toString()}"
                        }
                      ed.total_pending?.let {
                          b.tvAmountPending.text = "₹${earningDetails?.pending_withdrawal.toString()}"
                      }
                     if (ed.balance != null && ed.total_pending != null){
                         b.tvTotalEarning.text = "Total Earnings: \u20B9 ${ed.total_earning}"
                     }

                    }

                }
            }
        }.addOnFailureListener {
            Toast.makeText(context,"Network Error occurred", Toast.LENGTH_SHORT).show()

        }
    }
}
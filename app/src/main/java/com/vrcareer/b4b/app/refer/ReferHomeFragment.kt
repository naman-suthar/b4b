package com.vrcareer.b4b.app.refer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import com.vrcareer.b4b.databinding.FragmentReferHomeBinding

/**
 * Fragment for Refer Tab*/
class ReferHomeFragment : Fragment() {
    private var binding: FragmentReferHomeBinding? = null
    private  var mInvitationUrl: Uri? = null
    @SuppressLint("QueryPermissionsNeeded")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentReferHomeBinding.inflate(inflater,container,false)

        /*binding?.referFriend?.setOnClickListener {
           val message = generateUriToShare(requireContext(),"Naman")
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, message.toString())
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            context?.startActivity(shareIntent)
        }*/
/**
 * Gererating dynamic link
 * */
        binding?.referFriend?.setOnClickListener {
            val user = Firebase.auth.currentUser!!
            val uid = user.uid
            val invitationLink = "https://www.example.com/?invitedby=$uid"
            Firebase.dynamicLinks.shortLinkAsync {
                link = Uri.parse(invitationLink)
                domainUriPrefix = "https://brand4brandss.page.link"
                androidParameters("com.vrcareer.b4b") {

                }
                socialMetaTagParameters {
                    this.title = "Brand4Brands"
                    this.description = "Tagline is not decided yet"
                    this.imageUrl = Uri.parse("https://images.freeimages.com/vhq/images/previews/86d/wharf-industrial-vector-28421.jpg")
                }
                /*iosParameters("com.example.ios") {
                    appStoreId = "123456789"
                    minimumVersion = "1.0.1"
                }*/
            }.addOnSuccessListener { shortDynamicLink ->
                mInvitationUrl = shortDynamicLink.shortLink
                val referrerName = Firebase.auth.currentUser?.displayName
                /*val subject = String.format("%s wants you to play MyExampleGame!", referrerName)
                val invitationLink = mInvitationUrl.toString()
                val msg = "Let's play MyExampleGame together! Use my referrer link: $invitationLink"
                val msgHtml = String.format("<p>Let's play MyExampleGame together! Use my " +
                        "<a href=\"%s\">referrer link</a>!</p>", invitationLink)*/

                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, mInvitationUrl.toString())
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                context?.startActivity(shareIntent)
            }   .addOnFailureListener {e->
                Toast.makeText(context,"Network error ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }


        binding?.claimReward?.setOnClickListener {
            val intent = Intent(requireActivity(),ClaimReferalsActivity::class.java)
            requireActivity().startActivity(intent)
        }
        return binding?.root
    }



}
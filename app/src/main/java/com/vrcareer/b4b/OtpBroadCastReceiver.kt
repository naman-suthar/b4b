package com.vrcareer.b4b

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class OtpBroadCastReceiver: BroadcastReceiver() {
    var smsBroadCastReceiverListener: OtpBroadCastReceiverListener ?= null
    override fun onReceive(p0: Context?, intent: Intent?) {
        if(SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action){
            val extras = intent.extras
            val smsRetrievesStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as Status

            when(smsRetrievesStatus.statusCode){
                CommonStatusCodes.SUCCESS -> {
                    val messageIntent = extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                    if (messageIntent != null) {
                        smsBroadCastReceiverListener?.onSuccess(messageIntent)
                    }
                }
                CommonStatusCodes.TIMEOUT -> {
                    smsBroadCastReceiverListener?.onFailure()
                }
            }
        }
    }

    interface OtpBroadCastReceiverListener{

        fun onSuccess(intent: Intent)
        fun onFailure()
    }
}
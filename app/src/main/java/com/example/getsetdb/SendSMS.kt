package com.example.getsetdb

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.telephony.SmsManager

class SendSMS: Service() {
    override fun onBind(p0: Intent?): IBinder? = null

    fun sendSMS(){
        var sms= SmsManager.getDefault()
        sms.sendTextMessage("796851896", "ME", "HERE", null, null)
    }
}
package com.example.getsetdb

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.util.*

class ServiceBackground: Service() {
    val TAG = "MyService"
    private val channel_id="ForegroundService Kotlin"

    init {
        Log.d(TAG, "SERVICE IS RUNNING")
    }

    companion object{
        fun startService(context: Context, message: String){
            val startIntent= Intent(context, ServiceBackground::class.java)
            startIntent.putExtra("inputExtra", message)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context){
            val stopIntent = Intent(context, ServiceBackground::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val input = intent?.getStringExtra("inputExtra")
        createNottificationChannel()
        val notificationIntent = Intent(this, MainScreen::class.java)
        val pendingIntent= PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val notification = NotificationCompat.Builder(this, channel_id).setContentTitle("GetSetDB")
            .setContentText(input)
            .setSmallIcon(androidx.drawerlayout.R.drawable.notification_icon_background)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)

        return START_NOT_STICKY
    }

    private fun createNottificationChannel(){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            val serviceChannel = NotificationChannel(channel_id, "Foreground Service Channel", NotificationManager.IMPORTANCE_DEFAULT)
            val manager=getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "KILLED")
    }

}
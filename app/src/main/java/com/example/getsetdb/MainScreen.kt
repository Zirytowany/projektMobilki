package com.example.getsetdb

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import android.view.Gravity.apply
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class MainScreen : AppCompatActivity(), LocationListener {
    private lateinit var btnSendData: Button
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var handler: Handler = Handler()
    private var runnable: Runnable?=null
    private var delay=1
    private val TAG="GPS"
    private var szer=""
    private var dlug=""
    private lateinit var database: DatabaseReference
    private var ref="https://getsetdb-default-rtdb.europe-west1.firebasedatabase.app"
    private lateinit var locationRequest: LocationRequest
    private var currentLocation: Location? = null
    private var PERMISSION_REQUEST_ACCESS_LOCATION=100
    private var login: HashMap<String, String>? = null
    private var id: String?=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)
        ServiceBackground.startService(this, "GesSetDB działa w tle...")

        btnSendData=findViewById(R.id.btnSendData)
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this)
        database = FirebaseDatabase.getInstance(ref).getReference("Users")

            id=intent.getStringExtra("id").toString()

            database.child(id!!).get().addOnSuccessListener {
                login = it.value as HashMap<String, String>
                Log.d(TAG, login.toString())
            }

        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.SEND_SMS),111)
        }

        btnSendData.setOnClickListener{
            getCurrentLocation()
            if(szer=="" && dlug==""){
                Log.d(TAG, "NULL RECEIVED")
                getCurrentLocation()
            }
            sendSMS("796851896", "szerokość: "+szer+" długość: "+dlug)
            sendGPS(id!!)
        }

    }



    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode== KeyEvent.KEYCODE_VOLUME_DOWN){
            if(szer=="" && dlug==""){
                Log.d(TAG, "NULL RECEIVED")
                getCurrentLocation()
            }
            sendSMS("796851896", "szerokość: "+szer+" długość: "+dlug)
            sendGPS(id!!)
            return true
        }
        return super.onKeyLongPress(keyCode, event)
    }



    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode== KeyEvent.KEYCODE_VOLUME_DOWN){
            if (event != null) {
                event.startTracking()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun sendSMS(number: String, text:String){
        val sms= SmsManager.getDefault()
        sms.sendTextMessage(number, "ME", text, null, null)
    }

    private fun getCurrentLocation(){
        if(checkPermissions()){
            if(isLocationEnabled()){
                if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermission()
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this){
                        task-> val location: Location?=task.result
                    if(location==null){
                        //Toast.makeText(this, "Null Received", Toast.LENGTH_LONG).show()
                        getNewLocation()
                    }else{
                        //Toast.makeText(this, "Get Success", Toast.LENGTH_LONG).show()
                        szer=""+location.latitude
                        dlug=""+location.longitude
                    }
                }
            }else{
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent= Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }else{
            requestPermission()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getNewLocation(){
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval=0
        locationRequest.numUpdates = 2
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,locationCallback, Looper.myLooper()!!
        )
    }

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            //super.onLocationResult(p0)
            var lastLocation: Location? = p0.lastLocation
            szer=""+ lastLocation!!.latitude
            dlug=""+lastLocation!!.longitude
        }
    }

    override fun onLocationChanged(location:Location){
        szer=""+location.latitude
        dlug=""+location.longitude
    }

    private fun isLocationEnabled():Boolean{
        val locationManager: LocationManager =getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    companion object{
        private const val PERMISSION_REQUEST_ACCESS_LOCATION=100
    }

    private fun checkPermissions(): Boolean{
        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED&&
            ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode== PERMISSION_REQUEST_ACCESS_LOCATION){
            if(grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Granted", Toast.LENGTH_LONG).show()
                getCurrentLocation()
            }else{
                Toast.makeText(applicationContext, "Denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume(){
        handler.postDelayed(Runnable {
            handler.postDelayed(runnable!!, delay.toLong())
            getCurrentLocation()
            //Log.d(TAG, "GPS LOCATION OBTAINED ${szer}, ${dlug}")
            if(szer!="" && dlug!=""){
                //Log.d(TAG, "GPS DELAY CHANGED")
                delay=20000
            }
        }.also { runnable=it }, delay.toLong())

        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable!!)
    }

    private fun isOnline(context: Context?): Boolean{
        if (context==null) return false
        val connectiviManager=context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            val capabilities =connectiviManager.getNetworkCapabilities(connectiviManager.activeNetwork)
            if(capabilities!=null){
                when{
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)->{
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)->{
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)->{
                        return true
                    }
                }
            }
        }else{
            val activeNetworkInfo=connectiviManager.activeNetworkInfo
            if(activeNetworkInfo != null && activeNetworkInfo.isConnected)
                return true
        }
        return false
    }

    private fun sendGPS( id:String, ){
        val pass = login?.get("password").toString()
        Log.d("TAG", currentLocation.toString())
        val user = User(id,pass, szer, dlug)
        if(isOnline(this))
            Toast.makeText(this, "Jest połaczenie z  Internetem", Toast.LENGTH_SHORT).show()
        database.child(id).setValue(user).addOnSuccessListener {
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
            database.child(id).get().addOnSuccessListener {
                Log.d("TAG", "got value ${it.value}")
            }.addOnFailureListener{
                Log.d("TAG", "error")
            }
        }.addOnFailureListener{
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
        }
    }
}
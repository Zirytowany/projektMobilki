package com.example.getsetdb

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private val nameLiveData = MutableLiveData<String>()
    private var logged=false
    private var getData=false
    private lateinit var database: DatabaseReference
    private  var ref="https://getsetdb-default-rtdb.europe-west1.firebasedatabase.app"
    private var login: HashMap<String, String> = HashMap<String, String>()
    val TAG="Logowanie"

    private val isValidLiveData = MediatorLiveData<Boolean>().apply {
        this.value=false
        addSource(nameLiveData){ name->
            val name = nameLiveData.value
            this.value=validateForm(name)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nameLayout=findViewById<TextInputLayout>(R.id.name)
        val logIn=findViewById<Button>(R.id.login)

        database = FirebaseDatabase.getInstance(ref).getReference("Users")

        nameLayout.editText?.doOnTextChanged{text,_,_,_ ->
            nameLiveData.value=text?.toString()
        }

        isValidLiveData.observe(this){isValid->
            logIn.isEnabled=isValid
        }

        logIn.setOnClickListener{
            if(loggedIn(nameLiveData.value) || logged) {
                Toast.makeText(this, "Zalogowano", Toast.LENGTH_SHORT).show()
                logged=true
                val intent= Intent(this, MainScreen::class.java)
                startActivity(intent)
            }else
                Toast.makeText(this, "Nie znaleziono identyfikatora lub nie ma połączenia z internetem", Toast.LENGTH_SHORT).show()
        }
    }

    private  fun validateForm(name: String?): Boolean{
        val isValidName=name!=null && name.isNotBlank()
        return isValidName
    }

    private fun loggedIn(id: String?): Boolean {
        if(isOnline(this)){
            if (id != null) {
                database.child(id).get().addOnSuccessListener {
                    login = it.value as HashMap<String, String>
                }
                while(getData){
                    if(login.containsValue(id)){
                        getData=true
                    }
                }
                getData=false
                return true
            }
        }
        return false
    }

    fun isOnline(context: Context?): Boolean{
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
}
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
    private val passLiveData = MutableLiveData<String>()
    private var logged=false
    private var getData=false
    private lateinit var database: DatabaseReference
    private  var ref="https://getsetdb-default-rtdb.europe-west1.firebasedatabase.app"
    private var login: HashMap<String, String> = HashMap<String, String>()
    val TAG="Logowanie"

    private val isValidLiveData = MediatorLiveData<Boolean>().apply {
        this.value=false
        addSource(nameLiveData){ name->
            val pass = passLiveData.value
            this.value=validateForm(name, pass)
        }

        addSource(passLiveData){ pass->
            val name = nameLiveData.value
            this.value=validateForm(name, pass)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nameLayout=findViewById<TextInputLayout>(R.id.name)
        val passLayout=findViewById<TextInputLayout>(R.id.pass)
        val logIn=findViewById<Button>(R.id.login)
        val rejestr=findViewById<Button>(R.id.restr)

        database = FirebaseDatabase.getInstance(ref).getReference("Users")

        nameLayout.editText?.doOnTextChanged{text,_,_,_ ->
            nameLiveData.value=text?.toString()
        }

        passLayout.editText?.doOnTextChanged{text,_,_,_ ->
            passLiveData.value=text?.toString()
        }

        isValidLiveData.observe(this){isValid->
            logIn.isEnabled=isValid
        }

        logIn.setOnClickListener{
            if(loggedIn(nameLiveData.value, passLiveData.value) || logged) {
                Toast.makeText(this, "Zalogowano", Toast.LENGTH_SHORT).show()
                logged=true
                val intent= Intent(this, MainScreen::class.java)
                intent.putExtra("id", nameLiveData.value.toString())
                startActivity(intent)
            }else
                Toast.makeText(this, "Nie znaleziono identyfikatora lub nie ma połączenia z internetem", Toast.LENGTH_SHORT).show()
        }

        rejestr.setOnClickListener {
            val intent= Intent(this, Rejestracja::class.java)
            startActivity(intent)
        }
    }

    private  fun validateForm(name: String?, pass: String?): Boolean{
        val isValidName=name!=null && name.isNotBlank()
        val isValidPass=pass!=null && pass.isNotBlank() && pass.length>=6
        return isValidName && isValidPass
    }

    private fun loggedIn(id: String?, pass: String?): Boolean {
        if(isOnline(this)){
            if (id != null) {
                database.child(id).get().addOnSuccessListener {
                    login = it.value as HashMap<String, String>
                    Log.d(TAG, login.toString())
                }
                login.forEach(){
                    if(login.get("id").toString()==id && login.get("password").toString()==pass){
                        Log.d(TAG, "pass")
                        getData=true
                    }
                }
                if(getData){
                    getData=false
                    return true
                }
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
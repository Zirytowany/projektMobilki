package com.example.getsetdb

import android.content.Intent
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

class Rejestracja : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private  var ref="https://getsetdb-default-rtdb.europe-west1.firebasedatabase.app"
    private var data: HashMap<String, String> = HashMap<String, String>()
    val TAG="Rejestracja"

    private val nameLiveData = MutableLiveData<String>()
    private val passLiveData = MutableLiveData<String>()
    private val passSecLiveData = MutableLiveData<String>()

    private val isValidLiveData = MediatorLiveData<Boolean>().apply {
        this.value=false
        addSource(nameLiveData){ name->
            val pass = passLiveData.value
            val passSec = passSecLiveData.value
            this.value=validateForm(name, pass, passSec)
        }

        addSource(passLiveData){ pass->
            val name = nameLiveData.value
            val passSec = passSecLiveData.value
            this.value=validateForm(name, pass, passSec)
        }

        addSource(passSecLiveData){ passSec->
            val name = nameLiveData.value
            val pass = passLiveData.value
            this.value=validateForm(name, pass, passSec)
        }
    }

    private  fun validateForm(name: String?, pass: String?, passSec: String?): Boolean{
        val isValidName=name!=null && name.isNotBlank()
        val isValidPass=pass!=null && pass.isNotBlank() && pass.length>=6
        val isValidPassSec=passSec!=null && passSec.isNotBlank() && pass==passSec
        return isValidName && isValidPass && isValidPassSec
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rejestracja)

        val nameLayout=findViewById<TextInputLayout>(R.id.name)
        val passLayout=findViewById<TextInputLayout>(R.id.pass)
        val passSecLayout=findViewById<TextInputLayout>(R.id.passSec)
        val zarej=findViewById<Button>(R.id.zarej)

        database = FirebaseDatabase.getInstance(ref).getReference("Users")

        nameLayout.editText?.doOnTextChanged{text,_,_,_ ->
            nameLiveData.value=text?.toString()
        }

        passLayout.editText?.doOnTextChanged{text,_,_,_ ->
            passLiveData.value=text?.toString()
        }

        passSecLayout.editText?.doOnTextChanged{text,_,_,_ ->
            passSecLiveData.value=text?.toString()
        }

        isValidLiveData.observe(this){isValid->
            zarej.isEnabled=isValid
        }

        zarej.setOnClickListener {
            if (nameLiveData.value != null) {
                database.child(nameLiveData.value.toString()).get().addOnSuccessListener {
                    data = it.value as HashMap<String, String>
                    Log.d(TAG, data.toString())
                }
                if(data.isNullOrEmpty()){
                    val user=User(nameLiveData.value.toString(), passLiveData.value.toString())
                    database.child(nameLiveData.value.toString()).setValue(user).addOnSuccessListener {
                        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener{
                        Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                    }
                } else{
                    Toast.makeText(this, "Istnieje już taki użytkownik", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
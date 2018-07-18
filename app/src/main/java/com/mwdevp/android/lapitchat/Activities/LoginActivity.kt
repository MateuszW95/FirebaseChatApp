package com.mwdevp.android.lapitchat.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.mwdevp.android.lapitchat.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var mProgressDialog: ProgressDialog
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mUserDatabaseReference: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mFirebaseAuth=FirebaseAuth.getInstance()
        mUserDatabaseReference=FirebaseDatabase.getInstance().reference.child("users")
        mProgressDialog= ProgressDialog(this@LoginActivity)
        login_button.setOnClickListener { 
            
            val email=login_email.text.toString()
            val password=login_password.text.toString()
            
            if(!TextUtils.isEmpty(email) || (!TextUtils.isEmpty(password))){
                mProgressDialog.setTitle("Logging in")
                mProgressDialog.setMessage("Please wait while we check your credentials")
                mProgressDialog.setCanceledOnTouchOutside(false)
                mProgressDialog.show()
                loginUser(email,password)
            }
        }

        setSupportActionBar(login_toolbar as Toolbar?)
        supportActionBar!!.title="Login"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun loginUser(email: String, password: String) {

        mFirebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this) {
            if(it.isSuccessful){
                mProgressDialog.dismiss()

                val currentUserId=FirebaseAuth.getInstance().currentUser!!.uid
                val deviceToken=FirebaseInstanceId.getInstance().token
                mUserDatabaseReference.child(currentUserId).child("device_token").setValue(deviceToken).addOnSuccessListener {
                    val intent=Intent(this@LoginActivity,MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                }

            }
            else
            {
                mProgressDialog.hide()
                Toast.makeText(this@LoginActivity, " ${it.exception.toString()}", Toast.LENGTH_LONG).show()

            }
        }
    }
}

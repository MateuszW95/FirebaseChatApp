package com.mwdevp.android.lapitchat.Activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import com.mwdevp.android.lapitchat.R
import kotlinx.android.synthetic.main.activity_register.*
import android.widget.Toast
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.Task
import android.support.annotation.NonNull
import com.google.android.gms.tasks.OnCompleteListener
import android.R.attr.password
import android.app.ProgressDialog
import android.content.Intent
import android.support.design.widget.TextInputEditText
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId


class RegisterActivity : AppCompatActivity() {

    private lateinit var mAuth:FirebaseAuth

    private lateinit var mProgressDialog:ProgressDialog
    private lateinit var mDatabase:DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        mAuth=FirebaseAuth.getInstance()
        reg_submit_bt.setOnClickListener {

            val displayName=(reg_name_tv as TextInputEditText).text.toString()
            val email=(reg_email_tv as TextInputEditText).text.toString()
            val password=(reg_password_tv as TextInputEditText).text.toString()

            if(!TextUtils.isEmpty(displayName) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password))
            {
                mProgressDialog.setTitle("Registering user")
                mProgressDialog.setMessage("Please wait while we create your account")
                mProgressDialog.setCanceledOnTouchOutside(false)
                mProgressDialog.show()
                registerUser(displayName,email,password)
            }

        }

        setSupportActionBar(register_toolbar as Toolbar?)
        supportActionBar!!.title="Create account"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mProgressDialog=ProgressDialog(this@RegisterActivity)
    }

    private fun registerUser(name:String,email:String,password:String){

        mAuth.createUserWithEmailAndPassword(email!!,password!!).addOnCompleteListener(this@RegisterActivity){ task ->
            val a = task
            if (task.isSuccessful) {

                val current_user=FirebaseAuth.getInstance().currentUser
                val uid=current_user!!.uid
                mDatabase= FirebaseDatabase.getInstance().reference.child("users").child(uid)
                val deviceToken= FirebaseInstanceId.getInstance().token
                val userMap=HashMap<String,String>()
                userMap.put("name",name)
                userMap.put("status","Hi there. I'm using Chat App")
                userMap.put("device_token",deviceToken!!)
                userMap.put("image","default")
                userMap.put("thumb_image","default")

                mDatabase.setValue(userMap).addOnCompleteListener(this@RegisterActivity){
                    if(it.isSuccessful){

                        mProgressDialog.dismiss()
                        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()
                    }
                }
//

            } else {
                mProgressDialog.hide()
                Toast.makeText(this@RegisterActivity, " ${task.exception.toString()}", Toast.LENGTH_LONG).show()
            }
        }
    }



}

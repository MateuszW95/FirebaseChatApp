package com.mwdevp.android.lapitchat.Activities

import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.SpannableStringBuilder
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.mwdevp.android.lapitchat.R
import kotlinx.android.synthetic.main.activity_status.*

class StatusActivity : AppCompatActivity() {

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mCurrentUser:FirebaseUser

    private lateinit var mProgress:ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)

        setSupportActionBar(status_app_bar as Toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title="Account settings"
        mCurrentUser= FirebaseAuth.getInstance().currentUser!!
        mDatabaseReference=FirebaseDatabase.getInstance().reference.child("users").child(mCurrentUser.uid)


        mProgress= ProgressDialog(this@StatusActivity)

        mDatabaseReference.child("status").addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {

                (status_input as EditText).text=SpannableStringBuilder(p0!!.value.toString())
            }

        })


        status_save_button.setOnClickListener {

            mProgress.setTitle("Saving changes")
            mProgress.setMessage("Please wait while we save changes")
            mProgress.show()
            val status=(status_input as EditText).text.toString()

            mDatabaseReference.child("status").setValue(status).addOnCompleteListener {
                if(it.isSuccessful){
                    mProgress.dismiss()
                }
                else{
                    mProgress.hide()
                }
            }
        }
    }
}

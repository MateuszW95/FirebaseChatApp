package com.mwdevp.android.lapitchat.Models

import android.app.Application
import android.support.annotation.IntegerRes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso

class LapitChat:Application() {

    private lateinit var mUserDatabase:DatabaseReference
    private lateinit var mAuth:FirebaseAuth

    override fun onCreate() {
        super.onCreate()
        //enable offline capabilities
       // FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        //Picasso offline

        val builder=Picasso.Builder(this@LapitChat)
        builder.downloader(OkHttp3Downloader(this@LapitChat, Long.MAX_VALUE))
        val built=builder.build()
        built.setIndicatorsEnabled(true)
        built.isLoggingEnabled=true
        Picasso.setSingletonInstance(built)


        mAuth=FirebaseAuth.getInstance()
        if(mAuth.currentUser!=null) {
            mUserDatabase = FirebaseDatabase.getInstance()
                    .reference.child("users").child(mAuth.currentUser!!.uid)

            mUserDatabase.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(p0: DataSnapshot?) {
                    if (p0 != null) {
                        mUserDatabase.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP)
                        mUserDatabase.child("online").setValue("true")

                    }
                }
            })
        }
    }
}
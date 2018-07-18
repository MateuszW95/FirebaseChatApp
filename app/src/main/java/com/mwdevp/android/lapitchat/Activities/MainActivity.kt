package com.mwdevp.android.lapitchat.Activities

import com.mwdevp.android.lapitchat.Adapters.SectionsPagerAdapter
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.mwdevp.android.lapitchat.R
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var mAuth:FirebaseAuth
    private lateinit var mSectionsPagerAdapter: SectionsPagerAdapter
    private lateinit var mUserRef:DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth=FirebaseAuth.getInstance()
        //toolbar settings
        setSupportActionBar(main_page_toolbar as Toolbar?)
        supportActionBar!!.title="Lapit chat"
        //viewPager
        mSectionsPagerAdapter= SectionsPagerAdapter(supportFragmentManager)
        mainTabPager.adapter=mSectionsPagerAdapter
        main_tabs.setupWithViewPager(mainTabPager)
        if(mAuth.currentUser!=null)
        {
            mUserRef=FirebaseDatabase.getInstance().reference.child("users").child(mAuth.currentUser!!.uid)
        }


    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.currentUser

        if(currentUser==null){
           sendToStart()
        }else
        {
            mUserRef.child("online").setValue("true")
        }
    }

    private fun sendToStart() {
        val startIntent= Intent(this@MainActivity,StartActivity::class.java)
        startActivity(startIntent)
        if(mAuth.currentUser!=null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP)
        }
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)

        menuInflater.inflate(R.menu.main_menu,menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        super.onOptionsItemSelected(item)

        when (item!!.itemId) {
            R.id.main_logout_button->{
                FirebaseAuth.getInstance().signOut()
                sendToStart()

            }
            R.id.main_settings_button->{
                val intent=Intent(this@MainActivity,SettingsActivity::class.java)
                startActivity(intent)
            }

            R.id.main_all_button->{
                val intent=Intent(this@MainActivity,UsersActivity::class.java)
                startActivity(intent)
            }

        }

        return true
    }

    override fun onStop() {
        super.onStop()
        if(mAuth.currentUser!=null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP)
        }
    }




}

package com.mwdevp.android.lapitchat.Activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.mwdevp.android.lapitchat.R
import kotlinx.android.synthetic.main.activity_start.*

class StartActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        start_reg_but.setOnClickListener {
            val regIntent= Intent(this@StartActivity,RegisterActivity::class.java)
            startActivity(regIntent)
        }

        start_login.setOnClickListener{
            startActivity(Intent(this@StartActivity,LoginActivity::class.java))
        }
    }
}

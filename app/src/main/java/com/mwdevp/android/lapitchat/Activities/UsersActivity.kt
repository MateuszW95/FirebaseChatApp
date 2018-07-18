package com.mwdevp.android.lapitchat.Activities

import com.mwdevp.android.lapitchat.Models.Users
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.mwdevp.android.lapitchat.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_users.*

class UsersActivity : AppCompatActivity() {

    private  lateinit var mUsersDatabase:DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)
        mUsersDatabase=FirebaseDatabase.getInstance().reference.child("users")
        mUsersDatabase.keepSynced(false)
        setSupportActionBar(users_app_bar as Toolbar)
        supportActionBar!!.title="All users"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        users_list.setHasFixedSize(true)
        users_list.layoutManager=LinearLayoutManager(this@UsersActivity)

    }

    //For display users on recycleView
    override fun onStart() {
        super.onStart()
        val options = FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(mUsersDatabase, Users::class.java)
                .setLifecycleOwner(this)
                .build()

        val adapter= object: FirebaseRecyclerAdapter<Users,UserViewHolder>(options){

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
                val view=LayoutInflater.from(this@UsersActivity).inflate(R.layout.user_single_layout,parent,false)
                return UserViewHolder(view)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: Users) {
                holder.bind(model)
                val userId=getRef(position).key
                holder.uID=userId
            }

        }
        users_list.adapter=adapter
    }

    inner class UserViewHolder(itemView: View):RecyclerView.ViewHolder(itemView),View.OnClickListener{
        lateinit var uID:String
        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            if(uID!=FirebaseAuth.getInstance().currentUser!!.uid) {
                val intent = Intent(this@UsersActivity, ProfileActivity::class.java)
                intent.putExtra("USER_KEY", uID)
                startActivity(intent)
            }
            else{
                Toast.makeText(this@UsersActivity,"This is your profile", Toast.LENGTH_LONG).show()

            }
        }



        fun bind(users: Users){
            val nameTextView:TextView=itemView.findViewById(R.id.user_name)
            val statusTextView:TextView=itemView.findViewById(R.id.user_status)
            val image:CircleImageView=itemView.findViewById(R.id.user_image)

            nameTextView.text=users.name
            statusTextView.text=users.status
            Picasso.get().load(users.thumb_image).placeholder(R.drawable.default_person).into(image)
        }

    }
}

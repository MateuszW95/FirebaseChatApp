package com.mwdevp.android.lapitchat.Fragments


import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mwdevp.android.lapitchat.Activities.ChatActivity
import com.mwdevp.android.lapitchat.Activities.ProfileActivity
import com.mwdevp.android.lapitchat.Activities.UsersActivity
import com.mwdevp.android.lapitchat.Models.Friends
import com.mwdevp.android.lapitchat.Models.Users

import com.mwdevp.android.lapitchat.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_users.*
import kotlinx.android.synthetic.main.fragment_friends.*


class FriendsFragment : Fragment() {

    private lateinit var mFriendsDatabaseReference: DatabaseReference
    private lateinit var mUsersDatabaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mCurrentUser:String
    private lateinit var mMainView:View
    private lateinit var mListView:RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mMainView=inflater.inflate(R.layout.fragment_friends, container, false)
        mAuth=FirebaseAuth.getInstance()
        mCurrentUser=mAuth.currentUser!!.uid
        mFriendsDatabaseReference=FirebaseDatabase.getInstance().reference.child("friends").child(mCurrentUser)
        //mFriendsDatabaseReference.keepSynced(true)
        mUsersDatabaseReference=FirebaseDatabase.getInstance().reference.child("users")
        mListView=mMainView.findViewById(R.id.friends_list)
        mListView.setHasFixedSize(true)
        mListView.layoutManager=LinearLayoutManager(context)
        return mMainView
    }


    override fun onStart() {
        super.onStart()
        val options = FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(mFriendsDatabaseReference, Friends::class.java)
                .setLifecycleOwner(this)
                .build()

        val adapter= object: FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options){

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
                val view=LayoutInflater.from(context).inflate(R.layout.user_single_layout,parent,false)
                return FriendsViewHolder(view)
            }

            override fun onBindViewHolder(holder: FriendsViewHolder, position: Int, model: Friends) {

                val userId=getRef(position).key
                holder.uID=userId


                val listUserId=getRef(position).key
                mUsersDatabaseReference.child(listUserId).addValueEventListener(object: ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        var userName=p0!!.child("name").value.toString()
                        var userThumbImage=p0!!.child("thumb_image").value.toString()
                        var userOnline=((p0!!.child("online").value.toString())=="true")
                        holder.chatUserName=userName
                        holder.bind(model,userName,userThumbImage,userOnline )
                    }
                })
            }

        }
        friends_list.adapter=adapter

    }

    inner class FriendsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView),View.OnClickListener{
        lateinit var uID:String
        lateinit var chatUserName:String
        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            val options = arrayOf<CharSequence>("Open profile", "Send message")

            var builder=AlertDialog.Builder(context!!)
                    .setTitle("Select options")
                    .setItems(options, DialogInterface.OnClickListener { dialog, which ->

                        when(which){
                            0->{
                                val intent = Intent(context, ProfileActivity::class.java)
                                intent.putExtra("USER_KEY", uID)
                                startActivity(intent)
                            }
                            1->{
                                val intent = Intent(context, ChatActivity::class.java)
                                intent.putExtra("USER_KEY", uID)
                                intent.putExtra("USER_NAME_APP",chatUserName)
                                startActivity(intent)
                            }
                        }

                    }).show()
        }



        fun bind(friend: Friends,name:String,thumbImage:String,online:Boolean){
            val nameTextView: TextView =itemView.findViewById(R.id.user_name)
            val statusTextView: TextView =itemView.findViewById(R.id.user_status)
            val image: CircleImageView =itemView.findViewById(R.id.user_image)
            val icon:ImageView=itemView.findViewById(R.id.user_online_icon)

            if(online){icon.visibility=View.VISIBLE} else {icon.visibility=View.INVISIBLE}


            statusTextView.text=friend.date
            nameTextView.text=name
            Picasso.get().load(thumbImage).placeholder(R.drawable.default_person).into(image)

        }

    }

}

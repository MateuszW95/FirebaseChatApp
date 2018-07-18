package com.mwdevp.android.lapitchat.Fragments


import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mwdevp.android.lapitchat.Activities.ChatActivity
import com.mwdevp.android.lapitchat.Models.Conv

import com.mwdevp.android.lapitchat.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class ChatsFragment : Fragment() {

    private lateinit var mConvList:RecyclerView
    private  lateinit var mConvDatabaseReference: DatabaseReference
    private lateinit var mMessageDatabaseReference: DatabaseReference
    private  lateinit var mUserDatabaseReference: DatabaseReference

    private lateinit var  mAuth:FirebaseAuth
    private  lateinit var mCurrentUser:String
    private lateinit var mMainView:View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
       mMainView=inflater.inflate(R.layout.fragment_chats, container, false)

        mConvList=mMainView.findViewById(R.id.conv_list)
        mAuth=FirebaseAuth.getInstance()
        mCurrentUser=mAuth.currentUser!!.uid

        mConvDatabaseReference=FirebaseDatabase.getInstance().reference.child("chat")
                .child(mCurrentUser)

        mUserDatabaseReference=FirebaseDatabase.getInstance().reference.child("users")
        mMessageDatabaseReference=FirebaseDatabase.getInstance().reference.child("messages")
                .child(mCurrentUser)

        val linearLayoutManager=LinearLayoutManager(context)
        linearLayoutManager.reverseLayout=true
        linearLayoutManager.stackFromEnd=true

        mConvList.setHasFixedSize(true)
        mConvList.layoutManager=linearLayoutManager
        return mMainView
    }

    override fun onStart() {
        super.onStart()

        val query = mConvDatabaseReference.orderByChild("timestamp")

        val options = FirebaseRecyclerOptions.Builder<Conv>()
                .setQuery(query, Conv::class.java)
                .setLifecycleOwner(this)
                .build()

        val adapter = object : FirebaseRecyclerAdapter<Conv, ConvViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ConvViewHolder {
                var view = LayoutInflater.from(context).inflate(R.layout.user_single_layout, parent, false)
                return ConvViewHolder(view)
            }


            override fun onBindViewHolder(holder: ConvViewHolder, position: Int, model: Conv) {

                val listUserId=getRef(position).key
                val lastMessageQuery=mMessageDatabaseReference.child(listUserId).limitToLast(1)

                lastMessageQuery.addChildEventListener(object: ChildEventListener{
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onChildMoved(p0: DataSnapshot?, p1: String?) {

                    }

                    override fun onChildChanged(p0: DataSnapshot?, p1: String?) {

                    }

                    override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                        var data=p0!!.child("message").value.toString()
                        if(data.length>25){
                            data=data.take(25)+"..."
                        }
                        holder.setMessage(data,model.seen)
                    }

                    override fun onChildRemoved(p0: DataSnapshot?) {

                    }
                })

                mUserDatabaseReference.child(listUserId).addValueEventListener(object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        val userName=p0!!.child("name").value.toString()
                        val userThumb=p0!!.child("thumb_image").value.toString()

                        if(p0!!.hasChild("online")){
                            val userOnline=p0!!.child("online").value.toString()
                            holder.setUserOnline(userOnline)
                        }

                        holder.setName(userName)
                        holder.setUserImage(userThumb)

                        holder.itemView.setOnClickListener {
                            val intent=Intent(context,ChatActivity::class.java)
                            intent.putExtra("USER_KEY",listUserId)
                            intent.putExtra("USER_NAME_APP",userName)
                            startActivity(intent)
                        }
                    }
                })

            }


        }
        mConvList.adapter=adapter
    }

    inner class ConvViewHolder(itemView:View): RecyclerView.ViewHolder(itemView) {

        fun setMessage(message:String,isSeen:Boolean){
            val userStatusView=itemView.findViewById<TextView>(R.id.user_status)
            userStatusView.text=message
            if(!isSeen){
                userStatusView.setTypeface(userStatusView.typeface,Typeface.BOLD)
            }else{
                userStatusView.setTypeface(userStatusView.typeface, Typeface.NORMAL)
            }

        }
        fun setName(name:String){
            val userNameView=itemView.findViewById<TextView>(R.id.user_name)
            userNameView.text=name
        }

        fun setUserImage(t:String){
            val userImageView=itemView.findViewById<CircleImageView>(R.id.user_image)
            Picasso.get().load(t).placeholder(R.drawable.default_person).into(userImageView)
        }
        fun setUserOnline(s:String){
            val imageView=itemView.findViewById<ImageView>(R.id.user_online_icon)
            if(s=="true"){
                imageView.visibility=View.VISIBLE
            }
            else{
                imageView.visibility=View.VISIBLE
            }
        }

    }


}

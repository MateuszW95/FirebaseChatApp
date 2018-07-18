package com.mwdevp.android.lapitchat.Adapters

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mwdevp.android.lapitchat.Models.Messages
import com.mwdevp.android.lapitchat.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.message_single_layout.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(val mMessageList:ArrayList<Messages>): RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    private var mUsersDatabaseReference=FirebaseDatabase.getInstance().reference.child("users")
    private  var mAuth:FirebaseAuth=FirebaseAuth.getInstance()
    override fun getItemCount(): Int {
        return mMessageList.size
    }


    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val currnetUser=mAuth.currentUser!!.uid
        val from=mMessageList[position].from
        mUsersDatabaseReference.child(from).addValueEventListener(object: ValueEventListener {

            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                var userName=p0!!.child("name").value.toString()
                var userThumbImage=p0!!.child("thumb_image").value.toString()
                holder.image=userThumbImage
                holder.name=userName
                holder.bind(mMessageList[position])
            }
        })







    }

    override fun getItemViewType(position: Int): Int {
         if(mMessageList[position].from==mAuth.currentUser!!.uid){
             if(mMessageList[position].type=="text") {
                 return 1
             }
             else{
                 return 2
             }
         }
        else{
             if(mMessageList[position].type=="text") {
                 return 0
             }
             else{
                 return 3
             }
         }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view: View = when (viewType) {
            0->{ LayoutInflater.from(parent.context).inflate(R.layout.message_single_layout, parent, false) }
            1-> { LayoutInflater.from(parent.context).inflate(R.layout.message_single_layout_a, parent, false)}
            2->{ LayoutInflater.from(parent.context).inflate(R.layout.message_image_layout, parent, false)}
            else-> { LayoutInflater.from(parent.context).inflate(R.layout.message_single_layout_image_a, parent, false)}
    }
        return MessageViewHolder(view,parent.context)
    }

    inner class MessageViewHolder(view:View,val c:Context):RecyclerView.ViewHolder(view){
        val messageTextView:TextView? = view.findViewById<TextView>(R.id.message_text_layout)
        val profileImage:CircleImageView? = view.findViewById<CircleImageView>(R.id.message_profile_layout)
        val timeTextView=view.findViewById<TextView>(R.id.message_time_view)
        val nameTextView=view.findViewById<TextView>(R.id.message_display_name)
        val imageView=view.findViewById<ImageView?>(R.id.message_image_layout)
        lateinit var image:String
        lateinit var name:String

        fun bind(m:Messages){

            timeTextView.text=SimpleDateFormat("yyyy.MM.dd HH:mm ").format(Date(m.time))
            nameTextView.text=name
            if(profileImage!=null) {
                Picasso.get().load(image).placeholder(R.drawable.default_person).into(profileImage)
            }
            if(messageTextView!=null){
                messageTextView.text=m.message
            }
            if(imageView!=null){
                Picasso.get().load(m.thumb).placeholder(R.drawable.default_person).into(imageView)
            }


        }

    }
}
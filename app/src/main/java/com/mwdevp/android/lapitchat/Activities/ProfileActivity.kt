package com.mwdevp.android.lapitchat.Activities

import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.mwdevp.android.lapitchat.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import java.text.DateFormat
import java.util.*
import kotlin.collections.HashMap

class ProfileActivity : AppCompatActivity() {

    private lateinit var mUsersDatabase: DatabaseReference
    private lateinit var mProgressDialog: ProgressDialog
    private lateinit var mCurrentState: String
    private lateinit var mCurrentUser: FirebaseUser
    private lateinit var mFiendDatabaseReference: DatabaseReference
    private lateinit var mNotificationDatabase: DatabaseReference
    private lateinit var mFriendRequestDatabaseReference: DatabaseReference
    private lateinit var mRootReference: DatabaseReference
    var displayName:String=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val uID = intent.getStringExtra("USER_KEY")
        mUsersDatabase = FirebaseDatabase.getInstance().reference.child("users").child(uID)
        mFriendRequestDatabaseReference = FirebaseDatabase.getInstance().reference.child("friend_req")
        mCurrentUser = FirebaseAuth.getInstance().currentUser!!
        mFiendDatabaseReference = FirebaseDatabase.getInstance().reference.child("friends")
        mNotificationDatabase=FirebaseDatabase.getInstance().reference.child("notifications")
        mRootReference=FirebaseDatabase.getInstance().reference
        //offline
//        mFiendDatabaseReference.keepSynced(true)
//        mFriendRequestDatabaseReference.keepSynced(true)

        mCurrentState = "not_friends"
        mProgressDialog = ProgressDialog(this@ProfileActivity)
        mProgressDialog.setTitle("Loading user data")
        mProgressDialog.setMessage("Please wait while we load user data")
        mProgressDialog.setCanceledOnTouchOutside(false)
        mProgressDialog.show()

        profile_decline_button.visibility= View.INVISIBLE
        profile_decline_button.isEnabled=false

        mUsersDatabase.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(p0: DataSnapshot?) {
                displayName = p0!!.child("name").value.toString()
                val status = p0!!.child("status").value.toString()
                val image = p0!!.child("image").value.toString()

                profile_name_textView.text = displayName
                profile_status_textView.text = status
                Picasso.get().load(image).placeholder(R.drawable.default_person).into(profile_image)
                //----------------------FRIEND LIST REQ/STATE---------------------------
                mFriendRequestDatabaseReference.child(mCurrentUser.uid).addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                         if (p0!!.hasChild(uID)) {
                            val reqType = p0!!.child(uID).child("request_type").value.toString()
                            if (reqType == "received") {
                                profile_request_button.isEnabled = true
                                mCurrentState = "req_received"
                                profile_request_button.text = "Accept friend request"
                                profile_decline_button.visibility= View.VISIBLE
                                profile_decline_button.isEnabled=true
                            } else if (reqType == "sent") {
                                mCurrentState = "req_sent"
                                profile_request_button.text = "Cancel friend request"
                                profile_decline_button.visibility= View.INVISIBLE
                                profile_decline_button.isEnabled=false
                            }
                             else{
                                mCurrentState = "not_friends"
                                profile_request_button.text = "Send friend request"
                                profile_decline_button.visibility= View.INVISIBLE
                                profile_decline_button.isEnabled=false
                            }
                            mProgressDialog.dismiss()
                        } else
                        {
                            mFiendDatabaseReference.child(mCurrentUser.uid).addValueEventListener(object :ValueEventListener{
                                override fun onCancelled(p0: DatabaseError?) {
                                    mProgressDialog.dismiss()
                                }

                                override fun onDataChange(p0: DataSnapshot?) {
                                    if(p0!!.hasChild(uID)){
                                        mCurrentState = "friends"
                                        profile_request_button.text = "Unfriend this person"

                                        profile_decline_button.visibility= View.INVISIBLE
                                        profile_decline_button.isEnabled=false
                                    }
                                    else{
                                        mCurrentState = "not_friends"
                                        profile_request_button.text = "Send friend request"
                                        profile_decline_button.visibility= View.INVISIBLE
                                        profile_decline_button.isEnabled=false
                                    }
                                    mProgressDialog.dismiss()
                                }
                            })
                        }





                    }
                })

            }
        })

        profile_request_button.setOnClickListener {


            //----------------------NOT FRIENDS STATE----------------
            if (mCurrentState == "not_friends") {

                val newNotificationRef=mRootReference.child("notifications").child(uID).push()
                val newNotificationId=newNotificationRef.key

                val notificationData=HashMap<String,String>()
                notificationData.put("from",mCurrentUser.uid)
                notificationData.put("type","request")

                val requestMap=HashMap<String,Any>()
                requestMap.put("friend_req/"+mCurrentUser.uid+"/"+uID+"/request_type","sent")
                requestMap.put("friend_req/"+uID+"/"+mCurrentUser.uid+"/request_type","received")
                requestMap.put("notifications/$uID/$newNotificationId",notificationData)


              mRootReference.updateChildren(requestMap as Map<String, Any>?, DatabaseReference.CompletionListener { databaseError, databaseReference ->

                   if(databaseError!=null){
                       Toast.makeText(this@ProfileActivity,"There was some errors in sending request",Toast.LENGTH_LONG).show()
                   }

                  profile_request_button.isEnabled=true
                  mCurrentState = "req_sent"
                  profile_request_button.text = "Cancel friend request"




                })

            }
            //-------------------CANCEL REQUEST STATE-----------------

            if (mCurrentState == "req_sent") {
                mFriendRequestDatabaseReference.child(mCurrentUser.uid).child(uID).removeValue().addOnCompleteListener {
                    if (it.isSuccessful) {
                        mFriendRequestDatabaseReference.child(uID).child(mCurrentUser.uid).removeValue().addOnSuccessListener {
                            profile_request_button.isEnabled = true
                            mCurrentState = "not_friends"
                            profile_request_button.text = "Send friend request"

                            profile_decline_button.visibility= View.INVISIBLE
                            profile_decline_button.isEnabled=false
                        }
                    }
                }
            }

            //---------------REQ RECEIVED STATE-----------

            if (mCurrentState == "req_received") {

                val currentDate = DateFormat.getDateInstance().format(Date()).toString()
                mFriendRequestDatabaseReference.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        if(p0!!.child(uID).hasChild(mCurrentUser.uid) && p0!!.child(mCurrentUser.uid).hasChild(uID)){

                            val map=HashMap<String,Any?>()
                            map.put("friends/${mCurrentUser.uid}/$uID/date",currentDate)
                            map.put("friends/$uID/${mCurrentUser.uid}/date",currentDate)
                            map.put("friend_req/"+mCurrentUser.uid+"/"+uID,null)
                            map.put("friend_req/"+uID+"/"+mCurrentUser.uid,null)

                            mRootReference.updateChildren(map, DatabaseReference.CompletionListener { databaseError, databaseReference ->
                                if(databaseError==null) {
                                    profile_request_button.isEnabled = true
                                    mCurrentState = "friends"
                                    profile_request_button.text = "Unfriend this person"

                                    profile_decline_button.visibility = View.INVISIBLE
                                    profile_decline_button.isEnabled = false
                                }
                            })

                        }else{
                            Toast.makeText(this@ProfileActivity, "Sorry but $displayName canceled request", Toast.LENGTH_LONG).show()
                            mCurrentState = "not_friends"
                            profile_request_button.text = "Send friend request"

                        }
                    }

                })

            }

            if(mCurrentState=="friends"){

                val unfriendMap=HashMap<String,Any?>()

                unfriendMap.put("friends/"+mCurrentUser.uid+"/"+uID+"/date",null)
                unfriendMap.put("friends/"+uID+"/"+mCurrentUser.uid+"/date",null)

                mRootReference.updateChildren(unfriendMap, { databaseError, databaseReference ->
                    if(databaseError==null){
                        mCurrentState = "no_friends"
                        profile_request_button.text = "Send friend request"


                        profile_decline_button.visibility= View.INVISIBLE
                        profile_decline_button.isEnabled=false
                    }else{
                        Toast.makeText(this@ProfileActivity, databaseError.toString(), Toast.LENGTH_LONG).show()

                    }
                    profile_request_button.isEnabled=true;
                })




//                mFiendDatabaseReference.child(mCurrentUser.uid).child(uID).removeValue().addOnSuccessListener {
//
//                    mFiendDatabaseReference.child(uID).child(mCurrentUser.uid).removeValue().addOnSuccessListener {
//                        profile_request_button.isEnabled = true
//                        mCurrentState = "not_friends"
//                        profile_request_button.text = "Send friend request"
//                    }
//
//                }
            }


        }
    }
}

package com.mwdevp.android.lapitchat.Activities

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.CursorLoader
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mwdevp.android.lapitchat.Adapters.MessageAdapter
import com.mwdevp.android.lapitchat.Models.*
import com.mwdevp.android.lapitchat.R
import com.squareup.picasso.Picasso
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chat_custom_bar.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI


class ChatActivity : AppCompatActivity() {

    companion object {
        val TOTAL_ITEMS_TO_LOAD:Int=10
        var mCurrentPage:Int=1
        var IMAGE_REQUEST:Int=55
        val MY_PERMISSIONS_REQUEST_READ_EXTERANAL:Int=53
    }

    private lateinit var mChatUser:String

    private lateinit var mRootDatabaseReference: DatabaseReference
    private lateinit var mAuth:FirebaseAuth
    private lateinit var mCurrentUserId:String
    private  var mMessagesList=ArrayList<Messages>()
    private lateinit var mImageStorage:StorageReference
    private lateinit var mMessagesAdapter:MessageAdapter
    private lateinit var mLinearLayout: LinearLayoutManager
    private lateinit var mMessagesDatabaseReference: DatabaseReference
    private lateinit var mRefreshLayout: SwipeRefreshLayout
    private  lateinit var mProgressDialog: ProgressDialog
    private lateinit var mValueEventListener: ValueEventListener



    private var itemPosition:Int=0
    private var mLastKey:String=""
    private var mPrevKey:String=""

//%%%%%%%%%%%%%%
    private   var mUsersList=ArrayList<String>()
//%%%%%%%%%%%%%

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        mChatUser=intent.getStringExtra("USER_KEY")
        val chatUserName=intent.getStringExtra("USER_NAME_APP")

        setSupportActionBar(chat_bar as Toolbar)
        val actionBar=supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setDisplayShowCustomEnabled(true)

        actionBar!!.title=chatUserName
        mRootDatabaseReference=FirebaseDatabase.getInstance().reference
        mAuth= FirebaseAuth.getInstance()
        mCurrentUserId=mAuth.currentUser!!.uid

        val inflater=getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val actionBarView=inflater.inflate(R.layout.chat_custom_bar,null)

        actionBar!!.customView = actionBarView

        mImageStorage= FirebaseStorage.getInstance().reference
        mMessagesAdapter= MessageAdapter(mMessagesList)
        mLinearLayout= LinearLayoutManager(this@ChatActivity)
        chat_message_list.setHasFixedSize(true)
        chat_message_list.layoutManager= mLinearLayout
        chat_message_list.adapter=mMessagesAdapter

        //Keyboard on screen and we need to scroll our list
        val unregistrar = KeyboardVisibilityEvent.registerEventListener(this@ChatActivity){
            if(it){
                chat_message_list.scrollToPosition(mMessagesList.size-1)
            }
        }
        mRootDatabaseReference.child("chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true)
        loadMessages()

        custom_bar_name.text=chatUserName
        mRootDatabaseReference.child("users").child(mChatUser).addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                val online=p0!!.child("online").value.toString()
                val image=p0!!.child("image").value.toString()

                if(online=="true"){
                    custom_bar_seen.text="Online"
                }else{
                    val gta=GetTimeAgo()
                   custom_bar_seen.text=gta.getTimeAgo(online.toLong(),this@ChatActivity)

                }

                Picasso.get().load(image).placeholder(R.drawable.default_person).into(custom_bar_image)

            }
        })

        //%%%%%%%%%%%%%%
        mRootDatabaseReference.child("users").addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {

            }

            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                var u=p0!!.key

                mUsersList.add(u)
            }

            override fun onChildRemoved(p0: DataSnapshot?) {

            }
        } )
        //%%%%%%%%%%%%%%


        mValueEventListener=object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if(!p0!!.hasChild(mChatUser)){


                    val chatAddMap=HashMap<String,Any?>()
                    chatAddMap.put("seen",false)
                    chatAddMap.put("timestamp",ServerValue.TIMESTAMP)

                    val chatUserMap=HashMap<String,Any?>()
                    chatUserMap.put("chat/$mCurrentUserId/$mChatUser",chatAddMap)
                    chatUserMap.put("chat/$mChatUser/$mCurrentUserId",chatAddMap)

                    mRootDatabaseReference.updateChildren(chatUserMap, DatabaseReference.CompletionListener { databaseError, databaseReference ->
                        //%%%%%%%%%%%%%%
                        var HM=HashMap<String,Any?>()
                        for(i in mUsersList){

                            if(i!=mCurrentUserId && i!=mChatUser){
                                HM.put("chat/$mCurrentUserId/$mChatUser/users/$i/_id",i)

                            }
                            HM.put("notification_mes/$mCurrentUserId/$mChatUser/time",ServerValue.TIMESTAMP)
                            mRootDatabaseReference.updateChildren(HM)
                        }
                        //%%%%%%%%%%%%%%
                        if(databaseError!=null){
                            Log.e("CHAT_ERROR_LOG",databaseError.toString())
                        }
                    })

                }
                else
                {
                    mRootDatabaseReference.child("chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true)
                    //%%%%%%%%%%%%%%
                    var HM=HashMap<String,Any?>()
                    HM.put("_id",null)
                    HM.put("t",null)
                    mRootDatabaseReference.child("chat").child(mCurrentUserId).child(mChatUser).child("users").child(mCurrentUserId).updateChildren(HM)


                    //%%%%%%%%%%%%%%%
                }
            }
        }
        mRootDatabaseReference.child("chat").child(mCurrentUserId).addValueEventListener(mValueEventListener)


        chat_send_button.setOnClickListener {

            sendMessage()
        }

        message_swipe_layout.setOnRefreshListener {
            mCurrentPage++
            itemPosition=0
            loadMoreMessages()
        }

        chat_add_button.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this@ChatActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this@ChatActivity,
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this@ChatActivity,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERMISSIONS_REQUEST_READ_EXTERANAL)

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }

            }
            else {
                val i = Intent()
                i.type = "image/*"
                i.action = Intent.ACTION_PICK
                startActivityForResult(Intent.createChooser(i, "Select image"), IMAGE_REQUEST)
            }


        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode== IMAGE_REQUEST && resultCode== Activity.RESULT_OK){
            mProgressDialog=ProgressDialog(this@ChatActivity)
            mProgressDialog.setTitle("Uploading image...")
            mProgressDialog.setMessage("Please wait while we upload your image")
            mProgressDialog.setCanceledOnTouchOutside(false)
            mProgressDialog.show()
            val uri=data!!.data

            val current_user_ref="messages/$mCurrentUserId/$mChatUser"
            val chat_user_ref="messages/$mChatUser/$mCurrentUserId"

            val userMessagePush=mRootDatabaseReference.child("messages").child(mCurrentUserId).child(mChatUser).push()

            val pushId=userMessagePush.key
            val filePath=mImageStorage.child("messages_images").child("$pushId.jpg")

            filePath.putFile(uri).addOnCompleteListener {
                if(it.isSuccessful){
                    val downloadUrl=it.result.downloadUrl.toString()

                    val pathA:String
            val cursor = contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DISPLAY_NAME,MediaStore.Images.ImageColumns.DATA ), null, null, null)

                    if (cursor != null) {
                        val columnName = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                        val columnData=cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA)
                        cursor.moveToFirst();
                       val nameFile = cursor.getString(columnName);
                        cursor.moveToFirst();
                        val pathB= cursor.getString(columnData);
                        cursor.close();
                        pathA= pathB

                    } else
                         pathA= uri.getPath();

                    val thumbFilePathTmp =File(pathA)

                    val thumbBitmap= Compressor(this@ChatActivity)
                            .setMaxWidth(100)
                            .setMaxHeight(100)
                            .setQuality(75)
                            .compressToBitmap(thumbFilePathTmp)
                    //upload bitmap to firebase
                    val baos = ByteArrayOutputStream()
                    thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val thumbByte = baos.toByteArray()


                    val thumbFilePath=mImageStorage.child("messages").child("thumbs").child("$pushId.jpg")
                    val uploadTask = thumbFilePath.putBytes(thumbByte)
                    uploadTask.addOnCompleteListener {
                        if(it.isSuccessful){
                            val thumbUrl=it.result.downloadUrl.toString()
                            val messageMap=HashMap<String,Any?>()
                            messageMap.put("message",downloadUrl)
                            messageMap.put("seen",false)
                            messageMap.put("type","image")
                            messageMap.put("thumb",thumbUrl)
                            messageMap.put("time",ServerValue.TIMESTAMP)
                            messageMap.put("from",mCurrentUserId)

                            chat_message.text.clear()


                            val messageUserMap=HashMap<String,Any?>()
                            messageUserMap.put("$current_user_ref/$pushId",messageMap)
                            messageUserMap.put("$chat_user_ref/$pushId",messageMap)

                            mRootDatabaseReference.child("chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true)

                            mRootDatabaseReference.child("chat").child(mCurrentUserId).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP)

                            mRootDatabaseReference.child("chat").child(mChatUser).child(mCurrentUserId).child("seen").setValue(false)
                            mRootDatabaseReference.child("chat").child(mChatUser).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP)

                            mRootDatabaseReference.updateChildren(messageUserMap, DatabaseReference.CompletionListener { databaseError, databaseReference ->
                                if(databaseError!=null){
                                    Log.e("CHAT_ERROR_LOG",databaseError.toString())

                                }
                                mProgressDialog.dismiss()


                            })
                        }
                    }


                }
            }
        }
    }

    private fun loadMoreMessages() {
        val messageRef=mRootDatabaseReference.child("messages").child(mCurrentUserId).child(mChatUser)
        val query=messageRef.orderByKey().endAt(mLastKey).limitToLast(TOTAL_ITEMS_TO_LOAD)

        query.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                val m=p0!!.getValue(Messages::class.java) as Messages

                var messageKey=p0!!.key

                if(mPrevKey!=messageKey){
                    mMessagesList.add(itemPosition++,m)
                }else
                {
                    mPrevKey=mLastKey
                }

                if(itemPosition==1){
                    mLastKey=messageKey
                }




                Log.d("CHAT_KEY","last key: $mLastKey | prevKey: $mPrevKey | currentKey: $messageKey")

                mMessagesAdapter.notifyDataSetChanged()


                message_swipe_layout.isRefreshing=false
                message_swipe_layout.scrollTo(itemPosition,0)
            }

            override fun onChildRemoved(p0: DataSnapshot?) {
            }
        })
    }


    private fun loadMessages() {
        val messageRef=mRootDatabaseReference.child("messages").child(mCurrentUserId).child(mChatUser)
        val query=messageRef.limitToLast(mCurrentPage*TOTAL_ITEMS_TO_LOAD)
        query.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {

            }

            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                val m=p0!!.getValue(Messages::class.java) as Messages
                itemPosition++
                if(itemPosition==1){
                    val messageKey=p0!!.key
                    mLastKey=messageKey
                    mPrevKey=messageKey
                }
                mMessagesList.add(m)
                mMessagesAdapter.notifyDataSetChanged()
                chat_message_list.scrollToPosition(mMessagesList.size-1)

                message_swipe_layout.isRefreshing=false
            }

            override fun onChildRemoved(p0: DataSnapshot?) {

            }
        })
    }

    private fun sendMessage() {
        val message=chat_message.text.toString()
        if(!TextUtils.isEmpty(message)){
            val currentUserRef="messages/$mCurrentUserId/$mChatUser"
            val chatUserRef="messages/$mChatUser/$mCurrentUserId"

            val userMessagePush=mRootDatabaseReference.child("messages").child(mCurrentUserId).child(mChatUser).push()
            val pushId=userMessagePush.key

            val messageMap=HashMap<String,Any?>()
            messageMap.put("message",message)
            messageMap.put("seen",false)
            messageMap.put("type","text")
            messageMap.put("time",ServerValue.TIMESTAMP)
            messageMap.put("from",mCurrentUserId)

            chat_message.text.clear()


            val messageUserMap=HashMap<String,Any?>()
            messageUserMap.put("$currentUserRef/$pushId",messageMap)
            messageUserMap.put("$chatUserRef/$pushId",messageMap)

            mRootDatabaseReference.child("chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true)
            //%%%%%%%%%%%%%%
            var HM=HashMap<String,Any?>()
            for(i in mUsersList){
                if(i!=mCurrentUserId && i!=mChatUser){
                    HM.put("chat/$mCurrentUserId/$mChatUser/users/$i/_id",i)

                }
                HM.put("notification_mes/$mCurrentUserId/$mChatUser/time",ServerValue.TIMESTAMP)
            }
            mRootDatabaseReference.updateChildren(HM)



            //%%%%%%%%%%%%%%%
            mRootDatabaseReference.child("chat").child(mCurrentUserId).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP)

            mRootDatabaseReference.child("chat").child(mChatUser).child(mCurrentUserId).child("seen").setValue(false)
            mRootDatabaseReference.child("chat").child(mChatUser).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP)

            mRootDatabaseReference.updateChildren(messageUserMap, DatabaseReference.CompletionListener { databaseError, databaseReference ->
                if(databaseError!=null){
                    Log.e("CHAT_ERROR_LOG",databaseError.toString())
                }

            })


        }
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_EXTERANAL -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

        // Add other 'when' lines to check for other
        // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }

    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        mRootDatabaseReference.child("chat").child(mCurrentUserId).removeEventListener(mValueEventListener)
        super.onDestroy()
    }
}

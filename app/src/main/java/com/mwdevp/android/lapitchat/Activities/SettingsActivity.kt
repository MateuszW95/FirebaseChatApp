package com.mwdevp.android.lapitchat.Activities

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.mwdevp.android.lapitchat.R
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_settings.*
import android.app.ProgressDialog
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

import com.squareup.picasso.Picasso
import id.zelory.compressor.Compressor
import java.io.File
import android.graphics.Bitmap
import android.R.attr.bitmap
import java.io.ByteArrayOutputStream
import android.R.attr.data
import android.app.FragmentManager
import com.google.firebase.storage.UploadTask
import com.mwdevp.android.lapitchat.Fragments.SampleDialogFragment
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy


class SettingsActivity : AppCompatActivity() {

    companion object {
        private const val IMAGE_REQUEST:Int=1
    }



    private  lateinit var mDatabaseReference: DatabaseReference
    private  lateinit var mCurrentUser:FirebaseUser
    private  lateinit var mStorageReference:StorageReference
    private  lateinit var mProgressDialog:ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        mCurrentUser= FirebaseAuth.getInstance().currentUser!!
        mDatabaseReference=FirebaseDatabase.getInstance().reference.child("users").child(mCurrentUser.uid)
        //offline capabilities only for strings
        mDatabaseReference.keepSynced(true)


        mStorageReference=FirebaseStorage.getInstance().reference
        mDatabaseReference.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {
                val name=p0!!.child("name").value.toString()
                val image=p0!!.child("image").value.toString()
                val status=p0!!.child("status").value.toString()
                val thumbImage=p0.child("thumb_image").value.toString()

                settings_name.text=name
                settings_status.text=status
                try {
                    if (image != "default")
                    {
                        //Picasso.get().load(image).placeholder(R.drawable.default_person).into(settings_image)
                        Picasso.get().load(image)
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.default_person)
                                .into(settings_image, object: Callback{
                                    override fun onSuccess() {
                                    }

                                    override fun onError(e: java.lang.Exception?) {
                                        Picasso.get().load(image).placeholder(R.drawable.default_person).into(settings_image)
                                    }
                                })
                    }
                    else
                    {
                        //Picasso.get().load(R.drawable.default_person).into(settings_image)
                        Picasso.get().load(image)
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.default_person)
                                .into(settings_image, object: Callback{
                                    override fun onSuccess() {
                                    }

                                    override fun onError(e: java.lang.Exception?) {
                                        Picasso.get().load(image).placeholder(R.drawable.default_person).into(settings_image)
                                    }
                                })
                    }
                }
                catch (e:Exception) {
                    //Picasso.get().load(R.drawable.default_person).into(settings_image)
                    Picasso.get().load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_person)
                            .into(settings_image, object: Callback{
                                override fun onSuccess() {
                                }

                                override fun onError(e: java.lang.Exception?) {
                                    Picasso.get().load(image).placeholder(R.drawable.default_person).into(settings_image)
                                }
                            })
                }



            }
        })

        settings_change_status_button.setOnClickListener {
            startActivity(Intent(this@SettingsActivity,StatusActivity::class.java))
//            val fm=fragmentManager
//            val dialog=SampleDialogFragment()
//            dialog.show(fm,"DIALOG")

        }

        settings_change_image_button.setOnClickListener {
            val i=Intent()
            i.type="image/*"
            i.action=Intent.ACTION_GET_CONTENT

            startActivityForResult(Intent.createChooser(i,"Select image"), IMAGE_REQUEST)


//            CropImage.activity()
//                    .setGuidelines(CropImageView.Guidelines.ON)
//                    .start(this@SettingsActivity)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode== IMAGE_REQUEST && resultCode== Activity.RESULT_OK){
            val imageURI=data!!.data

            CropImage.activity(imageURI)
                    .setAspectRatio(1,1)
                    .start(this@SettingsActivity)
        }

        if (requestCode === CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            val result = CropImage.getActivityResult(data)

            if (resultCode === Activity.RESULT_OK) {
                mProgressDialog=ProgressDialog(this@SettingsActivity)
                mProgressDialog.setTitle("Uploading image...")
                mProgressDialog.setMessage("Please wait while we upload your image")
                mProgressDialog.setCanceledOnTouchOutside(false)
                mProgressDialog.show()
                val resultUri = result.uri

                val thumbFilePathTmp=File(resultUri.path)

                val thumbBitmap=Compressor(this@SettingsActivity)
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(75)
                        .compressToBitmap(thumbFilePathTmp)
                //upload bitmap to firebase
                val baos = ByteArrayOutputStream()
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val thumbByte = baos.toByteArray()


                val filePath=mStorageReference.child("profile_images").child(mCurrentUser.uid+".jpg")
                val thumbFilePath=mStorageReference.child("profile_images").child("thumbs").child(mCurrentUser.uid+".jpg")


                filePath.putFile(resultUri).addOnCompleteListener{
                    if(it.isSuccessful){
                        val downloadUrl=it.result.downloadUrl.toString()
                        val uploadTask = thumbFilePath.putBytes(thumbByte)
                        uploadTask.addOnCompleteListener{

                            val thumbDownloadUrl=it.result.downloadUrl.toString()
                            if (it.isSuccessful)
                            {
                                val updateHashMap=HashMap<String,String>()
                                updateHashMap.put("image",downloadUrl)
                                updateHashMap.put("thumb_image",thumbDownloadUrl)
                                mDatabaseReference.updateChildren(updateHashMap as Map<String, Any>?).addOnCompleteListener {
                                    if(it.isSuccessful)
                                        mProgressDialog.dismiss()
                                    Toast.makeText(this@SettingsActivity,"Process success",Toast.LENGTH_LONG).show()
                                }
                            }
                            else{
                                Toast.makeText(this@SettingsActivity,"Upload thumbnail error",Toast.LENGTH_LONG).show()
                                mProgressDialog.dismiss()
                            }
                        }



                    }
                    else{
                        Toast.makeText(this@SettingsActivity,"Upload error",Toast.LENGTH_LONG).show()
                        mProgressDialog.dismiss()
                    }
                }
            } else if (resultCode === CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                val error = result.error
            }
        }
    }


}

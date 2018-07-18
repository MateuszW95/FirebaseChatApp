package com.mwdevp.android.lapitchat.Services
import android.support.v4.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.app.NotificationManager
import android.app.NotificationChannel
import android.content.Context
import android.os.Build
import com.mwdevp.android.lapitchat.R
import android.content.Intent
import android.app.PendingIntent
import java.util.*


open class FirebaseMessagingService: FirebaseMessagingService() {

    companion object {
        private  const val N_CHANNEL_ID:String="com.mwdevp.android.lapitchat.CHANEL_LAPITCHAT"
        private  const val N_CHANNEL_NAME:String="CHANEL_LAPITCHAT"
    }
    var mNotificationId:Int=0
    lateinit var mBuilder:NotificationCompat.Builder
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onMessageReceived(p0: RemoteMessage?) {
        createNotificationChannel()
        super.onMessageReceived(p0)

        val notificationTitle=p0!!.notification!!.title
        val notificationMessage=p0!!.notification!!.body
        val clickAction=p0!!.notification!!.clickAction
        val fromUserId=p0!!.data["USER_KEY"]
        val type=p0!!.data["N_TYPE"]

        if(type=="FR") {
            mBuilder = NotificationCompat.Builder(this@FirebaseMessagingService, N_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationMessage)
            val intent = Intent(clickAction)
            intent.putExtra("USER_KEY", fromUserId)
//        intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK
//        intent.flags=Intent.FLAG_ACTIVITY_SINGLE_TOP


            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            mBuilder.setContentIntent(pendingIntent)
            mNotificationId=System.currentTimeMillis().toInt()
        }
        else
        {
            mBuilder = NotificationCompat.Builder(this@FirebaseMessagingService, N_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationMessage)

            mNotificationId=castKetToIntID(fromUserId!!)
        }

        //val mNotificationId:Int=System.currentTimeMillis().toInt()




        val mNotificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(mNotificationId,mBuilder.build())
    }




    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val description = "LapitChat notification Chanel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(N_CHANNEL_ID, N_CHANNEL_NAME, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }


    private fun castKetToIntID(s:String):Int{
        var tmp:Int=0
        for(i in 0..(s.length-1)){
            var a=s.get(i).toInt()
            tmp+=a
        }
        return tmp;
    }



}
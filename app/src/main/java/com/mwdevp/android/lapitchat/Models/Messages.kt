package com.mwdevp.android.lapitchat.Models

class Messages {

    lateinit var message:String
    var seen:Boolean=false
    var time:Long=0
    lateinit var type:String
    lateinit var from:String
    lateinit var thumb:String
}
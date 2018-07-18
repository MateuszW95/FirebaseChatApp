package com.mwdevp.android.lapitchat.Fragments

import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.support.v7.app.AlertDialog

class SampleDialogFragment:DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
                .setTitle("Choose something")
                .setPositiveButton("Ok",null)
                .create()
    }
}
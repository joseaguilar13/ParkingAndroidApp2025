package com.navix.mapboxvainilla.ui.view

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity: AppCompatActivity() {
    fun ThereIsInternetConnection ( context: Context ): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo =  cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }
}
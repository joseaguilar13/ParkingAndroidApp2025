package com.navix.mapboxvainilla.ui.view

import android.app.Application
import com.parse.Parse

class MapBoxVainilla: Application()  {
    override fun onCreate() {
        super.onCreate()
        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId("myAppId")
                .server("http://104.236.240.41:3009/parse/")
                .build()
        )

    }
}
package com.navix.mapboxvainilla

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class PermissionManager {
    companion object {
        const val REQUEST_CODE = 1000

        fun requestPermissions(
            activity: Activity,
            permissions: Array<out String>,
            onPermissionGranted: () -> Unit,
            onPermissionDenied: () -> Unit,
        ) {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    permissions.first(),
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    permissions,
                    REQUEST_CODE,
                )
            } else {
                onPermissionGranted()
            }
        }
    }
}

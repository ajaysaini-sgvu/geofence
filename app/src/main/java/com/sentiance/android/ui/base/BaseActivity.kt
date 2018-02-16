package com.sentiance.android.ui.base

import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    fun requestPermissionsSafely(permissions: Array<String>, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun hasPermission(permission: String): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

}
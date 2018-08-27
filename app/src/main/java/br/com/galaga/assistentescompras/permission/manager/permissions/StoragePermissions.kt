package br.com.galaga.assistentescompras.permission.manager.permissions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

class StoragePermissions(private val permissionCode: Int,
                         private val activity: Activity,
                         private val context: Context) : AppPermissions {
    override fun shouldAskPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED
    }

    override fun askPermission() {
        ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), permissionCode)
    }
}
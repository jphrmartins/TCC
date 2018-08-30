package br.com.galaga.assistentescompras.permission.manager.permissions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

class CameraPermissions(private val context: Context) : AppPermissions {
    override fun permissionType(): String {
        return android.Manifest.permission.CAMERA
    }

    override fun shouldAskPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED
    }
}
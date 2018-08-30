package br.com.galaga.assistentescompras.permission.manager

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import br.com.galaga.assistentescompras.MainActivity
import br.com.galaga.assistentescompras.permission.manager.permissions.AppPermissions

class PermissionAsker(private val activity: Activity,
                      private val permissions: List<AppPermissions>) {
    fun askPermitions() {
        val permissionsName = permissions
                .filter { it.shouldAskPermission() }
                .map { it.permissionType() }
                .toTypedArray()
        if (!permissionsName.isEmpty())
            ActivityCompat.requestPermissions(activity, permissionsName, 0)
    }
}
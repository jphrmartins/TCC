package br.com.galaga.assistentescompras.permission.manager

import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import br.com.galaga.assistentescompras.MainActivity
import br.com.galaga.assistentescompras.permission.manager.permissions.AppPermissions

class PermissionAsker(private val permission: List<AppPermissions>) {
    fun askPermitions() {
        permission.forEach {
            if (it.shouldAskPermission()) it.askPermission()
        }
    }
}
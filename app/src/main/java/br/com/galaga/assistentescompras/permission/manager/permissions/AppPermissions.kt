package br.com.galaga.assistentescompras.permission.manager.permissions

interface AppPermissions {
    fun shouldAskPermission(): Boolean
    fun permissionType() : String
}
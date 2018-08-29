package br.com.galaga.assistentescompras

import android.net.Uri


class Item() : Comparable<Item> {
    var name: String = ""
    var description: String? = null
    var position: Int? = null
    var uuid: String = ""
    var checked: Boolean = false
    var imageUri: String? = null

    constructor(name: String, description: String?, imageUri: String?) : this() {
        this.name = name
        this.description = description
        this.imageUri = imageUri
    }

    override fun compareTo(other: Item): Int {
        return this.checked.compareTo(other.checked)
    }
}
package br.com.galaga.assistentescompras

import android.os.Parcelable


class Item(){
    var name: String = ""
    var description: String? = null
    var position: String? = null
    var uuid: String = ""

    constructor(name: String, description: String?) : this() {
        this.name = name
        this.description = description
    }
}
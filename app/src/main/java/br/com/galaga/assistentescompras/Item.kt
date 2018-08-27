package br.com.galaga.assistentescompras


class Item() : Comparable<Item> {
    var name: String = ""
    var description: String? = null
    var position: Int? = null
    var uuid: String = ""
    var checked: Boolean = false
    var imageUri: String? = null

    constructor(name: String, description: String?) : this() {
        this.name = name
        this.description = description
    }

    override fun compareTo(other: Item): Int {
        return this.checked.compareTo(other.checked)
    }
}
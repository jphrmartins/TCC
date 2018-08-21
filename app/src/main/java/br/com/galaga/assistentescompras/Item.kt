package br.com.galaga.assistentescompras


class Item() : Comparable<Item> {
    var name: String = ""
    var description: String? = null
    var position: String? = null
    var uuid: String = ""
    var checked: Boolean = false

    constructor(name: String, description: String?) : this() {
        this.name = name
        this.description = description
    }

    override fun compareTo(other: Item): Int {
        return this.checked.compareTo(other.checked)
    }
}
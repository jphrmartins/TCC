package br.com.galaga.assistentescompras.domain

class User() {
    var email: String? = null
    var uuid: String? = null
    var family: String? = null

    constructor(uuid: String, email: String, family: String) : this() {
        this.email = email
        this.family = family
        this.uuid = uuid
    }
}
package br.com.galaga.assistentescompras.domain

class User() {
    var email: String? = null
    var senha: String? = null
    var uuid: String? = null
    var family: String? = null

    constructor(uuid: String, email: String, senha: String, family: String) : this() {
        this.email = email
        this.family = family
        this.uuid = uuid
        this.senha = senha
    }
}
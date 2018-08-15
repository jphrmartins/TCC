package br.com.galaga.assistentescompras

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_add_product.*

class AddProductActivity : AppCompatActivity() {

    var database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("listaItens")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_salvar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_salvar -> {
                if (edtNome == null || edtNome.text.trim().isEmpty()) {
                    edtNome.error = "Adicione um valor ao nome"
                } else {
                    if (edtNome.text.length >= 15) {
                        edtNome.error = "Nome deve ter um tamnho menor que 10 caracteres"
                    } else {
                        saveItem(createItem())
                        finish()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun createItem(): Item {
        val nome = edtNome.text.toString().trim().capitalize()
        val description = edtDescricao.text.toString().trim().capitalize()
        return Item(nome, description)
    }

    private fun saveItem(item: Item) {
        val uuid = myRef.push().key
        item.uuid = uuid
        myRef.child(uuid).setValue(item)
    }
}

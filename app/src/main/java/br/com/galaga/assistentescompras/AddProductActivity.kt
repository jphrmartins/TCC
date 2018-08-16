package br.com.galaga.assistentescompras

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_add_product.*

class AddProductActivity : AppCompatActivity() {

    var database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("listaItens")
    val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)
        if (intent.hasExtra("item")) {
            val item = gson.fromJson(intent.getStringExtra("item"), Item::class.java)
            edtNome.setText(item.name)
            edtDescricao.setText(item.description)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_salvar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_salvar -> {
                if (intent.hasExtra("item")) {
                    updateItem(intent.getStringExtra("item"))
                } else {
                    addItem()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateItem(jsonItem: String) {
        val item = gson.fromJson(jsonItem, Item::class.java)
        if (!validadeEdtNome()) {
            edtNome.error = "Adicione um valor a nome de até 15 caracteres"
        } else {
            item.name = getTextEdtName()
            item.description = getTextDescription()
            item.position = null
            myRef.updateChildren(mapOf<String, Item>(item.uuid to item))
            finish()
        }
    }

    private fun addItem() {
        if (!validadeEdtNome()) {
            edtNome.error = "Adicione um valor a nome de até 15 caracteres"
        } else {
            saveItem(createItem())
            finish()
        }
    }

    private fun createItem(): Item {
        val nome = getTextEdtName()
        val description = getTextDescription()
        return Item(nome, description)
    }

    private fun getTextDescription(): String? {
        val text = edtDescricao.text.toString().trim().capitalize()
        if (!text.isEmpty())
            return text
        return null
    }

    private fun getTextEdtName() = edtNome.text.toString().trim().capitalize()

    private fun saveItem(item: Item) {
        myRef.push().key?.let {
            item.uuid = it
            myRef.child(it).setValue(item)
        }
    }

    private fun validadeEdtNome(): Boolean {
        return edtNome.text.trim().isEmpty() || edtNome.text.toString().length < 16
    }
}

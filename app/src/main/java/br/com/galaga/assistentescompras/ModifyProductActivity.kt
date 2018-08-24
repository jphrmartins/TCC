package br.com.galaga.assistentescompras

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import br.com.galaga.assistentescompras.R.id.*
import br.com.galaga.assistentescompras.R.string.salvar
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_modify_product.*

class ModifyProductActivity : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("listaItens")
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_product)
        createSpinner()
        //edtPrice.visibility = View.GONE
        if (intent.hasExtra("item")) {
            val item = gson.fromJson(intent.getStringExtra("item"), Item::class.java)
            edtNome.setText(item.name)
            edtDescription.setText(item.description)
        }
    }

    private fun createSpinner() {
        val list = (1..100)
                .map {
                    it.toString()
                }.toMutableList()
        list.add(0, "Quantidade")
        spinner.adapter = ArrayAdapter(baseContext, android.R.layout.simple_spinner_dropdown_item, list.toList())
        spinner.setOnItemSelectedListener(itemSelected())
    }

    private fun itemSelected(): AdapterView.OnItemSelectedListener {
        return object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        intent
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
        val text = edtDescription.text.toString().trim().capitalize()
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
        return if (!edtNome.text.trim().isEmpty()) edtNome.text.toString().length < 16 else false
    }
}

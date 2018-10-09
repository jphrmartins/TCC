package br.com.galaga.assistentescompras

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import br.com.galaga.assistentescompras.domain.Item
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_modify_product.*
import org.jetbrains.anko.doAsync
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ModifyProductActivity : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val CAMERA_REQUEST_CODE = 1
    private val storageReference = FirebaseStorage.getInstance().getReference()
    private val myRef = database.getReference("listaItens")
    private val gson = Gson()
    private var resultUri: Uri? = null
    private var downloadUri: Task<Uri>? = null
    private var currentPath: String? = null
    private var qtdSelected: Int? = null
    private lateinit var familia: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_product)
        createSpinner()
        image.setOnClickListener { takePicture() }

        familia = intent.getStringExtra("familia")
        edtPrice.visibility = View.GONE
        if (intent.hasExtra("itemUpdate")) {
            val item = gson.fromJson(intent.getStringExtra("itemUpdate"), Item::class.java)
            loadImageIfContainsUrl(item)
            edtName.setText(item.name)
            edtDescription.setText(item.description)
        } else if (intent.hasExtra("itemSelected")) {
            val item = gson.fromJson(intent.getStringExtra("itemSelected"), Item::class.java)
            loadImageIfContainsUrl(item)
            edtName.setText(item.name)
            edtDescription.setText(item.description)
            spinner.visibility = View.VISIBLE
            edtPrice.visibility = View.VISIBLE
        }
    }

    private fun loadImageIfContainsUrl(item: Item?) {
        if (item?.imageUri != null)
            Glide.with(baseContext).load(item.imageUri).into(image)
    }

    private fun createSpinner() {
        val list = (1..100)
                .map {
                    it.toString()
                }.toMutableList()
        list.add(0, "Quantidade")
        spinner.adapter = ArrayAdapter(baseContext, android.R.layout.simple_spinner_dropdown_item, list.toList())
        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                qtdSelected = if (qtdSelected != null) qtdSelected else null
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                qtdSelected = if (position == 0) null else position
            }
        })
        spinner.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_salvar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_salvar -> {
                if (intent.hasExtra("itemUpdate")) {
                    updateItem(intent.getStringExtra("itemUpdate"))
                } else if (intent.hasExtra("itemSelected")) {
                    takeItem(intent.getStringExtra("itemSelected"))
                } else {
                    addItem()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun takeItem(jsonItem: String) {
        val item = gson.fromJson(jsonItem, Item::class.java)
        val isValuesValid = validadeValues()
        if (isValuesValid) {
            item.price = edtPrice.text.toString().toDouble()
            item.quantity = qtdSelected
            item.checked = !item.checked
            myRef.child(familia).updateChildren(mapOf(item.uuid to item))
            finish()
        }
    }

    private fun validadeValues(): Boolean {
        var valid = true
        if (qtdSelected == null) {
            genereteSnackbar("Coloque um valor na quantidade", Snackbar.LENGTH_LONG)
            valid = false
        }
        if (edtPrice.text.toString().trim().isEmpty()) {
            edtPrice.error = "Por favor, insira um valor válido no preço"
            valid = false
        }
        if (!edtPrice.text.trim().toString().isEmpty() && edtPrice.text.toString().toDouble() <= 0) {
            edtPrice.error = "Insira um valor maior que zero"
            valid = false
        }
        return valid
    }

    private fun updateItem(jsonItem: String) {
        val item = gson.fromJson(jsonItem, Item::class.java)
        if (!validadeEdtNome()) {
            edtName.error = "Adicione um valor a nome de até 20 caracteres"
        } else {
            item.name = getTextEdtName()
            item.description = getTextDescription()
            item.position = null
            if (this.downloadUri != null) {
                if (!this.downloadUri?.isComplete!!) {
                    genereteSnackbar("Salvando imagem, por favor aguarde...", Snackbar.LENGTH_LONG)
                } else {
                    item.imageUri = this.resultUri.toString()
                    myRef.child(familia).updateChildren(mapOf<String, Item>(item.uuid to item))
                    finish()
                }
            } else {
                myRef.child(familia).updateChildren(mapOf<String, Item>(item.uuid to item))
                finish()
            }
        }
    }

    private fun addItem() {
        if (!validadeEdtNome()) {
            edtName.error = "Adicione um valor a nome de até 20 caracteres"
        } else {
            if (this.downloadUri != null) {
                if (!this.downloadUri?.isComplete!!) {
                    genereteSnackbar("Salvando imagem, por favor aguarde...", Snackbar.LENGTH_LONG)
                } else {
                    saveItem(createItem())
                    finish()
                }
            } else {
                saveItem(createItem())
                finish()
            }

        }
    }

    private fun createItem(): Item {
        val nome = getTextEdtName()
        val description = getTextDescription()
        val imageUri = if (this.downloadUri != null) resultUri.toString() else null
        return Item(nome, description, imageUri)
    }

    private fun getTextDescription(): String? {
        val text = edtDescription.text.toString().trim().capitalize()
        if (!text.isEmpty())
            return text
        return null
    }

    private fun getTextEdtName() = edtName.text.toString().trim().capitalize()

    private fun saveItem(item: Item) {
        myRef.child(familia).push().key?.let {
            item.uuid = it
            myRef.child(familia).child(it).setValue(item)
        }
    }

    private fun validadeEdtNome(): Boolean {
        return if (!edtName.text.trim().isEmpty()) edtName.text.toString().length < 21 else false
    }

    private fun takePicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val photo = createPhotoFile()
            if (photo != null) {
                val photoUri = FileProvider.getUriForFile(baseContext,
                        "br.com.galaga.assistentescompras.fileprovider", photo)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            }
        }
    }

    private fun createPhotoFile(): File? {
        try {
            return createImage()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    fun createImage(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageName = "JPEG_" + timestamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageName, ".jpg", storageDir)
        currentPath = image.absolutePath
        return image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val file = File(currentPath)
            val uri = Uri.fromFile(file)
            genereteSnackbar("Salvando imagem, por favor, aguarde", Snackbar.LENGTH_LONG)
            val storage = storageReference.child("fotos").child(uri.lastPathSegment)
            doAsync {
                uploadToFirestore(storage, uri)
            }
            Glide.with(baseContext).load(uri).into(image)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun uploadToFirestore(storage: StorageReference, photoUri: Uri) {
        downloadUri = storage.putFile(photoUri).continueWithTask { taskSnapshot ->
            if (!taskSnapshot.isSuccessful) {
                throw taskSnapshot.exception!!
            }
            return@continueWithTask storage.downloadUrl
        }
        resultUri = Tasks.await(downloadUri!!)
        Log.i("URL", resultUri.toString())
        genereteSnackbar("Upload da imagem concluido", Snackbar.LENGTH_SHORT)
    }

    private fun genereteSnackbar(text: String, snackbarTimer: Int) {
        Snackbar.make(constraintLayout, text, snackbarTimer).show()
    }
}

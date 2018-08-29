package br.com.galaga.assistentescompras

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_product)
        createSpinner()
        image.setOnClickListener { takePicture() }

        edtPrice.visibility = View.GONE
        if (intent.hasExtra("itemUpdate")) {
            val item = gson.fromJson(intent.getStringExtra("itemUpdate"), Item::class.java)
            edtNome.setText(item.name)
            edtDescription.setText(item.description)
        } else if (intent.hasExtra("itemSelected")) {
            val item = gson.fromJson(intent.getStringExtra("itemSelected"), Item::class.java)
            edtNome.setText(item.name)
            edtDescription.setText(item.description)
            spinner.visibility = View.VISIBLE
            edtPrice.visibility = View.VISIBLE
        }
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

    private fun takeItem(jsonItem: String) {
        val item = gson.fromJson(jsonItem, Item::class.java)
    }

    private fun validadeValues(): Boolean {
        return qtdSelected != null || edtPrice.text.toString().trim().isEmpty()
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
            if (this.downloadUri != null) {
                if (!this.downloadUri?.isComplete!!) {
                    Toast.makeText(baseContext, "Salvando imagem, por favor aguarde...",
                            Toast.LENGTH_LONG).show()
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
            val bitmap = genererateThumbnail(file)
            Toast.makeText(baseContext, "Uploading Image, please Wait", Toast.LENGTH_LONG).show()
            val storage = storageReference.child("fotos").child(uri.lastPathSegment)
            doAsync {
                uploadToFirestore(storage, uri)
            }
            Glide.with(baseContext).load(uri).into(image)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun genererateThumbnail(file: File): Bitmap {
        return BitmapFactory.decodeFile(file.absolutePath)
    }

    private fun uploadToFirestore(storage: StorageReference, photoUri: Uri) {
        Log.i("URL", "inicio do envio1")
        downloadUri = storage.putFile(photoUri).continueWithTask { taskSnapshot ->
            if (!taskSnapshot.isSuccessful) {
                throw taskSnapshot.exception!!
            }
            Log.i("URL", "inicio do envio3")
            return@continueWithTask storage.downloadUrl
        }
        Log.i("URL", "inicio do envio2")
        resultUri = Tasks.await(downloadUri!!)
        Log.i("URL", resultUri.toString())
    }
}

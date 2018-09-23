package br.com.galaga.assistentescompras

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import br.com.galaga.assistentescompras.domain.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    private val myAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val myUserRef = database.getReference("users")
    private val myListaRef = database.getReference("listaItens")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_login, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_login -> {
                if (edtEmail.text.isEmpty() || edtSenha.text.isEmpty() || edtFamilia.text.isEmpty()) {
                    setErrorsMessage()
                } else {
                    registerUser()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setErrorsMessage() {
        if (edtSenha.text.isEmpty()) {
            edtSenha.error = "Insira sua senha"
        }
        if (edtEmail.text.isEmpty()) {
            edtEmail.error = "Insira seu email"
        }
        if (edtFamilia.text.isEmpty()) {
            edtFamilia.error = "Insira o nome da familia"
        }
    }

    private fun registerUser() {
        hideKeyboard()
        val email = edtEmail.text.toString()
        val senha = edtSenha.text.toString()
        val familia = edtFamilia.text.toString().trim().toLowerCase()

        myListaRef.orderByKey().equalTo(familia).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    createUser(familia, email, senha)
                } else {
                    Snackbar.make(constraitLayout, "$familia não existe, deseja criar?", Snackbar.LENGTH_INDEFINITE)
                            .setAction("ADICIONAR", View.OnClickListener {
                                myListaRef.child(familia)
                                createUser(familia, email, senha)
                            }).show()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }

    private fun hideKeyboard() {
        edtEmail.onEditorAction(EditorInfo.IME_ACTION_DONE)
        edtSenha.onEditorAction(EditorInfo.IME_ACTION_DONE)
        edtFamilia.onEditorAction(EditorInfo.IME_ACTION_DONE)
    }

    private fun createUser(familia: String, email: String, senha: String) {
        myAuth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener {
            if (it.isSuccessful) {
                val uuid = myAuth.currentUser!!.uid
                myUserRef.child(uuid).setValue(
                        User(uuid, email, senha, familia)
                )
                openMainActvity(familia)
            } else {
                Snackbar.make(constraitLayout, "Erro ao criar usuario, por favor, tente mais tarde", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun openMainActvity(familia: String) {
        val intent = Intent(baseContext, MainActivity::class.java)
        intent.putExtra("familia", familia)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}

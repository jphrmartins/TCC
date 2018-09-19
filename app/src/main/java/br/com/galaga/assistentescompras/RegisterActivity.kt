package br.com.galaga.assistentescompras

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
                    edtSenha.error = "Insira sua senha"
                    edtEmail.error = "Insira seu email"
                    edtFamilia.error = "Insira o nome da familia"
                } else {
                    registerUser()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun registerUser() {
        val email = edtEmail.text.toString()
        val senha = edtSenha.text.toString()
        val familia = edtFamilia.text.toString()

        myListaRef.orderByKey().equalTo(familia).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    
                } else {
                    Snackbar.make(constraitLayout, "$familia não existe, deseja criar?", Snackbar.LENGTH_INDEFINITE)
                            .setAction("ADICIONAR", View.OnClickListener {
                                myListaRef.child(familia)
                            }).show()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        myAuth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener {
            if (it.isSuccessful) {

            } else {

            }
        }


    }
}

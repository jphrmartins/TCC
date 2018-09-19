package br.com.galaga.assistentescompras

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private val myAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        txtRegister.setOnClickListener {
            val intent = Intent(baseContext, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_login, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_login -> {
                if (edtPassword.text.isEmpty() || edtEmail.text.isEmpty()) {
                    edtPassword.error = "Insira sua senha"
                    edtEmail.error = "Insira seu email"
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
        val senha = edtPassword.text.toString()
        myAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(baseContext, "VER COMO FAZER PARA PEGAR OS DADOS DA FAMILIA", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(baseContext, "Usuario inexistente, por favor, cadastre-se", Toast.LENGTH_LONG).show()
                    }
                }
    }

//    fun registerUser() {
//        val text = edtEmail.text.toString()
//        myRef.orderByKey().equalTo("abacaxi").addListenerForSingleValueEvent(object: ValueEventListener{
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                Log.i("DEEEEEBUUUUUUGG", dataSnapshot.exists().toString())
//            }
//
//            override fun onCancelled(p0: DatabaseError) {
//
//            }
//        })
//    }
}

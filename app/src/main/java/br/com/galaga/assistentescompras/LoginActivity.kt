package br.com.galaga.assistentescompras

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import br.com.galaga.assistentescompras.domain.User
import br.com.galaga.assistentescompras.permission.manager.PermissionAsker
import br.com.galaga.assistentescompras.permission.manager.permissions.CameraPermissions
import br.com.galaga.assistentescompras.permission.manager.permissions.StoragePermissions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private val myAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val myUserRef = database.getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askPermissions()
        setContentView(R.layout.activity_login)
        txtRegister.setOnClickListener {
            val intent = Intent(baseContext, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun askPermissions() {
        PermissionAsker(this, listOf(
                CameraPermissions(baseContext),
                StoragePermissions(baseContext)
        )).askPermitions()
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
                    logingUser()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logingUser() {
        val email = edtEmail.text.toString()
        val senha = edtPassword.text.toString()
        myAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        myUserRef.orderByKey().equalTo(myAuth.currentUser?.uid)
                                .addListenerForSingleValueEvent(openMainActivity())
                    } else {
                        Toast.makeText(baseContext, "Usuario inexistente, por favor, cadastre-se", Toast.LENGTH_LONG).show()
                    }
                }
    }

    private fun openMainActivity(): ValueEventListener {
        return object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                val intent = Intent(baseContext, MainActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("familia", user?.family)
                startActivity(intent)
                finish()
            }
        }
    }
}

package br.com.galaga.assistentescompras

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import br.com.galaga.assistentescompras.adapter.MarketListAdapter
import br.com.galaga.assistentescompras.adapter.SwipeHandler
import br.com.galaga.assistentescompras.domain.Item
import br.com.galaga.assistentescompras.domain.User
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.doAsync


class MainActivity : AppCompatActivity() {

    var database = FirebaseDatabase.getInstance()
    val myListRef = database.getReference("listaItens")
    val myUserRef = database.getReference("users")
    val myStorage = FirebaseStorage.getInstance().getReference()
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    lateinit var adapter: MarketListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
    }

    override fun onStart() {
        if (mAuth.currentUser == null) {
            val intent = Intent(baseContext, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val uuid = mAuth.currentUser!!.uid
            setOnStartExecution(uuid)
        }
        super.onStart()
    }

    private fun setOnStartExecution(uuid: String) {
        myUserRef.orderByKey().equalTo(uuid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.child(uuid).getValue(User::class.java)
                        val family = user?.family!!
                        adapter = MarketListAdapter(this@MainActivity, family)
                        val recyclerView = recyclerView
                        recyclerView.adapter = adapter
                        val layoutManager = LinearLayoutManager(baseContext, LinearLayoutManager.VERTICAL, false)
                        recyclerView.layoutManager = layoutManager
                        recyclerView.addItemDecoration(DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL))

                        val swipeHandler = object : SwipeHandler() {
                            override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
                                removeItem(viewHolder, family)
                            }
                        }
                        val itemTouchHelper = ItemTouchHelper(swipeHandler)
                        itemTouchHelper.attachToRecyclerView(recyclerView)

                        myListRef.child(family).addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val itens = dataSnapshot.children.mapIndexedNotNull { index, dataSnapshot ->
                                    dataSnapshot.getValue(Item::class.java)?.let {
                                        it.position = index + 1
                                        it
                                    }
                                }.sorted()
                                adapter.itens = itens
                                adapter.notifyDataSetChanged()
                                val totalPrice = "R\$ ${calculateTotalPrice(itens)}"
                                price.text = totalPrice
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Failed to read value
                                Log.w("Warning", "Failed to read value.", error.toException())
                            }
                        })

                        fab.setOnClickListener { view ->
                            val intent = Intent(baseContext, ModifyProductActivity::class.java)
                            intent.putExtra("familia", family)
                            startActivity(intent)
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {
                    }
                })
    }

    private fun calculateTotalPrice(itens: List<Item>): String {
        return itens
                .filter { it.checked }
                .map {
                    it.price!! * it.quantity!!
                }
                .sum()
                .toString()
    }

    private fun removeItem(viewHolder: RecyclerView.ViewHolder?, family: String) {
        val item = adapter.itens.get(viewHolder!!.adapterPosition)
        doAsync { deleteImage(item.imageUri) }
        myListRef.child(family).child(item.uuid).setValue(null)
    }

    private fun deleteImage(stringUri: String?) {
        if (stringUri != null) {
            Log.i("StorageURi", stringUri)
            val photoName = Uri.parse(stringUri).lastPathSegment
            Log.i("lastPath", photoName)
            val deleteTask = myStorage.child(photoName).delete().continueWithTask {
                if (!it.isSuccessful) {
                    throw it.exception!!
                }
                return@continueWithTask it
            }
            Tasks.await(deleteTask)
        }
    }
}

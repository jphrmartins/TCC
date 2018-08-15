package br.com.galaga.assistentescompras

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import br.com.galaga.assistentescompras.adapter.MarketListAdapter
import br.com.galaga.assistentescompras.adapter.SwipeDelete
import com.google.firebase.database.*

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    var database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("listaItens")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val recyclerView = recyclerView
        recyclerView.adapter = MarketListAdapter(baseContext)
        val layoutManager = LinearLayoutManager(baseContext, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        val swipeHandler = object : SwipeDelete(recyclerView.adapter as MarketListAdapter){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
                val adapter = recyclerView.adapter as MarketListAdapter
                val user = adapter.itens.get(viewHolder!!.adapterPosition)
                myRef.child(user.uuid).removeValue()
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var counter = 1
                val itens = dataSnapshot.children.mapNotNull {
                    val item = it.getValue(Item::class.java)
                    item?.position = "$counter -"
                    counter++
                    item
                }
                val adapter = MarketListAdapter(itens, baseContext)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Warning", "Failed to read value.", error.toException())
            }
        })

        fab.setOnClickListener { view ->
            val intent = Intent(baseContext, AddProductActivity::class.java)
            startActivity(intent)
        }
    }
}

package br.com.galaga.assistentescompras

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import br.com.galaga.assistentescompras.adapter.MarketListAdapter
import br.com.galaga.assistentescompras.adapter.SwipeHandler
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    var database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("listaItens")
    lateinit var adapter : MarketListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        this.adapter = MarketListAdapter(baseContext, { item: Item -> longClickLisnter(item) })

        val recyclerView = recyclerView
        recyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(baseContext, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        val swipeHandler = object : SwipeHandler(recyclerView.adapter as MarketListAdapter) {
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
                adapter.itens = itens
                adapter.notifyDataSetChanged()
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

    private fun longClickLisnter(item: Item): Boolean {
        val intent = Intent(baseContext, AddProductActivity::class.java)
        val jsonItem = Gson().toJson(item)
        intent.putExtra("item", jsonItem)
        startActivity(intent)
        return true
    }
}

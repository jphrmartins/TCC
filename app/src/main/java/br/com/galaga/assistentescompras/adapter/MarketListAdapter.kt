package br.com.galaga.assistentescompras.adapter

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.galaga.assistentescompras.Item
import br.com.galaga.assistentescompras.ModifyProductActivity
import br.com.galaga.assistentescompras.R
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_component.view.*

class MarketListAdapter(private val context: Context) : Adapter<MarketListAdapter.ViewHolder>() {
    var itens: List<Item> = listOf()
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("listaItens")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.item_component, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return this.itens.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itens[position]
        holder.let {
            it.bindView(item, { longClickListner(item) }, { checkItem(item) })
        }
    }

    private fun checkItem(item: Item) {
        val intent = Intent(context, ModifyProductActivity::class.java)
        val jsonItem = Gson().toJson(item)
        intent.putExtra("itemSelected", jsonItem)
        startActivity(context, intent, null)
    }

    private fun longClickListner(item: Item): Boolean {
        val intent = Intent(context, ModifyProductActivity::class.java)
        val jsonItem = Gson().toJson(item)
        intent.putExtra("itemUpdate", jsonItem)
        startActivity(context, intent, null)
        return true
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(item: Item, longClickListner: (Item) -> Boolean, onCheckItem: (Item) -> Unit) {
            itemView.setOnLongClickListener { longClickListner(item) }

            val title = itemView.txtItemTitle
            val description = itemView.txtItemDescription
            val checkBox = itemView.checkBox
            val position = itemView.txtPosition

            title.text = item.name
            checkBox.isChecked = item.checked
            position.text = formatPosition(item.position)
            description.text = item.description
            description.visibility = if (item.description != null) View.VISIBLE else View.GONE

            checkBox.setOnClickListener { onCheckItem(item) }
        }

        private fun formatPosition(position: Int?): CharSequence? {
            return "$position -"
        }
    }
}
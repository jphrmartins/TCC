package br.com.galaga.assistentescompras.adapter

import android.content.Context
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.galaga.assistentescompras.Item
import br.com.galaga.assistentescompras.R
import kotlinx.android.synthetic.main.item_component.view.*

class MarketListAdapter(private val context: Context, val longClickListner: (Item) -> Boolean) : Adapter<MarketListAdapter.ViewHolder>() {
    var itens: List<Item> = listOf()

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
            it.bindView(item, longClickListner)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(item: Item, longClickListner: (Item) -> Boolean) {
            itemView.setOnLongClickListener { longClickListner(item) }
            val title = itemView.txtItemTitle
            val description = itemView.txtItemDescription
            val checkBox = itemView.checkBox
            val position = itemView.txtPosition

            title.text = item.name
            checkBox.isChecked = item.checked
            position.text = item.position
            description.text = item.description
            description.visibility = if (item.description != null) View.VISIBLE else View.GONE
        }
    }
}
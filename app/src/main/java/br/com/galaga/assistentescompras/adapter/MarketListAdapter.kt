package br.com.galaga.assistentescompras.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import br.com.galaga.assistentescompras.Item
import br.com.galaga.assistentescompras.R
import kotlinx.android.synthetic.main.item_component.view.*

class MarketListAdapter(private val context: Context, val longClickListner: (Item) -> Boolean) : Adapter<MarketListAdapter.ViewHolder>() {
    var itens: List<Item> = listOf()

    constructor(itens: List<Item>, context: Context, longClickListner: (Item) -> Boolean) : this(context, longClickListner) {
        this.itens = itens
    }

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
            position.text = item.position
            description.text = item.description
            if (item.description != null) {
                description.visibility = View.VISIBLE
            }
                   }
    }
}
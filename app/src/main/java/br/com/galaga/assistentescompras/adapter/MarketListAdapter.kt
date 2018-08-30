package br.com.galaga.assistentescompras.adapter

import android.app.Activity
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
import com.bumptech.glide.Glide
import com.google.android.gms.flags.Flag
import com.google.android.gms.flags.impl.FlagProviderImpl
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_component.view.*

class MarketListAdapter(private val activity: Activity) : Adapter<MarketListAdapter.ViewHolder>() {
    var itens: List<Item> = listOf()
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("listaItens")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(activity.baseContext).inflate(R.layout.item_component, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return this.itens.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itens[position]
        holder.let {
            it.bindView(activity.baseContext, item)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(baseContext: Context, item: Item) {
            itemView.setOnLongClickListener { longClickListner(item) }

            val title = itemView.txtItemTitle
            val description = itemView.txtItemDescription
            val checkBox = itemView.checkBox
            val position = itemView.txtPosition
            val image = itemView.image

            title.text = item.name
            checkBox.isChecked = item.checked
            position.text = formatPosition(item.position)
            description.text = item.description
            description.visibility = if (item.description != null) View.VISIBLE else View.GONE

            if (item.imageUri != null) {
                Glide.with(baseContext).load(item.imageUri).into(image)
                image.visibility = View.VISIBLE
            } else {
                image.visibility = View.GONE
            }

            checkBox.setOnClickListener { onCheckItem(item) }
        }

        private fun formatPosition(position: Int?): CharSequence? {
            return "$position -"
        }

        private fun onCheckItem(item: Item) {
            val intent = Intent(activity.baseContext, ModifyProductActivity::class.java)
            if (!item.checked) {
                val jsonItem = Gson().toJson(item)
                intent.putExtra("itemSelected", jsonItem)
                activity.startActivity(intent)
            } else {
                item.checked = !item.checked
                item.price = null
                item.quantity = null
                myRef.updateChildren(mapOf(item.uuid to item))
            }
        }

        private fun longClickListner(item: Item): Boolean {
            val intent = Intent(activity.baseContext, ModifyProductActivity::class.java)
            val jsonItem = Gson().toJson(item)
            intent.putExtra("itemUpdate", jsonItem)
            activity.startActivity(intent)
            return true
        }
    }
}
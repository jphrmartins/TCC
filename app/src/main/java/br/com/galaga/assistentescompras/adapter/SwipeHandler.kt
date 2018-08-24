package br.com.galaga.assistentescompras.adapter

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log

abstract class SwipeHandler(val adapter: MarketListAdapter) :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {


    override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
        return false
    }

    override fun onChildDraw(canvas: Canvas?, recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?,
                             dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        val background = ColorDrawable()

        viewHolder?.let { viewHolder ->
            val itemView = viewHolder.itemView
            Log.d("[O DX]", dX.toString())
            background.color = Color.parseColor("#7ff436")
            background.setBounds(itemView.left + dX.toInt(), itemView.top, itemView.left, itemView.bottom)
            background.draw(canvas)


        }
        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}

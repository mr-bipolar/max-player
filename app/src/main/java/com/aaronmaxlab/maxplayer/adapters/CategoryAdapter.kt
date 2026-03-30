package com.aaronmaxlab.maxplayer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aaronmaxlab.maxplayer.R
import com.aaronmaxlab.maxplayer.subclass.DeviceUtils

class CategoryAdapter(
    private val categories: MutableList<String>,
    private val click: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.VH>() {

    private var selectedPos = 0
    private var isTv : Boolean = false

    fun update(newList: List<String>) {
        categories.clear()
        categories.addAll(newList)
        selectedPos = 0
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        isTv = DeviceUtils.isTv(parent.context)
        return VH(view)
    }

    override fun getItemCount() = categories.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.text.text = categories[position]

        // UI state
        val isSelected = position == selectedPos

        // Android Tv
        if(isTv){
            holder.itemView.setBackgroundResource(
                if (isSelected) R.drawable.bg_cat_tv_active else R.drawable.bg_channel_selector
            )
        }else {
            holder.itemView.setBackgroundResource(
                if (isSelected) R.drawable.bg_cat_active else R.drawable.bg_cat_normal
            )

            holder.text.setTextColor(
                holder.itemView.context.getColor(
                    if (isSelected) R.color.primary else android.R.color.white
                )
            )

        }



        holder.itemView.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

            val oldPos = selectedPos
            selectedPos = pos

            notifyItemChanged(oldPos)
            notifyItemChanged(selectedPos)

            click(categories[pos])
        }
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.catText)
    }
}



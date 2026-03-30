package com.aaronmaxlab.maxplayer.subclass

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class TvRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    override fun focusSearch(focused: View, direction: Int): View? {

        val next = super.focusSearch(focused, direction) ?: return focused

        return next
    }
}
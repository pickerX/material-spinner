package com.pickerx.material.spinner

import android.content.Context
import androidx.recyclerview.widget.RecyclerView

interface ItemRetrieve<T> {
    fun getItem(position: Int): T

    fun getItems(): List<T>
}

internal class MaterialSpinnerAdapterWrapper<T>(
    context: Context,
    private val adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
) : MaterialSpinnerBaseAdapter<T>(context) {

    private lateinit var mRetrieve: ItemRetrieve<T>

    init {
        setItems(mRetrieve.getItems())
    }

    fun setItemRetrieveListener(retrieve: ItemRetrieve<T>) {
        this.mRetrieve = retrieve
    }

    override fun getItemCount(): Int {
        val size = adapter.itemCount
        return if (size == 1 || isHintEnabled) size else size - 1
    }

    override fun getItem(position: Int): T {
        return if (isHintEnabled) {
            mRetrieve.getItem(position)
        } else if (position >= selectedIndex && adapter.itemCount != 1) {
            mRetrieve.getItem(position + 1)
        } else {
            mRetrieve.getItem(position)
        }
    }

    override fun get(position: Int): T {
        return mRetrieve.getItem(position)
    }
}
package com.pickerx.material.spinner

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView

typealias IsSameContent <T> = (T, T) -> Boolean
typealias OnSpinnerItemClick<T> = (View, Int, T) -> Unit

abstract class MaterialSpinnerBaseAdapter<T>(private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var selectedIndex = 0
        private set
    private var textColor = 0
    private var backgroundSelector = 0
    private var paddingTop = 0

    private var paddingLeft = 0
    private var paddingBottom = 0
    private var paddingRight = 0
    private var iconSize = 0

    private var placeHolderDrawable: Drawable? = null

    var isHintEnabled = false

    open var checkSameItem: IsSameContent<T> = { a: T, b: T ->
        a.toString() == b.toString()
    }

    open var onSpinnerClickListener: OnSpinnerItemClick<T>? = null

    private val items: ArrayList<T> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        val itemView = inflater.inflate(R.layout.ms__list_item, parent, false)
        val vh = ViewHolder(itemView)
        vh.itemView.setOnClickListener {
            val position = vh.adapterPosition
            onSpinnerClickListener?.invoke(it, position, getItem(position))
            notifyItemSelected(position)
        }
        if (backgroundSelector != 0) {
            vh.itemView.setBackgroundResource(backgroundSelector)
        }
        vh.itemView.setPadding(
            paddingLeft,
            paddingTop,
            paddingRight,
            paddingBottom
        )
        return vh
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val h = holder as ViewHolder
        h.text.setTextColor(textColor)
        h.text.setPadding(paddingLeft / 2, 0, 0, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val config = context.resources.configuration
            if (config.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                h.text.textDirection = View.TEXT_DIRECTION_RTL
            }
        }
        val item = getItem(position)
        h.text.text = getItemText(item)

        h.icon.visibility = View.GONE

        if (iconSize > 0) {
            val p = h.icon.layoutParams
            p.height = iconSize
            p.width = iconSize
            h.icon.layoutParams = p
        }

        placeHolderDrawable?.let {
            h.icon.visibility = View.VISIBLE
            h.icon.setImageDrawable(it)
        }

        val iconRes = getItemIcon(item)
        if (iconRes != 0) {
            h.icon.visibility = View.VISIBLE
            h.icon.setImageResource(iconRes)
        }

        downloadIcon(item, h.icon)
    }

    /***
     * implement download icon by user
     * default empty implement
     */
    open fun downloadIcon(item: T, imageView: ImageView) {}

    open fun getItemText(position: Int): String = getItem(position).toString()

    abstract fun getItemText(item: T): String

    /**
     * get icon resource as placeholder icon
     */
    abstract fun getItemIcon(item: T): Int

    fun notifyItemSelected(index: Int) {
        selectedIndex = index
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        val size = items.size
        return if (size == 1 || isHintEnabled) size else size - 1
    }

    /**
     * update data when found dirty data
     */
    fun setItems(data: List<T>) {
        val result = diff(items, data) { a, b ->
            checkSameItem.invoke(a, b)
        }
        result.dispatchUpdatesTo(this)
        items.clear()
        items.addAll(data)
    }

    fun getItems() = items

    open fun getItem(position: Int): T {
        items.let {
            return if (isHintEnabled) {
                it[position]
            } else if (position >= selectedIndex && it.size != 1) {
                it[position + 1]
            } else {
                it[position]
            }
        }
    }

    open fun get(position: Int): T {
        return items[position]
    }

    fun setTextColor(@ColorInt textColor: Int) {
        this.textColor = textColor
    }

    fun setBackgroundSelector(@DrawableRes backgroundSelector: Int) {
        this.backgroundSelector = backgroundSelector
    }

    fun setPlaceHolderDrawable(placeHolderDrawable: Drawable?) {
        this.placeHolderDrawable = placeHolderDrawable
    }

    fun setIconSize(iconSize: Int) {
        this.iconSize = iconSize
    }

    fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        paddingLeft = left
        paddingTop = top
        paddingRight = right
        paddingBottom = bottom
    }

    fun selectedItem(): T = items[selectedIndex]

    private class ViewHolder(
        itemView: View,
        val text: TextView = itemView.findViewById(R.id.tv_tinted_spinner),
        val icon: ImageView = itemView.findViewById(R.id.iv_icon)
    ) :
        RecyclerView.ViewHolder(itemView)
}
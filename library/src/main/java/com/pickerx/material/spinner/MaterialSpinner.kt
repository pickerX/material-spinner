package com.pickerx.material.spinner

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupWindow
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * A spinner that shows a [PopupWindow] under the view when clicked.
 * Support icon on the left or right
 */
class MaterialSpinner<T> constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    LinearLayout(context, attrs, defStyleAttr) {

    private var onNothingSelectedListener: OnNothingSelectedListener<T>? = null
    private var onItemSelectedListener: OnItemSelectedListener<T>? = null
    private var mAdapter: MaterialSpinnerBaseAdapter<T>? = null

    /**
     * Get the [PopupWindow].
     *
     * @return The [PopupWindow] that is displayed when the view has been clicked.
     */
    private var mPopupWindow: PopupWindow? = null

    /**
     * Get the [ListView] that is used in the dropdown menu
     *
     * @return the ListView shown in the PopupWindow.
     */
    private var mRecyclerView: RecyclerView? = null

    private var arrowDrawable: Drawable? = null
    private var placeHolderDrawable: Drawable? = null
    private var hideArrow = false
    private var nothingSelected = false

    private var iconSize = 0
    private var popupWindowMaxHeight = 0
    private var popupWindowHeight = 0
    private var selectedIndex = 0
    private var backgroundColor = 0
    private var backgroundSelector = 0
    private var arrowColor = 0
    private var arrowColorDisabled = 0
    private var textColor = 0
    private var hintColor = 0
    private var popupPaddingTop = 0
    private var popupPaddingLeft = 0
    private var popupPaddingBottom = 0
    private var popupPaddingRight = 0

    private var hintText: String? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    private var mIconView: AppCompatImageView? = null
    private val mTextView: AppCompatTextView = AppCompatTextView(context)

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        val ta = context.obtainStyledAttributes(attrs, R.styleable.MaterialSpinner)
        val defaultColor = mTextView.textColors.defaultColor
        val rtl = isRtl(context)

        val paddingLeft: Int
        val paddingTop: Int
        val paddingRight: Int
        val paddingBottom: Int

        val resources = resources
        val defaultPaddingTop = resources.getDimensionPixelSize(R.dimen.px__padding_top)
        var defaultPaddingRight = defaultPaddingTop
        var defaultPaddingLeft = defaultPaddingRight

        if (rtl) {
            defaultPaddingRight = resources.getDimensionPixelSize(R.dimen.px__padding_left)
        } else {
            defaultPaddingLeft = resources.getDimensionPixelSize(R.dimen.px__padding_left)
        }
        val defaultPopupPaddingRight =
            resources.getDimensionPixelSize(R.dimen.px__popup_padding_left)
        val defaultPopupPaddingBottom =
            resources.getDimensionPixelSize(R.dimen.px__popup_padding_top)
        val defaultIconSize = resources.getDimensionPixelSize(R.dimen.px__icon_size)
        try {
            backgroundColor =
                ta.getColor(R.styleable.MaterialSpinner_px_background_color, Color.WHITE)
            backgroundSelector =
                ta.getResourceId(R.styleable.MaterialSpinner_px_background_selector, 0)
            textColor = ta.getColor(R.styleable.MaterialSpinner_px_text_color, defaultColor)
            hintColor = ta.getColor(R.styleable.MaterialSpinner_px_hint_color, defaultColor)
            arrowColor = ta.getColor(R.styleable.MaterialSpinner_px_arrow_tint, textColor)
            hideArrow = ta.getBoolean(R.styleable.MaterialSpinner_px_hide_arrow, false)
            hintText = ta.getString(R.styleable.MaterialSpinner_px_hint) ?: ""

            popupWindowMaxHeight =
                ta.getDimensionPixelSize(R.styleable.MaterialSpinner_px_spinner_max_height, 0)
            popupWindowHeight = ta.getLayoutDimension(
                R.styleable.MaterialSpinner_px_spinner_height,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            paddingTop = ta.getDimensionPixelSize(
                R.styleable.MaterialSpinner_px_padding_top,
                defaultPaddingTop
            )
            paddingLeft = ta.getDimensionPixelSize(
                R.styleable.MaterialSpinner_px_padding_left,
                defaultPaddingLeft
            )
            paddingBottom = ta.getDimensionPixelSize(
                R.styleable.MaterialSpinner_px_padding_bottom,
                defaultPaddingTop
            )
            paddingRight = ta.getDimensionPixelSize(
                R.styleable.MaterialSpinner_px_padding_right,
                defaultPaddingRight
            )
            popupPaddingTop = ta.getDimensionPixelSize(
                R.styleable.MaterialSpinner_px_popup_padding_top,
                defaultPopupPaddingBottom
            )
            popupPaddingLeft = ta.getDimensionPixelSize(
                R.styleable.MaterialSpinner_px_popup_padding_left,
                defaultPopupPaddingRight
            )
            popupPaddingBottom = ta.getDimensionPixelSize(
                R.styleable.MaterialSpinner_px_popup_padding_bottom,
                defaultPopupPaddingBottom
            )
            popupPaddingRight = ta.getDimensionPixelSize(
                R.styleable.MaterialSpinner_px_popup_padding_right,
                defaultPopupPaddingRight
            )
            placeHolderDrawable = ta.getDrawable(R.styleable.MaterialSpinner_px_placeholder)
            iconSize =
                ta.getDimensionPixelSize(R.styleable.MaterialSpinner_px_icon_size, defaultIconSize)

            arrowColorDisabled = lighter(arrowColor, 0.8f)
        } finally {
            ta.recycle()
        }

        mTextView.gravity = Gravity.CENTER_VERTICAL or Gravity.START

        nothingSelected = true
        isClickable = true

        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
        mTextView.setPadding(paddingLeft / 2, 0, 0, 0)

        setBackgroundResource(R.drawable.px__selector)

        if (MIN_SKD_JELLY_BEAN_MR1 && rtl) {
            mTextView.layoutDirection = LAYOUT_DIRECTION_RTL
            mTextView.textDirection = TEXT_DIRECTION_RTL
        }

        updateArrow(rtl)
        initRecycler()
        initSpinnerPopup()

        if (backgroundColor != Color.WHITE) {
            // default color is white
            setBackgroundColor(backgroundColor)
        } else if (backgroundSelector != 0) {
            setBackgroundResource(backgroundSelector)
        }
        if (textColor != defaultColor) {
            setTextColor(textColor)
        }

        // imageView + TextView
        placeHolderDrawable?.let {
            mIconView = AppCompatImageView(context)
            mIconView?.setImageDrawable(it)
            addView(mIconView)

            mIconView?.layoutParams?.width = iconSize
            mIconView?.layoutParams?.height = iconSize
        }
        addView(mTextView)
        mTextView.layoutParams.width = LayoutParams.MATCH_PARENT
    }

    private fun initSpinnerPopup() {
        mPopupWindow = PopupWindow(context).apply {
            contentView = mRecyclerView
            isOutsideTouchable = true
            isFocusable = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                elevation = 16f
                setBackgroundDrawable(getDrawable(context, R.drawable.px__drawable))
            } else {
                setBackgroundDrawable(
                    getDrawable(context, R.drawable.px__spinner_shadow)
                )
            }
            setOnDismissListener {
                if (nothingSelected && onNothingSelectedListener != null) {
                    onNothingSelectedListener!!.onNothingSelected(this@MaterialSpinner)
                }
                if (!hideArrow) {
                    animateArrow(false)
                }
            }
        }
    }

    private fun resetListener() {
        mAdapter?.onSpinnerClickListener = { _: View, index: Int, item: T ->
            onItemSelectedListener?.onItemSelected(this, index, mAdapter!!.getItemId(index), item)
            selectedIndex = index
            nothingSelected = false
            setTextColor(textColor)
            collapse()
        }
        mAdapter?.bindSpinner({ mIconView }, { mTextView })
    }

    private fun initRecycler() {
        mAdapter = MaterialSpinnerAdapter<T>(context).apply {
            setPadding(
                popupPaddingLeft,
                popupPaddingTop,
                popupPaddingRight,
                popupPaddingBottom
            )
            setPlaceHolderDrawable(placeHolderDrawable)
            setBackgroundSelector(backgroundSelector)
            setTextColor(textColor)
        }
        resetListener()

        mRecyclerView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }

//        mRecyclerView!!.onItemClickListener = OnItemClickListener { parent, view, position, id ->
//            var index = position
//            if (index >= selectedIndex && index < adapter!!.count && adapter!!.items!!.size != 1 && TextUtils.isEmpty(
//                    hintText
//                )
//            ) {
//                index++
//            }
//        }
    }

    private fun updateArrow(rtl: Boolean) {
        if (hideArrow) return
        arrowDrawable = getDrawable(context, R.drawable.px__arrow_rotate)?.mutate()
        arrowDrawable?.setColorFilterSrcIn(arrowColor)

        val drawables = mTextView.compoundDrawables
        if (rtl) {
            drawables[0] = arrowDrawable
        } else {
            drawables[2] = arrowDrawable
        }
        mTextView.setCompoundDrawablesWithIntrinsicBounds(
            drawables[0],
            drawables[1],
            drawables[2],
            drawables[3]
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        mPopupWindow!!.width = MeasureSpec.getSize(widthMeasureSpec)
        mPopupWindow!!.height = calculatePopupWindowHeight()
        if (mAdapter != null) {
            val currentText = mTextView.text
            var longestItem = currentText.toString()
            for (i in 0 until mAdapter!!.itemCount) {
                val itemText = mAdapter!!.getItemText(i)
                if (itemText.length > longestItem.length) {
                    longestItem = itemText
                }
            }
            mTextView.text = longestItem
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            mTextView.text = currentText
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            if (isEnabled && isClickable) {
                if (!mPopupWindow!!.isShowing) {
                    expand()
                } else {
                    collapse()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun setBackgroundColor(color: Int) {
        backgroundColor = color
        val background = background
        if (background is StateListDrawable) { // pre-L
            try {
                val getStateDrawable = StateListDrawable::class.java.getDeclaredMethod(
                    "getStateDrawable",
                    Int::class.javaPrimitiveType
                )
                if (!getStateDrawable.isAccessible) getStateDrawable.isAccessible = true
                val colors = intArrayOf(darker(color, 0.85f), color)
                for (i in colors.indices) {
                    val drawable = getStateDrawable.invoke(background, i) as ColorDrawable
                    drawable.color = colors[i]
                }
            } catch (e: Exception) {
                Log.e("MaterialSpinner", "Error setting background color", e)
            }
        } else background?.setColorFilterSrcIn(color)
        mPopupWindow!!.background.setColorFilterSrcIn(color)
    }

    fun setTextColor(color: Int) {
        textColor = color
        mAdapter?.setTextColor(textColor)
        mAdapter?.notifyDataSetChanged()
        mTextView.setTextColor(color)
    }

    fun setHintColor(color: Int) {
        hintColor = color
        mTextView.setTextColor(color)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("state", super.onSaveInstanceState())
        bundle.putInt("selected_index", selectedIndex)
        bundle.putBoolean("nothing_selected", nothingSelected)
        if (mPopupWindow != null) {
            bundle.putBoolean("is_popup_showing", mPopupWindow!!.isShowing)
            collapse()
        } else {
            bundle.putBoolean("is_popup_showing", false)
        }
        return bundle
    }

    override fun onRestoreInstanceState(savedState: Parcelable) {
        if (savedState is Bundle) {
            selectedIndex = savedState.getInt("selected_index")
            nothingSelected = savedState.getBoolean("nothing_selected")
            if (mAdapter != null) {
                mTextView.text = if (nothingSelected && !TextUtils.isEmpty(hintText)) {
                    setHintColor(hintColor)
                    hintText
                } else {
                    setTextColor(textColor)
                    mAdapter?.selectedItem().toString()
                }
                mAdapter?.notifyItemSelected(selectedIndex)
            }
            if (savedState.getBoolean("is_popup_showing")) {
                if (mPopupWindow != null) {
                    // Post the show request into the looper to avoid bad token exception
                    post { expand() }
                }
            }
            val state: Parcelable? = savedState.getParcelable("state")
            super.onRestoreInstanceState(state)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        arrowDrawable?.setColorFilterSrcIn(if (enabled) arrowColor else arrowColorDisabled)
    }

    /**
     * @return the selected item position
     */
    fun getSelectedIndex(): Int {
        return selectedIndex
    }

    /**
     * Set the default spinner item using its index
     *
     * @param position the item's position
     */
    fun setSelectedIndex(position: Int) {
        if (position >= 0 && position <= mAdapter!!.itemCount) {
            mAdapter!!.notifyItemSelected(position)
            selectedIndex = position
            mTextView.text = mAdapter?.getItemText(position)
            mAdapter?.reselectedDrawable(position)
        } else {
            throw IllegalArgumentException("Position must be lower than adapter count!")
        }
    }

    /**
     * Register a callback to be invoked when an item in the dropdown is selected.
     *
     * @param onItemSelectedListener The callback that will run
     */
    fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener<T>?) {
        this.onItemSelectedListener = onItemSelectedListener
    }

    /**
     * Register a callback to be invoked when the [PopupWindow] is shown but the user didn't select an item.
     *
     * @param onNothingSelectedListener the callback that will run
     */
    fun setOnNothingSelectedListener(onNothingSelectedListener: OnNothingSelectedListener<T>?) {
        this.onNothingSelectedListener = onNothingSelectedListener
    }

    /**
     * Set the dropdown items
     *
     * @param items A list of items
     * @param <T> The item type
    </T> */
    fun setItems(vararg items: T) {
        setItems(listOf(*items))
    }

    /**
     * Set the dropdown items
     *
     * @param items A list of items
     * @param <T> The item type
    </T> */
    fun setItems(items: List<T>) {
        mAdapter?.setItems(items)
        setAdapterInternal(mAdapter!!)
    }

    /**
     * Set a custom adapter for the dropdown items
     *
     * @param adapter The list adapter
     */
    fun setAdapter(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
        val retrieve = adapter as ItemRetrieve<T>
        this.mAdapter = MaterialSpinnerAdapterWrapper<T>(context, adapter).apply {
            setPadding(
                popupPaddingLeft,
                popupPaddingTop,
                popupPaddingRight,
                popupPaddingBottom
            )
            setBackgroundSelector(backgroundSelector)
            setTextColor(textColor)
            setItemRetrieveListener(retrieve)
        }
        resetListener()
        setAdapterInternal(this.mAdapter!!)
    }

    /**
     * Set the custom adapter for the dropdown items
     *
     * @param adapter The adapter
     * @param <T> The type
    </T> */
    fun setAdapter(adapter: MaterialSpinnerAdapter<T>) {
        this.mAdapter = adapter.apply {
            setTextColor(textColor)
            setBackgroundSelector(backgroundSelector)
            setPadding(
                popupPaddingLeft,
                popupPaddingTop,
                popupPaddingRight,
                popupPaddingBottom
            )
        }
        resetListener()
        setAdapterInternal(adapter)
    }

    private fun setAdapterInternal(adapter: MaterialSpinnerBaseAdapter<*>) {
        val shouldResetPopupHeight = mRecyclerView!!.adapter != null

        adapter.isHintEnabled = !TextUtils.isEmpty(hintText)
        mRecyclerView!!.adapter = adapter
        if (selectedIndex >= adapter.itemCount) {
            selectedIndex = 0
        }
        if (adapter.getItems().isNotEmpty()) {
            if (nothingSelected && !TextUtils.isEmpty(hintText)) {
                mTextView.text = hintText
                setHintColor(hintColor)
            } else {
                setTextColor(textColor)
                mTextView.text = adapter.selectedItem().toString()
            }
        } else {
            mTextView.text = ""
        }
        if (shouldResetPopupHeight) {
            mPopupWindow!!.height = calculatePopupWindowHeight()
        }
    }

    /**
     * Get the list of items in the adapter
     *
     * @param <T> The item type
     * @return A list of items or `null` if no items are set.
    </T> */
    fun getItems(): List<T>? {
        return mAdapter?.getItems()
    }

    /**
     * Show the dropdown menu
     */
    private fun expand() {
        if (canShowPopup()) {
            if (!hideArrow) {
                animateArrow(true)
            }
            nothingSelected = true
            mPopupWindow!!.showAsDropDown(this)
        }
    }

    /**
     * Closes the dropdown menu
     */
    private fun collapse() {
        if (!hideArrow) {
            animateArrow(false)
        }
        mPopupWindow?.dismiss()
    }

    /**
     * Set the tint color for the dropdown arrow
     *
     * @param color the color value
     */
    fun setArrowColor(@ColorInt color: Int) {
        arrowColor = color
        arrowColorDisabled = lighter(arrowColor, 0.8f)
        arrowDrawable?.setColorFilterSrcIn(arrowColor)
    }

    private fun canShowPopup(): Boolean {
        val activity = activity
        if (activity == null || activity.isFinishing) {
            return false
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            isLaidOut
        } else {
            width > 0 && height > 0
        }
    }

    private val activity: Activity?
        get() {
            var context = context
            while (context is ContextWrapper) {
                if (context is Activity) {
                    return context
                }
                context = context.baseContext
            }
            return null
        }

    @SuppressLint("ObjectAnimatorBinding")
    private fun animateArrow(shouldRotateUp: Boolean) {
        val start = if (shouldRotateUp) 0 else 10000
        val end = if (shouldRotateUp) 10000 else 0
        val animator = ObjectAnimator.ofInt(arrowDrawable, "Level", start, end)
        animator.start()
    }

    /**
     * Set the maximum height of the dropdown menu.
     *
     * @param height the height in pixels
     */
    fun setDropdownMaxHeight(height: Int) {
        popupWindowMaxHeight = height
        mPopupWindow!!.height = calculatePopupWindowHeight()
    }

    /**
     * Set the height of the dropdown menu
     *
     * @param height the height in pixels
     */
    fun setDropdownHeight(height: Int) {
        popupWindowHeight = height
        mPopupWindow!!.height = calculatePopupWindowHeight()
    }

    private fun calculatePopupWindowHeight(): Int {
        if (mAdapter == null) {
            return WindowManager.LayoutParams.WRAP_CONTENT
        }
        val itemHeight = resources.getDimension(R.dimen.px__item_height)
        val listViewHeight = mAdapter!!.itemCount * itemHeight
        if (popupWindowMaxHeight > 0 && listViewHeight > popupWindowMaxHeight) {
            return popupWindowMaxHeight
        } else if (popupWindowHeight != WindowManager.LayoutParams.MATCH_PARENT &&
            popupWindowHeight != WindowManager.LayoutParams.WRAP_CONTENT &&
            popupWindowHeight <= listViewHeight
        ) {
            return popupWindowHeight
        } else if (listViewHeight == 0f && mAdapter!!.getItems().size == 1) {
            return itemHeight.toInt()
        }
        return WindowManager.LayoutParams.WRAP_CONTENT
    }

    /**
     * Interface definition for a callback to be invoked when an item in this view has been selected.
     *
     * @param <T> Adapter item type
    </T> */
    interface OnItemSelectedListener<T> {
        /**
         *
         * Callback method to be invoked when an item in this view has been selected. This callback is invoked only when
         * the newly selected position is different from the previously selected position or if there was no selected
         * item.
         *
         * @param view The [MaterialSpinner] view
         * @param position The position of the view in the adapter
         * @param id The row id of the item that is selected
         * @param item The selected item
         */
        fun onItemSelected(view: MaterialSpinner<T>, position: Int, id: Long, item: T)
    }

    /**
     * Interface definition for a callback to be invoked when the dropdown is dismissed and no item was selected.
     */
    interface OnNothingSelectedListener<T> {
        /**
         * Callback method to be invoked when the [PopupWindow] is dismissed and no item was selected.
         *
         * @param spinner the [MaterialSpinner]
         */
        fun onNothingSelected(spinner: MaterialSpinner<T>)
    }
}
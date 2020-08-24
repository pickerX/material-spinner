package com.pickerx.material.spinner

import android.content.Context

class MaterialSpinnerAdapter<T>(context: Context) :
    MaterialSpinnerBaseAdapter<T>(context) {

    override fun getItemText(item: T): String = item.toString()

    override fun getItemIcon(item: T): Int = 0

}
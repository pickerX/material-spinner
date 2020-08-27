package com.pickerx.material.sample

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.snackbar.Snackbar
import com.pickerx.material.spinner.MaterialSpinner
import com.pickerx.material.spinner.MaterialSpinnerAdapter


class MainActivity : AppCompatActivity() {

    private val ANDROID_VERSIONS = listOf(
        "Cupcake",
        "Donut",
        "Eclair",
        "Froyo",
        "Gingerbread",
        "Honeycomb",
        "Ice Cream Sandwich",
        "Jelly Bean",
        "KitKat",
        "Lollipop",
        "Marshmallow",
        "Nougat",
        "Oreo"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val spinner: MaterialSpinner = findViewById(R.id.spinner)
        val adapter = object : MaterialSpinnerAdapter<String>(this) {
            override fun downloadIcon(item: String, imageView: ImageView, position: Int) {}

            override fun getItemDrawable(position: Int): Drawable? {
                return ResourcesCompat.getDrawable(resources, R.mipmap.ic_wukong, null)
            }
        }
        adapter.onSpinnerSelectedListener = { view: View, i: Int, s: String ->
            Snackbar.make(view, "Clicked${s}", Snackbar.LENGTH_LONG).show()
        }
        spinner.setAdapter(adapter)

        spinner.setItems(ANDROID_VERSIONS)

        spinner.setOnNothingSelectedListener(object :
            MaterialSpinner.OnNothingSelectedListener<String> {
            override fun onNothingSelected(spinner: MaterialSpinner) {
                Snackbar.make(spinner, "Nothing selected", Snackbar.LENGTH_LONG).show()
            }
        })

        spinner.setSelectedIndex(2)
    }
}
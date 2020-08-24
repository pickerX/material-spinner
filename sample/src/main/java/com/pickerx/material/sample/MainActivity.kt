package com.pickerx.material.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.pickerx.material.spinner.MaterialSpinner


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

        val spinner: MaterialSpinner<String> = findViewById(R.id.spinner)
        spinner.setItems(ANDROID_VERSIONS)
        spinner.setOnItemSelectedListener(object :
            MaterialSpinner.OnItemSelectedListener<String> {
            override fun onItemSelected(
                view: MaterialSpinner<String>,
                position: Int,
                id: Long,
                item: String
            ) {
                Snackbar.make(view, "Clicked $item", Snackbar.LENGTH_LONG).show()
            }

        })
        spinner.setOnNothingSelectedListener(object :
            MaterialSpinner.OnNothingSelectedListener<String> {
            override fun onNothingSelected(spinner: MaterialSpinner<String>) {
                Snackbar.make(spinner, "Nothing selected", Snackbar.LENGTH_LONG).show()
            }
        })
    }
}
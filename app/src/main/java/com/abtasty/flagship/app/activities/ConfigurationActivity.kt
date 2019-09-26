package com.abtasty.flagship.app.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.abtasty.flagship.app.R
import kotlinx.android.synthetic.main.activity_configuration.*

class ConfigurationActivity : AppCompatActivity() {

    var id = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration)

        id = intent.getIntExtra("position", 0)
        initComponents()
    }

    private fun initComponents() {
        try {
            val imageId = resources.getIdentifier(
                "campaign_${id + 1}_summary",
                "drawable", this.packageName
            )
            val titleId = resources.getIdentifier(
                "flagship_campaign_${id + 1}_title",
                "string", this.packageName
            )
            main_title2.text = getString(titleId)
            imageView.setImageResource(imageId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

package com.abtasty.flagship.app.activities

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
            val ims = assets.open("campaign_${id + 1}_summary.png")
            val drawable = Drawable.createFromStream(ims, null)
            ims.close()
            val titleId = resources.getIdentifier(
                "flagship_campaign_${id + 1}_title",
                "string", this.packageName
            )
            main_title2.text = getString(titleId)
            imageView.setImageDrawable(drawable)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

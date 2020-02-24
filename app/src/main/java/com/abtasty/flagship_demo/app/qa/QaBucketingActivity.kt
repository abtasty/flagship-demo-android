package com.abtasty.flagship_demo.app.qa

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.abtasty.flagship.main.Flagship
import com.abtasty.flagship_demo.app.R
import kotlinx.android.synthetic.main.activity_qa_bucketing.*

class QaBucketingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qa_bucketing)
    }

    override fun onResume() {
        super.onResume()
        initComponents()
        initResult()
    }

    private fun initComponents() {
        qa_bucketing_add_key.setOnClickListener {
            val key = qa_bucketing_key.text.toString()
            val value = qa_bucketing_value.text.toString()
            try {
                when (qa_add_spinner.selectedItemPosition) {
                    0 -> Flagship.updateContext(
                        key,
                        (value == "1" || value.toLowerCase() == "true")
                    )
                    1 -> {
                        try {
                            Flagship.updateContext(key, value.toInt())
                        } catch (e: Exception) {}
                        try {
                            Flagship.updateContext(key, value.toDouble())
                        } catch (e: Exception) {}
                        try {
                            Flagship.updateContext(key, value.toFloat())
                        } catch (e: Exception) {}
                        try {
                            Flagship.updateContext(key, value.toLong())
                        } catch (e: Exception) {}
                    }
                    else -> Flagship.updateContext(key, value)
                }
                Toast.makeText(this@QaBucketingActivity, "Add : $key with success", Toast.LENGTH_SHORT).show()
                qa_bucketing_key.setText("")
                qa_bucketing_value.setText("")

            } catch (e : Exception) {
                Toast.makeText(this@QaBucketingActivity, "Add : $key with failed", Toast.LENGTH_SHORT).show()
            }

        }

        qa_sync.setOnClickListener {
            Flagship.syncCampaignModifications({
                runOnUiThread {
                    initResult()
                }
            })
        }
    }

    private fun initResult() {
        try {
            val colorStr = Flagship.getModification("btn-color", "#FFFFFF")
            val clickDisabled = Flagship.getModification("btn-disabled", false)
            val maxChar = Flagship.getModification("char-counter", 10)
            qa_bucketing_result_tv.setTextColor(Color.parseColor(colorStr))
            qa_bucketing_result_tv.setOnClickListener {
                if (!clickDisabled)
                    Toast.makeText(this@QaBucketingActivity, "Click !!", Toast.LENGTH_SHORT).show()
            }
            qa_bucketing_result_tv.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxChar))
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }


}

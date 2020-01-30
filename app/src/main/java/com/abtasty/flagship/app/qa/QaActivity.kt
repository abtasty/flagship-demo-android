package com.abtasty.flagship.app.qa

import android.app.Dialog
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.abtasty.flagship.app.R
import com.abtasty.flagship.app.utils.EnvManager
import com.abtasty.flagship.main.Flagship
import kotlinx.android.synthetic.main.activity_flagship_dialog.view.save
import kotlinx.android.synthetic.main.activity_qa.*
import kotlinx.android.synthetic.main.activity_qa_dialog.view.*

class QaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qa)
    }

    override fun onResume() {
        super.onResume()
        initSpinner()
        loadContextValues()
    }


    fun loadContextValues() {
        flagship_qa_section_recycler.layoutManager = LinearLayoutManager(this)
        flagship_qa_section_recycler.adapter =
            QaSectionAdapter {
                startSubQAActivity(it)
            }
    }

    fun startSubQAActivity(position: Int) {
        val intent = Intent(this, when (position) {
             0 -> AutomaticContextActivity::class.java
            else -> null
        })
        startActivity(intent)
    }


    private fun initSpinner() {

        envid_spinner.background.setColorFilter(ContextCompat.getColor(this, android.R.color.white), PorterDuff.Mode.SRC_ATOP)
        refreshSpinner()
        envid_add.setOnClickListener {showAddDialog()}
        envid_spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                parent?.adapter?.getItem(position)?.let {
                    EnvManager.saveSelectedEnvId(this@QaActivity, it as String)
//                    Toast.makeText(this@QaActivity, R.string.flagship_qa_envid_warning, Toast.LENGTH_LONG).show()
                    Flagship.start(this@QaActivity,
                        EnvManager.loadSelectedEnvId(this@QaActivity, true),
                        EnvManager.loadVisitorId(this@QaActivity))
                }
            }
        }

    }

    private fun showAddDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.activity_qa_dialog, null)

        val dialog = Dialog(this, R.style.VisitorDialog)
        dialog.setCancelable(true)
        dialog.setContentView(view)

        view.save.setOnClickListener {
            val name = view.envid_add_name.text.toString()
            val id = view.envid_add_id.text.toString()
            if (name.isNotEmpty() && id.isNotEmpty()) {
                addNewEndId(name, id)
                refreshSpinner()
            }
            dialog.dismiss()
        }
        dialog.window?.setLayout(
            ((this.window?.decorView?.width ?: 0) * 0.8).toInt(),
            ((this.window?.decorView?.height ?: 0) * 0.3).toInt()
        )
        dialog.show()
    }

    private fun refreshSpinner() {
        val list = EnvManager.loadEnvId(this).map { e ->  e.key + " - " + e.value}
        val selected = EnvManager.loadSelectedEnvId(this)
        val index = list.indexOf(selected)
        envid_spinner.adapter = ArrayAdapter(this, R.layout.activity_qa_spinner_elem, list)
        if (index > 0) {
            envid_spinner.setSelection(index)
        }
    }

    private fun addNewEndId(name : String, id : String) {
        val ids = EnvManager.loadEnvId(this)
        ids[name] = id
        EnvManager.saveEnvId(this, ids)
    }
}


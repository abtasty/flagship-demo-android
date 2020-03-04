package com.abtasty.flagship_demo.app.qa

import android.app.Dialog
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.abtasty.flagship.main.Flagship
import com.abtasty.flagship.main.Flagship.Companion
import com.abtasty.flagship_demo.app.R
import com.abtasty.flagship_demo.app.utils.ConfManager
import kotlinx.android.synthetic.main.activity_flagship_dialog.view.save
import kotlinx.android.synthetic.main.activity_qa.*
import kotlinx.android.synthetic.main.activity_qa_dialog.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class QaActivity : AppCompatActivity() {

    var sendRequestLog = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qa)
    }

    override fun onResume() {
        super.onResume()
        loadContextValues()
        initSpinner()
    }


    fun loadContextValues() {
        flagship_qa_section_recycler.layoutManager = LinearLayoutManager(this)
        flagship_qa_section_recycler.adapter =
            QaSectionAdapter {
                startSubQAActivity(it)
            }

        val mode = ConfManager.currentConf.useBucketing
        env_id_use_bucketing.isChecked = mode
        env_id_use_apac.isChecked = ConfManager.currentConf.useAPAC
    }

    fun startSubQAActivity(position: Int) {
        val intent = Intent(
            this, when (position) {
                0 -> AutomaticContextActivity::class.java
                1 -> QaBucketingActivity::class.java
                else -> null
            }
        )
        startActivity(intent)
    }


    private fun initSpinner() {

        envid_spinner.background.setColorFilter(
            ContextCompat.getColor(this, android.R.color.white),
            PorterDuff.Mode.SRC_ATOP
        )
        refreshSpinner()
        envid_add.setOnClickListener { showAddDialog() }
        envid_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            var count = 0
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (count >= 1) {
                    parent?.adapter?.getItem(position)?.let {
                        ConfManager.currentConf.selectedEnvId = (it as String).split(" - ")[1]
                        ConfManager.saveConf(this@QaActivity)
                        restartSDK()
                    }
                }
                count++
            }
        }

        env_id_use_bucketing.setOnCheckedChangeListener { buttonView, isChecked ->
            ConfManager.currentConf.useBucketing = isChecked
            ConfManager.saveConf(this@QaActivity)
            restartSDK()
        }

        env_id_use_apac.setOnCheckedChangeListener { buttonView, isChecked ->
            ConfManager.currentConf.useAPAC = isChecked
            ConfManager.saveConf(this@QaActivity)
            restartSDK()
        }
    }

    private fun restartSDK() {
//        ConfManager.loadConf(this@QaActivity)
        val builder = Flagship.Builder(this@QaActivity, ConfManager.currentConf.selectedEnvId)
            .withFlagshipMode(if (env_id_use_bucketing.isChecked) Flagship.Mode.BUCKETING else Flagship.Mode.DECISION_API)
            .withVisitorId(ConfManager.currentConf.visitorId)
        if (ConfManager.currentConf.useAPAC)
            builder.withAPACRegion("j2jL0rzlgVaODLw2Cl4JC3f4MflKrMgIaQOENv36")
        else
            builder.withAPACRegion("")
        builder.start()
        sendRequestLog = env_id_use_bucketing.isChecked
        initLogs()
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
        val list = ConfManager.currentConf.envIds.map { e -> e.key + " - " + e.value }
        val index = list.indexOfFirst { e -> e.contains(ConfManager.currentConf.selectedEnvId) }
        envid_spinner.adapter = ArrayAdapter(this, R.layout.activity_qa_spinner_elem, list)
        if (index > 0) {
            envid_spinner.setSelection(index)
        }
    }

    private fun addNewEndId(name: String, id: String) {
        ConfManager.currentConf.envIds[name] = id
        ConfManager.saveConf(this@QaActivity)
    }

    fun initLogs() {
        val logCatViewModel = QaActivity.LogCatViewModel()
        clearLogCat()
        logCatViewModel.logCatOutput().observe(this, Observer { logMessage ->
            if (logMessage.contains("bucketing") && (logMessage.contains("200") || logMessage.contains(
                    "304"
                )) && sendRequestLog
            ) {
                Toast.makeText(this, logMessage, Toast.LENGTH_LONG).show()
                sendRequestLog = false
            }
        })
    }

    fun clearLogCat() {
        GlobalScope.launch {
            Runtime.getRuntime().exec("logcat -c")
        }
    }

    class LogCatViewModel : ViewModel() {
        fun logCatOutput() = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            Runtime.getRuntime().exec("logcat -c")
            Runtime.getRuntime().exec("logcat")
                .inputStream
                .bufferedReader()
                .useLines { lines ->
                    lines.forEach { line -> emit(line) }
                }
        }
    }
}


package com.abtasty.flagship_demo.app.qa

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.abtasty.flagship.api.Hit
import com.abtasty.flagship.main.Flagship
import com.abtasty.flagship_demo.app.R
import kotlinx.android.synthetic.main.activity_automatic_context.*
import kotlinx.coroutines.*

class AutomaticContextActivity : AppCompatActivity() {

    var sendRequestLog = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_automatic_context)
    }

    override fun onResume() {
        super.onResume()
        initRecycler()
        initCampaigns()
        initLogs()
    }

    fun initCampaigns() {
        send.setOnClickListener {
            sendRequestLog = true
            Flagship.syncCampaignModifications ({
                runOnUiThread {
                    val value = Flagship.getModification("IS_TARGET_OK", "Null", true)
                    auto_context_result_title.text = getString(R.string.flagship_qa_auto_context_results) + " $value"
                    Flagship.sendTracking(Hit.Event(Hit.EventCategory.ACTION_TRACKING, "kpi_qa_context")
                        .withEventValue(666)
                        .withEventLabel("kpi_qa_context2"))
                }
            })
        }
        reset.setOnClickListener {
            (auto_context_rv.adapter as? QaContextAutoAdapter)?.refresh(this@AutomaticContextActivity)
        }
    }

    fun initRecycler() {
        auto_context_rv.layoutManager = LinearLayoutManager(this)
        val adapter = QaContextAutoAdapter()
        auto_context_rv.adapter = adapter
        adapter.refresh(this)
    }

    fun initLogs() {
        val logCatViewModel = LogCatViewModel()
        clearLogCat()
        logCatViewModel.logCatOutput().observe(this, Observer { logMessage ->
            if (logMessage.contains("/campaigns/") && sendRequestLog) {
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
                .useLines { lines -> lines.forEach { line -> emit(line) }
                }
        }
    }

}

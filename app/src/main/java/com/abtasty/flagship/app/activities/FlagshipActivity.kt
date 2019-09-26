package com.abtasty.flagship.app.activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtasty.flagship.api.Hit
import com.abtasty.flagship.app.R
import com.abtasty.flagship.app.adapters.FlagshipRecyclerViewAdapter
import com.abtasty.flagship.app.interfaces.IFlagshipRecycler
import com.abtasty.flagship.main.Flagship
import kotlinx.android.synthetic.main.activity_flagship.*
import kotlinx.android.synthetic.main.activity_flagship_dialog.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL


class FlagshipActivity : AppCompatActivity(), IFlagshipRecycler {

    var visitorId = "defaultVisitorId"
    var daysSinceLastLaunch = 0
    var isVIPUser = false

    var isImageDisplayed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flagship)

        initComponents()

        //Flagship init
        Flagship.start(this.applicationContext, "") //Todo YOUR ENV ID HERE
        Flagship.setVisitorId(visitorId)
        Flagship.enableLog(Flagship.LogMode.ALL)

        updateContext()
        updateView()
    }

    private fun updateContext() {
        Flagship.setVisitorId(visitorId)
        Flagship.updateContext("isVIPUser", isVIPUser)
        Flagship.updateContext("daysSinceLastLaunch", daysSinceLastLaunch)
    }

    private fun initComponents() {
        loadData()
        campaigns_rv.layoutManager = LinearLayoutManager(this) as RecyclerView.LayoutManager?
        val adapter = FlagshipRecyclerViewAdapter()
        adapter.callback = this
        campaigns_rv.adapter = adapter
        fab.setOnClickListener { showVisitorDialog() }
    }


    private fun updateView() {

        user_id.text = visitorId
        Flagship.syncCampaignModifications {
            this@FlagshipActivity.runOnUiThread {
                applyFlagship()
            }
        }
    }

    /**
     * Get, apply and activate Flagship modifications.
     *
     */
    private fun applyFlagship() {

        //Enable the feature with the value returned by Flagship
        (campaigns_rv.adapter as? FlagshipRecyclerViewAdapter)?.let {
            it.enableVIPFeature =
                Flagship.getModification("featureEnabled", default = false, activate = true)
            it.notifyDataSetChanged()
        }

        //Set the title with the value returned by Flagship
        main_title.text = Flagship.getModification("title", "Welcome", true)

        main_title.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            Flagship.getModification("titleSize", 40, true).toFloat()
        )

        //Set the title color with the value returned by Flagship
        user_id.setTextColor(
            Color.parseColor(
                Flagship.getModification("visitorIdColor", "#DE436F", true)
            )
        )

        displayImage()
    }

    private fun displayImage() {
        if (!isImageDisplayed) {
            GlobalScope.launch {
                val logoUrl = Flagship.getModification("my_image", "", true)
                try {
                    val bitmap = BitmapFactory.decodeStream(URL(logoUrl).openConnection().getInputStream())
                    runOnUiThread {
                        logo.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


    private fun showVisitorDialog() {

        val view = LayoutInflater.from(this).inflate(R.layout.activity_flagship_dialog, null)
        view.et_visitorId.setText(visitorId)
        view.sw_vip.isChecked = isVIPUser
        view.et_days.setText("$daysSinceLastLaunch")

        val dialog = Dialog(this, R.style.VisitorDialog)
        dialog.setCancelable(true)
        dialog.setContentView(view)

        view.save.setOnClickListener {
            visitorId = view.et_visitorId.text.toString()
            daysSinceLastLaunch = view.et_days.text.toString().toIntOrNull() ?: 0
            isVIPUser = view.sw_vip.isChecked
            saveData()
            updateContext()
            updateView()
            dialog.dismiss()
        }
        dialog.window?.setLayout(
            ((this.window?.decorView?.width ?: 0) * 0.8).toInt(),
            ((this.window?.decorView?.height ?: 0) * 0.5).toInt()
        )
        dialog.show()
    }

    override fun onPageClick() {
        //send a page hit tracking
        Flagship.sendTracking(Hit.Page("MainActivity"))
    }

    override fun onEventClick() {
        //send event hit tracking
        Flagship.sendTracking(
            Hit.Event(Hit.EventCategory.ACTION_TRACKING, "kpi_name")
                .withEventLabel("Button_Event")
                .withEventValue(System.currentTimeMillis() / 1000)
        )
    }

    override fun onTransactionClick() {
        //Send a transaction hit tracking
        Flagship.sendTracking(
            Hit.Transaction("#transaction_id", "kpi_affiliation")
                .withCouponCode("coupon")
                .withCurrency("EUR")
                .withItemCount(1)
                .withPaymentMethod("payment_method")
                .withShippingCost(12f)
                .withShippingMethod("shipping_method")
                .withTaxes(2f)
                .withTotalRevenue(12f)
        )
    }

    override fun onItemClick() {
        //send a Item hit tracking
        Flagship.sendTracking(
            Hit.Item("#transaction_id", "product_name")
                .withItemCategory("product_category")
                .withItemCode("product_code")
                .withItemQuantity(1)
                .withPrice(12f)
        )
    }

    override fun onItemClick(position: Int) {
        val intent = Intent(this, ConfigurationActivity::class.java)
        intent.putExtra("position", position)
        startActivity(intent)
    }

    private fun saveData() {
        val pref = getSharedPreferences("flagship-visitor-context", Context.MODE_PRIVATE).edit()
        pref.putString("visitorId", visitorId)
        pref.putInt("daysSinceLastLaunch", daysSinceLastLaunch)
        pref.putBoolean("isVIPUser", isVIPUser)
        pref.apply()
    }

    private fun loadData() {
        val pref = getSharedPreferences("flagship-visitor-context", Context.MODE_PRIVATE)
        daysSinceLastLaunch = pref.getInt("daysSinceLastLaunch", 0)
        isVIPUser = pref.getBoolean("isVIPUser", false)
        visitorId = pref.getString("visitorId", "defaultId")
    }

}

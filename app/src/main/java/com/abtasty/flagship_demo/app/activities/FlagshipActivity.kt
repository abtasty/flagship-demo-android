package com.abtasty.flagship_demo.app.activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtasty.flagship.api.Hit
import com.abtasty.flagship.main.Flagship
import com.abtasty.flagship_demo.app.R
import com.abtasty.flagship_demo.app.adapters.FlagshipRecyclerViewAdapter
import com.abtasty.flagship_demo.app.interfaces.IFlagshipRecycler
import com.abtasty.flagship_demo.app.qa.QaActivity
import com.abtasty.flagship_demo.app.utils.EnvManager
import kotlinx.android.synthetic.main.activity_flagship.*
import kotlinx.android.synthetic.main.activity_flagship_dialog.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL

/**
 *
 * How to use this sample app :
 *
 * 1) Set your env ID at the Flagship init
 * 2) Reproduce the campaigns configurations to your flagship account. (screens in the assets/ folder)
 * 3) Build and run the app
 *
 * -------------------------------------------------------------------------------------------------
 *
 * There are 4 campaigns :
 *
 * 1) Bottom Logo (Perso) : Displays the flagship logo at the bottom left of the app.
 * 2) VIP Feature (Toggle Feature) : Enable the tracking feature for VIP users.
 * 3) Title Wording (ab test) : Change the title wording and the user color according to the selected variations : (Hi, Hello, Hey, Ahoy)
 * 4) Title Wording (ab test) : Change the title to 'Welcome back' or 'Glad to see you back' if the value 'daysSinceLastLaunch' is greater than 5.
 *
 *
 * The following variables : 'visitorId', 'isVIPUser' and 'daysSinceLastLaunch' are visitor context values.
 * Flagship decision api will allocation campaigns/variations according to these context values.
 *
 *
 * You can modify them by clicking on the floating action button at the bottom right of the app.
 * Then click save to update the campaigns.
 *
 * -------------------------------------------------------------------------------------------------
 *
 * Tracking
 *
 * You can send Flagship hit tracking by clicking the 4 corresponding buttons in the Tracking section of the app.
 *
 *
 * -------------------------------------------------------------------------------------------------
 *
 * You can find the technical documentation at : https://developers.flagship.io/android/
 *
 */
class FlagshipActivity : AppCompatActivity(), IFlagshipRecycler {

    var visitorId = "defaultVisitorId"
    var daysSinceLastLaunch = 0
    var isVIPUser = false

    var isImageDisplayed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flagship)

        initComponents()

//        //Flagship init
////        Flagship.start(this.applicationContext, "bkk4s7gcmjcg07fke9dg") //Todo YOUR ENV ID HERE
//        Flagship.start(this.applicationContext, EnvManager.loadSelectedEnvId(this, true)) //Todo YOUR ENV ID HERE
//        Flagship.setVisitorId(visitorId)
//        Flagship.enableLog(Flagship.LogMode.ALL)

        Flagship.FlagshipBuilder(applicationContext, EnvManager.loadSelectedEnvId(this, true))
            .withLogEnabled(Flagship.LogMode.ALL)
            .withVisitorId(visitorId)
            .start()
    }

    override fun onResume() {
        super.onResume()

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
        Flagship.syncCampaignModifications({
            this@FlagshipActivity.runOnUiThread {
                applyFlagship()
            }
        })
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.qa -> {
                startQaActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startQaActivity() {
        val i = Intent(this, QaActivity::class.java)
        startActivity(i)
    }

}

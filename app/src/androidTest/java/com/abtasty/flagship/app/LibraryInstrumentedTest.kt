package com.abtasty.flagship.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.abtasty.flagship.api.Hit
import com.abtasty.flagship.main.Flagship
import com.abtasty.flagship.model.Modification
import com.abtasty.flagship.utils.Utils
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties


@RunWith(AndroidJUnit4ClassRunner::class)
class LibraryInstrumentedTest {

    val fs get() = Flagship.Companion

    init {

        val context = ApplicationProvider.getApplicationContext<Context>()
        fs.start(context.applicationContext, "bk87t3jggr10c6l6sdog")
    }

    @Suppress("UNCHECKED_CAST")
    fun <R> readInstanceProperty(instance: Any, propertyName: String): R {
        val property = instance::class.memberProperties
            // don't cast here to <Any, R>, it would succeed silently
            .first { it.name == propertyName } as KProperty1<Any, *>
        // force a invalid cast exception if incorrect type here
        return property.get(instance) as R
    }

    fun writeInstanceProperty(instance: Any, propertyName: String, value : Any) {
        val property = instance::class.memberProperties
            // don't cast here to <Any, R>, it would succeed silently
            .first { it.name == propertyName } as KMutableProperty<*>
        // force a invalid cast exception if incorrect type here
        property.setter.call(instance, value)
    }



    @Test
    fun visitorId() {
        val id = "toto3000"
        var visitorId : String = readInstanceProperty(fs, "visitorId")
        System.out.println("visitorId : $visitorId")
        assertTrue(!visitorId.isNullOrBlank())
        Flagship.setCustomVisitorId(id)
        visitorId = readInstanceProperty(fs, "visitorId")
        assertTrue(visitorId == id)
    }

    @Test
    fun updateContext() {
        fs.updateContext("unitTestValue1", "douze")
        fs.updateContext("unitTestValue1", 12)

        fs.updateContext("unitTestValue2", "onze")
        fs.updateContext("unitTestValue2", "douze")

        fs.updateContext("unitTestValue0", true)

        val visitorContext : HashMap<String, Any> = readInstanceProperty(fs, "context")
        assertTrue(visitorContext["unitTestValue1"] == 12)
        assertTrue(visitorContext["unitTestValue2"] == "douze")
        assertTrue(visitorContext["unitTestValue0"] == true)
        assertTrue(visitorContext["unitTestValue3"] == null)
    }

    @Test
    fun getModificationDefault() {

        val mod = fs.getModification("value", 31)
        assertTrue(mod == 31)
        val mod2 = fs.getModification("value2", "default")
        assertTrue(mod2 == "default")
        val mod3 = fs.getModification("value3", 31.31)
        assertTrue(mod3 == 31.31)
        val mod4 = fs.getModification("value4", 31f)
        assertTrue(mod4 == 31f)
        val mod5 = fs.getModification("value5", true)
        assertTrue(mod5)
    }

    @Test
    fun getModifications() {
        val modifications = HashMap<String, Modification>()
        modifications["value"] = Modification("value", "group0", "variation0", 31)
        modifications["value2"] = Modification("value2", "group2", "variation2", "value")
        modifications["value3"] = Modification("value3", "group3", "variation3", 31.31)
        modifications["value4"] = Modification("value4", "group4", "variation4", 31f)
        modifications["value5"] = Modification("value5", "group5", "variation5", 31L)
        modifications["value6"] = Modification("value6", "group6", "variation6", true)

        writeInstanceProperty(fs, "modifications", modifications )

        val mad = fs.getModification("value", "trente-et-un")
        assertTrue(mad == "trente-et-un")

        val mod = fs.getModification("value", 12)
        assertTrue(mod == 31)

        val mod2 = fs.getModification("value2", "default")
        assertTrue(mod2 == "value")

        val mod3 = fs.getModification("value3", 31.12)
        assertTrue(mod3 == 31.31)

        val mod4 = fs.getModification("value4", 12f)
        assertTrue(mod4 == 31f)

        val mod5 = fs.getModification("value5", 12L)
        assertTrue(mod5 == 31L)

        val mod6 = fs.getModification("value6", false)
        assertTrue(mod6)

    }

    @Test
    fun isVipFalse() {
        val latch = CountDownLatch(1)
        Flagship.updateContext("isVIPUser", false) {
            val enabled = Flagship.getModification("featureEnabled", default = false, activate = true)
            assertFalse(enabled)
            latch.countDown()
        }
        latch.await()
        assertTrue(true)
    }

    @Test
    fun isVipTrue() {
        val latch = CountDownLatch(1)
        Flagship.updateContext("isVIPUser", true) {
            val enabled = Flagship.getModification("featureEnabled", default = false, activate = true)
            assertTrue(enabled)
            latch.countDown()
        }
        latch.await()
        assertTrue(true)
    }

    @Test
    fun syncCampaignModificationsVIP() {
        var latch  = CountDownLatch(1)
        fs.updateContext("isVIPUser", false)
        fs.syncCampaignModifications {
            val enabled = fs.getModification("featureEnabled", default = false, activate = true)
            assertTrue(!enabled)
            latch.countDown()
        }
        latch.await()
        latch  = CountDownLatch(1)
        fs.updateContext("isVIPUser", true)
        fs.syncCampaignModifications {
            val enabled = fs.getModification("featureEnabled", default = false, activate = true)
            assertTrue(enabled)
            latch.countDown()
        }
        latch.await()
        latch  = CountDownLatch(1)
        fs.updateContext("isVIPUser", false)
        fs.syncCampaignModifications {
            val enabled = fs.getModification("featureEnabled", default = false, activate = true)
            assertTrue(!enabled)
            latch.countDown()
        }
        latch.await()
    }

    @Test
    fun pageHit() {
        val hit = Hit.Page("LibraryInstrumentedTest")
        assertTrue(hit.data.get(Hit.KeyMap.ORIGIN.key) == "LibraryInstrumentedTest")
        fs.sendTracking(hit)
    }

    @Test
    fun transactionHit() {
        val hit = Hit.Transaction("#4802982", "mobile_purchases")
            .withCouponCode("#PROMO")
            .withCurrency("EUR")
            .withItemCount(1)
            .withPaymentMethod("credit_card")
            .withShippingCost(2.99f)
            .withShippingMethod("express")
            .withTaxes(15.47f)
            .withTotalRevenue(279f)

        assertTrue(hit.data.get(Hit.KeyMap.TRANSACTION_ID.key) == "#4802982")
        assertTrue(hit.data.get(Hit.KeyMap.TRANSACTION_AFFILIATION.key) == "mobile_purchases")
        assertTrue(hit.data.get(Hit.KeyMap.TRANSACTION_REVENUE.key) == 279f)
        assertTrue(hit.data.get(Hit.KeyMap.TRANSACTION_SHIPPING.key) == 2.99f)
        assertTrue(hit.data.get(Hit.KeyMap.TRANSACTION_TAX.key) == 15.47f)
        assertTrue(hit.data.get(Hit.KeyMap.TRANSACTION_CURRENCY.key) == "EUR")
        assertTrue(hit.data.get(Hit.KeyMap.TRANSACTION_PAYMENT_METHOD.key) == "credit_card")
        assertTrue(hit.data.get(Hit.KeyMap.TRANSACTION_SHIPPING_METHOD.key) == "express")
        assertTrue(hit.data.get(Hit.KeyMap.TRANSACTION_ITEM_COUNT.key) == 1)
        assertTrue(hit.data.get(Hit.KeyMap.TRANSACTION_COUPON.key) == "#PROMO")
    }

    @Test
    fun itemHit() {
        val hit = Hit.Item("#4802982", "N_SWITCH_19")
            .withItemCategory("video_games")
            .withItemCode("#NS19AHLD")
            .withItemQuantity(1)
            .withPrice(279f)
        assertTrue(hit.data.get(Hit.KeyMap.TRANSACTION_ID.key) == "#4802982")
        assertTrue(hit.data.get(Hit.KeyMap.ITEM_NAME.key) == "N_SWITCH_19")
        assertTrue(hit.data.get(Hit.KeyMap.ITEM_PRICE.key) == 279f)
        assertTrue(hit.data.get(Hit.KeyMap.ITEM_QUANTITY.key) == 1)
        assertTrue(hit.data.get(Hit.KeyMap.ITEM_CODE.key) == "#NS19AHLD")
        assertTrue(hit.data.get(Hit.KeyMap.ITEM_CATEGORY.key) == "video_games")
    }

    @Test
    fun eventHit() {
        val hit = Hit.Event(Hit.EventCategory.ACTION_TRACKING, "click")
            .withEventLabel("button_click")
            .withEventValue(12)
        assertTrue(hit.data.get(Hit.KeyMap.EVENT_CATEGORY.key) == Hit.EventCategory.ACTION_TRACKING.key)
        assertTrue(hit.data.get(Hit.KeyMap.EVENT_ACTION.key) == "click")
        assertTrue(hit.data.get(Hit.KeyMap.EVENT_LABEL.key) == "button_click")
        assertTrue(hit.data.get(Hit.KeyMap.EVENT_VALUE.key) == 12)
    }

    @Test
    fun genVisitor() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val id = Utils.genVisitorId(context)
        assertTrue(id != null)
    }

    @Test
    fun deviceContext() {
        val deviceContext : HashMap<String, Any> = readInstanceProperty(fs, "deviceContext")
        assertTrue(deviceContext.containsKey(Hit.KeyMap.DEVICE_RESOLUTION.key) && deviceContext[Hit.KeyMap.DEVICE_RESOLUTION.key] != null)
        assertTrue(deviceContext.containsKey(Hit.KeyMap.DEVICE_LOCALE.key) && deviceContext[Hit.KeyMap.DEVICE_LOCALE.key] != null)
    }

    @Test
    fun panic() {
        val panic : Boolean = readInstanceProperty(fs, "panicMode")
        assertFalse(panic)

        writeInstanceProperty(fs, "panicMode", true)

        fs.updateContext("panicModeValue", 31)

        val visitorContext : HashMap<String, Any> = readInstanceProperty(fs, "context")
        assertTrue(visitorContext["panicModeValue"] == null)

        writeInstanceProperty(fs, "panicMode", false)
    }
}
package com.abtasty.flagship_demo.app.utils;

import androidx.appcompat.app.AppCompatActivity;

import com.abtasty.flagship.api.Hit;
import com.abtasty.flagship.main.Flagship;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

class Java extends AppCompatActivity {
    public Java() {
//        Flagship.Companion.builder(this.getApplicationContext(), "my_env_id")
//
//                .withFlagshipMode(Flagship.Mode.BUCKETING)
//                .withLogEnabled(Flagship.LogMode.ALL)
//                .withVisitorId("my_visitor_id")
//                .withReadyCallback(() -> {
//                    new Hit.Event(Hit.EventCategory.ACTION_TRACKING, "sdk-android-ready").send();
//                    MainJava.this.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            updateView();
//                        }
//                    });
//                    return null;
//                })
//                .withAPACRegion("my_api_key")
//                .start();
    }

    public void updateView() {
    }
}

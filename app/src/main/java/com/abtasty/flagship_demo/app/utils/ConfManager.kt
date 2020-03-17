package com.abtasty.flagship_demo.app.utils

import android.content.Context
import org.json.JSONObject

class ConfManager {

    data class Conf(var envIds : HashMap<String, String> = HashMap(),
                    var selectedEnvId: String = "defaultEnvId",
                    var useBucketing : Boolean = false,
                    var useAPAC : Boolean = false,
                    var visitorId : String = "defaultId") {


        fun envIdsToJsonString(): JSONObject {
            val result = JSONObject()
            for (e in envIds) {
                result.put(e.key, e.value)
            }
            return result
        }

        fun toJSONString() : String {
            return JSONObject()
                .put("envIds", envIdsToJsonString())
                .put("selectedEnvId", selectedEnvId)
                .put("useBucketing", useBucketing)
                .put("useAPAC", useAPAC)
                .put("visitorId", visitorId)
                .toString()
        }

        companion object {

            fun fromJSONObject(json: JSONObject) : Conf {
                val conf = Conf()
                conf.envIds = envIdsFromJSONString(json.optJSONObject("envIds"))
                conf.selectedEnvId = json.optString("selectedEnvId")
                conf.useBucketing = json.optBoolean("useBucketing")
                conf.useAPAC = json.optBoolean("useAPAC")
                conf.visitorId = json.optString("visitorId")
                return conf
            }

            fun envIdsFromJSONString(json : JSONObject) : HashMap<String, String> {
                val results = HashMap<String, String>()
                for (k in json.keys()) {
                    results[k] = json.getString(k)
                }
                return results
            }

        }
    }

    companion object {

        var currentConf : Conf = Conf(hashMapOf("Raph" to "bkk4s7gcmjcg07fke9dg"),
            "bkk4s7gcmjcg07fke9dg",
            false,
            false,
            "Raph")
//        var currentConf : Conf = Conf(hashMapOf("Top chef prod" to "blhsrbjggr132j03r630"),
//            "blhsrbjggr132j03r630",
//            false,
//            false,
//            "Arthur")

        fun loadConf(context: Context) {
            currentConf = initConf(context)
        }

        private fun initConf(context: Context) : Conf {
            val pref = context.getSharedPreferences("Flagship_conf", Context.MODE_PRIVATE)
            val json = pref.getString("currentConf", currentConf.toJSONString()) ?: currentConf.toJSONString()
            return Conf.fromJSONObject(JSONObject(json))
        }

        fun saveConf(context: Context) {
            val edit = context.getSharedPreferences("Flagship_conf", Context.MODE_PRIVATE).edit()
            edit.putString("currentConf", currentConf.toJSONString())
            edit.apply()
        }
    }
}
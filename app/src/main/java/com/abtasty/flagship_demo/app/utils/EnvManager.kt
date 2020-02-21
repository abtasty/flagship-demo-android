package com.abtasty.flagship_demo.app.utils

import android.content.Context
import org.json.JSONObject

class EnvManager {

    companion object {


        fun loadEnvId(context: Context): HashMap<String, String> {
            val results = HashMap<String, String>()
            val pref = context.getSharedPreferences("Flagship_env", Context.MODE_PRIVATE)
            try {
                val json = JSONObject(pref.getString("qaEnvIds", ""))
                for (k in json.keys()) {
                    json.optString(k, "").let { id -> if (id.isNotEmpty()) results[k] = id }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return results
        }



        fun saveEnvId(context: Context, values: HashMap<String, String>) {
            val obj = JSONObject()
            for (v in values) {
                obj.put(v.key, v.value)
            }
            context.getSharedPreferences("Flagship_env", Context.MODE_PRIVATE).edit().putString("qaEnvIds", obj.toString()).apply()
        }

        fun loadSelectedEnvId(context: Context, realKey : Boolean = false) : String {
            val pref = context.getSharedPreferences("Flagship_env", Context.MODE_PRIVATE)
            return try {
                val selection = pref.getString("qaSelectedId","")!!
                return if (!realKey)
                    selection
                else
                    selection.split(" - ")[1]
            } catch (e : Exception) {
                ""
            }
        }

        fun saveSelectedEnvId(context: Context, id : String) {
            val pref = context.getSharedPreferences("Flagship_env", Context.MODE_PRIVATE).edit()
            pref.putString("qaSelectedId",id).apply()

        }

        fun loadVisitorId(context: Context) : String {
            val pref = context.getSharedPreferences("flagship-visitor-context", Context.MODE_PRIVATE)
            return pref.getString("visitorId", "defaultId")!!
        }

        fun saveModeEnvId(context: Context, mode : Int) {
            val pref = context.getSharedPreferences("Flagship_env", Context.MODE_PRIVATE).edit()
            pref.putInt("qaMode", mode).apply()
        }

        fun loadModeEnvId(context: Context) : Int{
            val pref = context.getSharedPreferences("Flagship_env", Context.MODE_PRIVATE)
            return pref.getInt("qaMode", 0)
        }
    }
}
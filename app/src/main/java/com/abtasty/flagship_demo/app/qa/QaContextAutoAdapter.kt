package com.abtasty.flagship_demo.app.qa

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abtasty.flagship.main.Flagship
import com.abtasty.flagship.utils.PresetContext
import com.abtasty.flagship_demo.app.R
import kotlinx.android.synthetic.main.flagship_qa_auto_context_item.view.*

class QaContextAutoAdapter : RecyclerView.Adapter<QaContextAutoAdapter.QaSectionViewHolder>(){

    var contextValues = LinkedHashMap<PresetContext, Any>()

    class QaSectionViewHolder(itemView : View, var listener : CustomEditTextListener) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.qa_auto_context_value.addTextChangedListener(listener)
        }

        fun bind(key: PresetContext, value: Any?) {
            listener.updatePosition(key)
            itemView.qa_auto_context_key.text = key.key
            itemView.qa_auto_context_value.setText(value.toString())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QaSectionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.flagship_qa_auto_context_item, parent, false)
        return QaSectionViewHolder(view, CustomEditTextListener { presetContext: PresetContext, any: Any ->
            if (contextValues[presetContext].toString() != any.toString()) {

                val castedValue = castValue(presetContext, any)
                castedValue?.let {
                    Flagship.updateContext(presetContext, it)
                    contextValues[presetContext] = it
                }
            }
        })
    }

    fun castValue(presetContext: PresetContext, value: Any) : Any? {
        try {
            if (presetContext.checkValue(value)) {
                return value
            }
        } catch (e : Exception) {

        }

        try {
            val newValue = value.toString().toDouble()
            if (presetContext.checkValue(newValue)) {
                return newValue
            }
        } catch (e : Exception) {

        }

        try {
            val newValue = value.toString().toUpperCase().toBoolean()
            if (presetContext.checkValue(newValue)) {
                return newValue
            }
        } catch (e : Exception) {

        }

        try {
            val newValue = value.toString().toInt()
            if (presetContext.checkValue(newValue)) {
                return newValue
            }
        } catch (e : Exception) {

        }

        return null
    }

    class CustomEditTextListener(var onValueChanged : (PresetContext, Any) -> (Unit)) : TextWatcher {
        private var key : PresetContext? = null

        fun updatePosition(key : PresetContext) {
            this.key = key
        }

        override fun beforeTextChanged(
            charSequence: CharSequence,
            i: Int,
            i2: Int,
            i3: Int
        ) { // no op
        }

        override fun onTextChanged(
            charSequence: CharSequence,
            i: Int,
            i2: Int,
            i3: Int
        ) {
        }

        override fun afterTextChanged(editable: Editable) {
            key?.let {
                onValueChanged(it, editable.toString())
            }
        }
    }

    override fun getItemCount(): Int {
        return contextValues.size
    }

    override fun onBindViewHolder(holder: QaSectionViewHolder, position: Int) {
        val key = contextValues.keys.elementAt(position)
        holder.bind(key, contextValues[key])
    }

    fun loadDeviceContext(context: Context) : LinkedHashMap<PresetContext, Any> {
        val newContextValues = LinkedHashMap<PresetContext, Any>()
        for (fsContext in PresetContext.values()) {
            val value = fsContext.value(context) ?: ""
            val castedValue = castValue(fsContext, value)
            castedValue?.let {
                Flagship.updateContext(fsContext, it)
            }
            newContextValues[fsContext] = value

        }
        return newContextValues
    }

    fun refresh(context: Context) {
        contextValues = loadDeviceContext(context)
        notifyDataSetChanged()
    }

}
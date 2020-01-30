package com.abtasty.flagship.app.qa

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abtasty.flagship.app.R
import com.abtasty.flagship.main.Flagship
import com.abtasty.flagship.utils.FlagshipContext
import kotlinx.android.synthetic.main.flagship_qa_auto_context_item.view.*

class QaContextAutoAdapter : RecyclerView.Adapter<QaContextAutoAdapter.QaSectionViewHolder>(){

    var contextValues = LinkedHashMap<FlagshipContext, Any>()

    class QaSectionViewHolder(itemView : View, var listener : CustomEditTextListener) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.qa_auto_context_value.addTextChangedListener(listener)
        }

        fun bind(key: FlagshipContext, value: Any?) {
            listener.updatePosition(key)
            itemView.qa_auto_context_key.text = key.key
            itemView.qa_auto_context_value.setText(value.toString())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QaSectionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.flagship_qa_auto_context_item, parent, false)
        return QaSectionViewHolder(view, CustomEditTextListener { flagshipContext: FlagshipContext, any: Any ->
            if (contextValues[flagshipContext].toString() != any.toString()) {

                val castedValue = castValue(flagshipContext, any)
                castedValue?.let {
                    Flagship.updateContext(flagshipContext, it)
                    contextValues[flagshipContext] = it
                }
            }
        })
    }

    fun castValue(flagshipContext: FlagshipContext, value: Any) : Any? {
        try {
            if (flagshipContext.checkValue(value)) {
                return value
            }
        } catch (e : Exception) {

        }

        try {
            val newValue = value.toString().toDouble()
            if (flagshipContext.checkValue(newValue)) {
                return newValue
            }
        } catch (e : Exception) {

        }

        try {
            val newValue = value.toString().toUpperCase().toBoolean()
            if (flagshipContext.checkValue(newValue)) {
                return newValue
            }
        } catch (e : Exception) {

        }

        try {
            val newValue = value.toString().toInt()
            if (flagshipContext.checkValue(newValue)) {
                return newValue
            }
        } catch (e : Exception) {

        }

        return null
    }

    class CustomEditTextListener(var onValueChanged : (FlagshipContext, Any) -> (Unit)) : TextWatcher {
        private var key : FlagshipContext? = null

        fun updatePosition(key : FlagshipContext) {
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

    fun loadDeviceContext(context: Context) : LinkedHashMap<FlagshipContext, Any> {
        val newContextValues = LinkedHashMap<FlagshipContext, Any>()
        for (fsContext in FlagshipContext.values()) {
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
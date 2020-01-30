package com.abtasty.flagship.app.qa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abtasty.flagship.app.R
import kotlinx.android.synthetic.main.flagship_qa_section_item.view.*

class QaSectionAdapter(var onClick : (Int) -> (Unit)) : RecyclerView.Adapter<QaSectionAdapter.QaSectionViewHolder>(){

    val sections = arrayOf("Predefined Context")

    class QaSectionViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        fun bind(sectionTitle : String) {
            itemView.qa_section_title.text = sectionTitle
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QaSectionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.flagship_qa_section_item, parent, false)
        return QaSectionViewHolder(view)
    }

    override fun getItemCount(): Int {
        return sections.size
    }

    override fun onBindViewHolder(holder: QaSectionViewHolder, position: Int) {
        holder.itemView.setOnClickListener { onClick(position) }
        holder.bind(sections[position])
    }

}
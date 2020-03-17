package com.abtasty.flagship_demo.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abtasty.flagship_demo.app.R
import com.abtasty.flagship_demo.app.interfaces.IFlagshipRecycler
import kotlinx.android.synthetic.main.activity_flagship_item.view.*
import kotlinx.android.synthetic.main.activity_flagship_item.view.item_desc
import kotlinx.android.synthetic.main.activity_flagship_item.view.item_title
import kotlinx.android.synthetic.main.activity_flagship_item_footer.view.*

class FlagshipRecyclerViewAdapter :
    RecyclerView.Adapter<FlagshipRecyclerViewAdapter.FlagshipRecyclerViewHolder>() {

    var enableVIPFeature = false
    var callback : IFlagshipRecycler? = null

    class FlagshipRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(position: Int, callback : IFlagshipRecycler?) {
            when (position) {
                0 -> {
                    itemView.item_title.text = itemView.context.getString(R.string.flagship_campaign_1_title)
                    itemView.item_desc.text = itemView.context.getString(R.string.flagship_campaign_1_desc)
                    itemView.item_type.text = itemView.context.getString(R.string.flagship_campaign_1_type)
                }
                1 -> {
                    itemView.item_title.text = itemView.context.getString(R.string.flagship_campaign_2_title)
                    itemView.item_desc.text = itemView.context.getString(R.string.flagship_campaign_2_desc)
                    itemView.item_type.text = itemView.context.getString(R.string.flagship_campaign_2_type)
                }
                2 -> {
                    itemView.item_title.text = itemView.context.getString(R.string.flagship_campaign_3_title)
                    itemView.item_desc.text = itemView.context.getString(R.string.flagship_campaign_3_desc)
                    itemView.item_type.text = itemView.context.getString(R.string.flagship_campaign_3_type)
                }
                3 -> {
                    itemView.item_title.text = itemView.context.getString(R.string.flagship_campaign_4_title)
                    itemView.item_desc.text = itemView.context.getString(R.string.flagship_campaign_4_desc)
                    itemView.item_type.text = itemView.context.getString(R.string.flagship_campaign_4_type)
                }
            }
            itemView.setOnClickListener{ callback?.onItemClick(position) }
        }

        fun bindFooter(callback : IFlagshipRecycler?, enableVIPFeature : Boolean = false) {
            itemView.vip.visibility = if (enableVIPFeature) View.GONE else View.VISIBLE
            callback?.let {
                itemView.page.setOnClickListener { callback.onPageClick() }
                itemView.event.setOnClickListener { callback.onEventClick() }
                itemView.transaction.setOnClickListener { callback.onTransactionClick() }
                itemView.item.setOnClickListener { callback.onItemClick() }
            }
        }
    }

    private fun isFooter(position: Int) : Boolean {
        return (position == (itemCount - 1))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlagshipRecyclerViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            if (!isFooter(viewType)) R.layout.activity_flagship_item else R.layout.activity_flagship_item_footer
            , parent, false)
        return FlagshipRecyclerViewHolder(itemView)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return 5
    }

    override fun onBindViewHolder(holder: FlagshipRecyclerViewHolder, position: Int) {
        if (!isFooter(position))
            holder.bind(position, callback)
        else
            holder.bindFooter(callback, enableVIPFeature)
    }
}
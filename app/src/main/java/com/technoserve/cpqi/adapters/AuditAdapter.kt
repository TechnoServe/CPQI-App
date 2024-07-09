package com.technoserve.cpqi.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.technoserve.cpqi.R
import com.technoserve.cpqi.data.AuditCategories

class AuditAdapter(val items: List<AuditCategories>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<AuditAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val auditNameView: TextView = itemView.findViewById(R.id.auditName)
        val auditIconView: ImageView = itemView.findViewById(R.id.auditIcon)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = items[adapterPosition].id.toInt()
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.audit_item_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]
        holder.auditNameView.text = currentItem.name
        loadImageFromUrl(currentItem.iconPath, holder.auditIconView)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    @SuppressLint("DiscouragedApi")
    private fun loadImageFromUrl(url: String, iconView: ImageView) {
        Glide.with(iconView.context)
            .load(
                iconView.context.resources.getIdentifier(
                    url,
                    "drawable",
                    iconView.context.packageName
                )
            )
            .into(iconView)
    }
}

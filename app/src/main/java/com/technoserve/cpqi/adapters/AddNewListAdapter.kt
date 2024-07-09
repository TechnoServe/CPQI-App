package com.technoserve.cpqi.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.technoserve.cpqi.R
import com.technoserve.cpqi.data.Answers
import com.technoserve.cpqi.data.RecordedAudit
import androidx.recyclerview.widget.DividerItemDecoration

class AddNewListAdapter(
    val items: Map<String, RecordedAudit>,
    private val totalItems: Int,
    private val auditQSize: Int,
    private val answers: List<Answers>,
    private val itemClickListener: OnItemClickListener,
) :
    RecyclerView.Adapter<AddNewListAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val auditCwsNameView: TextView = itemView.findViewById(R.id.auditCWSName)
        val totalAView: TextView = itemView.findViewById(R.id.totalA)
        val auditDateView: TextView = itemView.findViewById(R.id.auditDate)
        val auditScoreView: TextView = itemView.findViewById(R.id.auditScore)
        override fun onClick(v: View?) {}

    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.addnew_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items.entries.elementAt(position).value
        holder.auditCwsNameView.text = currentItem.cwsName
        holder.auditDateView.text = currentItem.date
        holder.totalAView.text = "${
            answers.filter { it.groupedAnswersId == currentItem.groupedAnswersId }.size
        } / $auditQSize"
        holder.auditScoreView.text =
            ((currentItem.score * 100) / auditQSize).toString() + "%"
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return if (items.isEmpty()) 0 else items.size
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setupItemDecoration(recyclerView: RecyclerView) {
        recyclerView.apply {
            setHasFixedSize(true)
            val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            itemDecoration.setDrawable(context.getDrawable(R.drawable.divider)!!)
            addItemDecoration(itemDecoration)
        }
    }
}
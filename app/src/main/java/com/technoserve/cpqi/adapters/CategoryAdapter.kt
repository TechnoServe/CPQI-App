package com.technoserve.cpqi.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.technoserve.cpqi.R
import com.technoserve.cpqi.data.Answers
import com.technoserve.cpqi.data.Categories
import com.technoserve.cpqi.data.Questions

class CategoryAdapter(
    val items: List<Categories>,
    private val listener: OnItemClickListener,
    val context: Context,
    private val editMode: Boolean,
    private val viewMode: Boolean,
    private val answerDetails: Array<Answers>,
    private val allCatQuestions: List<Questions>
) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
    private var selectedCategoryIds: MutableSet<Int> = mutableSetOf()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val categoryNameView: TextView = itemView.findViewById(R.id.categoryName)
        val categoryIconView: ImageView = itemView.findViewById(R.id.imageView)
        val progressBar: View = itemView.findViewById(R.id.progressBar)

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryAdapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.category_item_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryAdapter.ViewHolder, position: Int) {
        val currentItem = items[position]
        holder.categoryNameView.text = currentItem.name

        if (editMode || viewMode) {
//            get the questions for the category
            val questions =
                allCatQuestions.filter { it.catId.toInt() == (currentItem.id.toInt() - 1) }

            val answeredQuestions =
                answerDetails.filter { it.qId.toInt() in questions.map { i -> i.id.toInt() } }

            if (answeredQuestions.isNotEmpty()) {
                selectedCategoryIds.add(currentItem.id.toInt())
            }
        }

        if (selectedCategoryIds.contains(currentItem.id.toInt())) {
            holder.progressBar.setBackgroundColor(context.getColor(R.color.maroon))
        }

        loadImageFromUrl(currentItem.iconPath, holder.categoryIconView)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    @SuppressLint("DiscouragedApi")
    private fun loadImageFromUrl(url: String, iconView: ImageView) {
        val resourceId =
            iconView.context.resources.getIdentifier(url, "drawable", iconView.context.packageName)
        Glide.with(iconView.context)
            .load(resourceId)
            .into(iconView)
    }

    fun updateColor(categoryId: Int) {
        selectedCategoryIds.add(categoryId)
    }
}
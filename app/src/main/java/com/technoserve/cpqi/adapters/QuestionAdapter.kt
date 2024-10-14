package com.technoserve.cpqi.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.technoserve.cpqi.R
import com.technoserve.cpqi.data.Answers
import com.technoserve.cpqi.data.Questions

class QuestionAdapter(
    private val auditId: Int,
    private val chart: Int?,
    private val items: List<Questions>,
    var answerDetails: Array<Answers>,
    private val respondent: String,
    private val cwsName: String,
    private val editMode: Boolean,
    private val viewMode: Boolean
) : RecyclerView.Adapter<QuestionAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chartImageView: ImageView? = itemView.findViewById(R.id.chartImageView)



        fun bind(currentItem: Questions) {
            yesBtn.setOnClickListener {
                if (editMode) {
                    answerDetails.forEachIndexed { index, item ->
                        if (item.qId == currentItem.id) {
                            answerDetails[index].answer = Answers.YES
                        } else {
                            answerDetails = answerDetails.plus(
                                Answers(
                                    null,
                                    respondent,
                                    Answers.YES,
                                    currentItem.id,
                                    auditId.toLong(),
                                    cwsName,
                                    item.groupedAnswersId
                                )
                            )
                        }
                    }
                } else {
                    items.forEachIndexed { index, item ->
                        if (item.id == currentItem.id) {
                            if (answerDetails.isNotEmpty() && index < answerDetails.size) {
                                if (answerDetails[index].qId == currentItem.id || answerDetails.size == 1) {
                                    answerDetails[index] = Answers(
                                        null,
                                        respondent,
                                        Answers.YES,
                                        currentItem.id,
                                        auditId.toLong(),
                                        cwsName,
                                        ""
                                    )
                                } else {
                                    answerDetails = answerDetails.plus(
                                        Answers(
                                            null,
                                            respondent,
                                            Answers.YES,
                                            currentItem.id,
                                            auditId.toLong(),
                                            cwsName,
                                            ""
                                        )
                                    )
                                }
                            } else {
                                answerDetails = answerDetails.plus(
                                    Answers(
                                        null,
                                        respondent,
                                        Answers.YES,
                                        currentItem.id,
                                        auditId.toLong(),
                                        cwsName,
                                        ""
                                    )
                                )
                            }
                        }
                    }
                }

                yesBtn.setBackgroundColor(itemView.resources.getColor(R.color.maroon))
                yesBtn.setTextColor(Color.WHITE)
                noBtn.setBackgroundColor(itemView.resources.getColor(R.color.transparent))
                noBtn.setBackgroundResource(R.drawable.border_green)
                noBtn.setTextColor(Color.BLACK)
                skipBtn.setBackgroundColor(itemView.resources.getColor(R.color.transparent))
                skipBtn.setBackgroundResource(R.drawable.border_grey)
            }

            noBtn.setOnClickListener {
                if (editMode) {
                    answerDetails.forEachIndexed { index, item ->
                        if (item.qId == currentItem.id) {
                            answerDetails[index].answer = Answers.NO
                        } else {
                            answerDetails = answerDetails.plus(
                                Answers(
                                    null,
                                    respondent,
                                    Answers.NO,
                                    currentItem.id,
                                    auditId.toLong(),
                                    cwsName,
                                    answerDetails[index].groupedAnswersId
                                )
                            )
                        }
                    }
                } else {
                    items.forEachIndexed { index, item ->
                        if (item.id == currentItem.id) {
                            if (answerDetails.isNotEmpty() && index < answerDetails.size) {
                                if (answerDetails[index].qId == currentItem.id || answerDetails.size == 1) {
                                    answerDetails[index] = Answers(
                                        null,
                                        respondent,
                                        Answers.NO,
                                        currentItem.id,
                                        auditId.toLong(),
                                        cwsName,
                                        ""
                                    )
                                } else {
                                    answerDetails = answerDetails.plus(
                                        Answers(
                                            null,
                                            respondent,
                                            Answers.NO,
                                            currentItem.id,
                                            auditId.toLong(),
                                            cwsName,
                                            ""
                                        )
                                    )
                                }
                            } else {
                                answerDetails = answerDetails.plus(
                                    Answers(
                                        null,
                                        respondent,
                                        Answers.NO,
                                        currentItem.id,
                                        auditId.toLong(),
                                        cwsName,
                                        ""
                                    )
                                )
                            }
                        }
                    }
                }

                noBtn.setBackgroundColor(itemView.resources.getColor(R.color.maroonWhite))
                noBtn.setTextColor(Color.WHITE)
                yesBtn.setBackgroundColor(itemView.resources.getColor(R.color.transparent))
                yesBtn.setBackgroundResource(R.drawable.border_maroon)
                yesBtn.setTextColor(Color.BLACK)
                skipBtn.setBackgroundColor(itemView.resources.getColor(R.color.transparent))
                skipBtn.setBackgroundResource(R.drawable.border_grey)
            }

            skipBtn.setOnClickListener {
                if (editMode) {
                    answerDetails.forEachIndexed { index, item ->
                        if (item.qId == currentItem.id) {
                            answerDetails[index].answer = Answers.SKIP
                        } else {
                            answerDetails = answerDetails.plus(
                                Answers(
                                    null,
                                    respondent,
                                    Answers.SKIP,
                                    currentItem.id,
                                    auditId.toLong(),
                                    cwsName,
                                    answerDetails[index].groupedAnswersId
                                )
                            )
                        }
                    }
                } else {
                    items.forEachIndexed { index, item ->
                        if (item.id == currentItem.id) {
                            if (answerDetails.isNotEmpty() && index < answerDetails.size) {
                                if (answerDetails[index].qId == currentItem.id || answerDetails.size == 1) {
                                    answerDetails[index] = Answers(
                                        null,
                                        respondent,
                                        Answers.SKIP,
                                        currentItem.id,
                                        auditId.toLong(),
                                        cwsName,
                                        ""
                                    )
                                } else {
                                    answerDetails = answerDetails.plus(
                                        Answers(
                                            null,
                                            respondent,
                                            Answers.SKIP,
                                            currentItem.id,
                                            auditId.toLong(),
                                            cwsName,
                                            ""
                                        )
                                    )
                                }
                            } else {
                                answerDetails = answerDetails.plus(
                                    Answers(
                                        null,
                                        respondent,
                                        Answers.SKIP,
                                        currentItem.id,
                                        auditId.toLong(),
                                        cwsName,
                                        ""
                                    )
                                )
                            }
                        }
                    }
                }

                skipBtn.setBackgroundColor(itemView.resources.getColor(R.color.grey))
                yesBtn.setBackgroundColor(itemView.resources.getColor(R.color.transparent))
                yesBtn.setBackgroundResource(R.drawable.border_green)
                yesBtn.setTextColor(Color.BLACK)
                noBtn.setBackgroundColor(itemView.resources.getColor(R.color.transparent))
                noBtn.setBackgroundResource(R.drawable.border_maroon)
                noBtn.setTextColor(Color.BLACK)
            }



            if (viewMode) {
                yesBtn.isEnabled = false
                noBtn.isEnabled = false
                skipBtn.isEnabled = false
            } else {
                yesBtn.isEnabled = true
                noBtn.isEnabled = true
                skipBtn.isEnabled = true
            }
        }

        val questionNumberView: TextView = itemView.findViewById(R.id.popUpTextNumbering)
        val questionNameView: TextView = itemView.findViewById(R.id.popUpText)
        val yesBtn: Button = itemView.findViewById(R.id.yesButton)
        val noBtn: Button = itemView.findViewById(R.id.noButton)
        val skipBtn: Button = itemView.findViewById(R.id.skipButton)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.popup_item_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]
        holder.questionNumberView.text = (position + 1).toString()
        holder.questionNameView.text = currentItem.qName
        holder.chartImageView?.visibility = if (items[position].catId == 0L) View.VISIBLE else View.GONE
        holder.chartImageView?.setImageResource(chart ?: 0)

        // Find the answer for this question from answersFromSP
        val answer = answerDetails.find { it.qId == currentItem.id }

        // Set background color based on the answer (optional, selection highlights remain)
        when (answer?.answer) {
            Answers.YES -> {
                holder.yesBtn.setBackgroundColor(holder.itemView.resources.getColor(R.color.maroon))
                holder.yesBtn.setTextColor(Color.WHITE)
            }

            Answers.NO -> {
                holder.noBtn.setBackgroundColor(holder.itemView.resources.getColor(R.color.maroonWhite))
                holder.noBtn.setTextColor(Color.WHITE)
            }

            Answers.SKIP -> {
                holder.skipBtn.setBackgroundColor(holder.itemView.resources.getColor(R.color.grey))
            }

            else -> {
                if (editMode || viewMode) {
                    val existingAnswer = answerDetails.find { it.qId == currentItem.id }
                    when (existingAnswer?.answer) {
                        Answers.YES -> {
                            holder.yesBtn.setBackgroundColor(holder.itemView.resources.getColor(R.color.maroon))
                            holder.yesBtn.setTextColor(Color.WHITE)
                        }

                        Answers.NO -> {
                            holder.noBtn.setBackgroundColor(holder.itemView.resources.getColor(R.color.maroonWhite))
                            holder.noBtn.setTextColor(Color.WHITE)
                        }

                        Answers.SKIP -> {
                            holder.skipBtn.setBackgroundColor(holder.itemView.resources.getColor(R.color.grey))
                        }
                    }
                }
            }
        }

        holder.bind(currentItem)
    }
}
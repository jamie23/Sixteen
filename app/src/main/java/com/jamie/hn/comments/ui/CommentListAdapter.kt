package com.jamie.hn.comments.ui

import android.content.Context
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.jamie.hn.R
import com.jamie.hn.core.extensions.visible
import com.jamie.hn.core.ui.convertDpToPixels
import kotlinx.android.synthetic.main.article_item.view.author
import kotlinx.android.synthetic.main.article_item.view.time
import kotlinx.android.synthetic.main.comment_item.view.*
import java.util.regex.Pattern

class CommentListAdapter : RecyclerView.Adapter<CommentListAdapter.CommentListHolder>() {
    class CommentListHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private var data = listOf<CommentViewItem>()

    fun data(newData: List<CommentViewItem>) {
        this.data = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentListHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.comment_item, parent, false)

        return CommentListHolder(view)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: CommentListHolder, position: Int) {
        holder.setIsRecyclable(false)

        holder.itemView.run {
            author.text = data[position].author
            time.text = data[position].time
            divider.visible(data[position].showTopDivider)
            text.text = data[position].text
            makeLinksClickable(text)
        }

        addDepthMargins(
            data[position].depth,
            holder.itemView.context,
            holder.itemView,
            data[position].author,
            data[position].text
        )
    }

    private fun makeLinksClickable(textView: TextView) {
        Linkify.addLinks(textView, Linkify.WEB_URLS)
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun addDepthMargins(depth: Int, context: Context, view: View, author: String, text: String) {
        val layout = view.findViewById<ConstraintLayout>(R.id.commentItem)
        val set = ConstraintSet()
        val marginIds = mutableListOf<Int>()

        for (i in 1..depth) {
            val margin = View(context)

            margin.id = View.generateViewId()
            margin.background = context.getDrawable(R.color.divider)
            margin.layoutParams = ConstraintLayout.LayoutParams(2, 0)

            layout.addView(margin)

            set.clone(layout)

            set.connect(margin.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, convertDpToPixels(12f, margin.context).toInt())
            set.connect(margin.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            set.connect(margin.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

            if (i == 1) {
                set.connect(
                    R.id.text,
                    ConstraintSet.START,
                    margin.id,
                    ConstraintSet.END,
                    convertDpToPixels(12f, margin.context).toInt()
                )
                set.connect(
                    R.id.author,
                    ConstraintSet.START,
                    margin.id,
                    ConstraintSet.END,
                    convertDpToPixels(12f, margin.context).toInt()
                )
            } else {
                set.connect(
                    marginIds[i - 2],
                    ConstraintSet.START,
                    margin.id,
                    ConstraintSet.END,
                    convertDpToPixels(12f, margin.context).toInt()
                )
            }

            set.applyTo(layout)

            marginIds.add(margin.id)
        }
    }
}

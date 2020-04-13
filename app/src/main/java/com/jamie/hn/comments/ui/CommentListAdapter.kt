package com.jamie.hn.comments.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.jamie.hn.R
import com.jamie.hn.core.extensions.visible
import kotlinx.android.synthetic.main.article_item.view.author
import kotlinx.android.synthetic.main.article_item.view.time
import kotlinx.android.synthetic.main.comment_item.view.*

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
        holder.itemView.run {
            author.text = data[position].author
            time.text = data[position].time
            text.text = data[position].text
            divider.visible(data[position].showTopDivider)
        }
        addDepthMargins(data[position].depth, holder.itemView.context, holder.itemView)
    }

    private fun addDepthMargins(depth: Int, context: Context, view: View) {
        val layout = view.findViewById<ConstraintLayout>(R.id.commentItem)
        val set = ConstraintSet()

//        var margin = View(holder.itemView.context)
        var margin = TextView(context)

        margin.text = "Hello World"
        margin.background = context.getDrawable(R.color.divider)
//        margin.layoutParams = ConstraintLayout.LayoutParams(1, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)
        margin.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)
        margin.id = View.generateViewId()

        layout.addView(margin)

        set.clone(layout)
        set.connect(margin.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        set.connect(margin.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

        set.applyTo(layout)
    }
}

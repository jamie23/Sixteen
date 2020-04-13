package com.jamie.hn.comments.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    }
}

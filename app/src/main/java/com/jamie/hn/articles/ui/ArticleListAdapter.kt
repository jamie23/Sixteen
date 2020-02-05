package com.jamie.hn.articles.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jamie.hn.R
import kotlinx.android.synthetic.main.article_item.view.*

class ArticleListAdapter : RecyclerView.Adapter<ArticleListAdapter.ArticleListHolder>() {

    class ArticleListHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private var data: List<ArticleViewItem> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleListHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.article_item, parent, false)

        return ArticleListHolder(view)
    }

    fun data(newData: List<ArticleViewItem>) {
        this.data = newData
        notifyDataSetChanged()
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ArticleListHolder, position: Int) {
        holder.itemView.author.text = data[position].author
        holder.itemView.comments.text = data[position].comments
        holder.itemView.score.text = data[position].score
        holder.itemView.time.text = data[position].time
        holder.itemView.title.text = data[position].title
        holder.itemView.url.text = data[position].url
    }
}
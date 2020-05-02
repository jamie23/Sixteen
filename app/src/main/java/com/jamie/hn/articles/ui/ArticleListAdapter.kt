package com.jamie.hn.articles.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jamie.hn.R
import kotlinx.android.synthetic.main.article_action_bar.view.*
import kotlinx.android.synthetic.main.article_item.view.*

class ArticleListAdapter : RecyclerView.Adapter<ArticleListAdapter.ArticleListHolder>() {

    class ArticleListHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private var data = listOf<ArticleViewItem>()

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
        holder.itemView.run {
            author.text = data[position].author
            comments.text = data[position].comments
            score.text = data[position].score
            time.text = data[position].time
            title.text = data[position].title
            url.text = data[position].url
            setOnClickListener {
                data[position].apply {
                    articleViewerCallback(id)
                }
            }

            linkActionBar.setOnClickListener {
                data[position].apply {
                    articleViewerCallback(id)
                }
            }

            comments.setOnClickListener {
                data[position].apply {
                    commentsCallback(id)
                }
            }

            commentsActionBar.setOnClickListener {
                data[position].apply {
                    commentsCallback(id)
                }
            }

        }
    }
}

package com.jamie.hn.stories.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jamie.hn.R
import kotlinx.android.synthetic.main.story_action_bar.view.*
import kotlinx.android.synthetic.main.story_item.view.*

class StoryListAdapter : RecyclerView.Adapter<StoryListAdapter.ArticleListHolder>() {

    class ArticleListHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private var data = listOf<StoryViewItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleListHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.story_item, parent, false)

        return ArticleListHolder(view)
    }

    fun data(newData: List<StoryViewItem>) {
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
                data[position].storyViewerCallback(data[position].id)
            }

            articleButton.setOnClickListener {
                data[position].storyViewerCallback(data[position].id)
            }

            comments.setOnClickListener {
                data[position].commentsCallback(data[position].id)
            }

            commentsButton.setOnClickListener {
                data[position].commentsCallback(data[position].id)
            }
        }
    }
}

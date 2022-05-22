package com.jamie.hn.stories.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jamie.hn.core.extensions.visibleOrInvisible
import com.jamie.hn.databinding.StoryItemCompleteBinding

class StoryListAdapter : RecyclerView.Adapter<StoryListAdapter.ArticleListHolder>() {

    class ArticleListHolder(val viewBinding: StoryItemCompleteBinding) :
        RecyclerView.ViewHolder(viewBinding.root)

    private var data = listOf<StoryViewItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleListHolder {
        val viewBinding =
            StoryItemCompleteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticleListHolder(viewBinding)
    }

    fun data(newData: List<StoryViewItem>) {
        this.data = newData
        notifyDataSetChanged()
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ArticleListHolder, position: Int) {
        holder.viewBinding.run {
            author.text = data[position].author
            actionBar.commentsButton.text = data[position].comments
            score.text = data[position].score
            scoreText.text = data[position].scoreText
            time.text = data[position].time
            title.text = data[position].title
            url.text = data[position].url
            actionBar.articleButton.visibleOrInvisible(data[position].showNavigateToArticle)
            storyItemMain.setOnClickListener {
                data[position].storyViewerCallback(data[position].id)
            }
            actionBar.articleButton.setOnClickListener {
                data[position].storyViewerCallback(data[position].id)
            }
            actionBar.commentsButton.setOnClickListener {
                data[position].commentsCallback(data[position].id)
            }
        }
    }
}

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
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jamie.hn.R
import com.jamie.hn.comments.ui.repository.model.CurrentState.COLLAPSED
import com.jamie.hn.comments.ui.repository.model.CurrentState.FULL
import com.jamie.hn.comments.ui.repository.model.CurrentState.HEADER
import com.jamie.hn.core.extensions.visibleOrGone
import com.jamie.hn.core.extensions.visibleOrInvisible
import com.jamie.hn.core.ui.convertDpToPixels
import com.jamie.hn.databinding.CommentItemCollapsedBinding
import com.jamie.hn.databinding.CommentItemFullBinding
import com.jamie.hn.databinding.StoryItemCompleteBinding

class CommentsListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class FullCommentViewHolder(val viewBinding: CommentItemFullBinding) :
        RecyclerView.ViewHolder(viewBinding.root)

    class CollapsedCommentViewHolder(val viewBinding: CommentItemCollapsedBinding) :
        RecyclerView.ViewHolder(viewBinding.root)

    class HeaderViewHolder(val viewBinding: StoryItemCompleteBinding) :
        RecyclerView.ViewHolder(viewBinding.root)

    private var differ = AsyncListDiffer(this, DiffCallback)
    private val globalMarginIds = mutableListOf<Int>()

    fun data(newData: List<ViewItem>) {
        differ.submitList(newData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewBinding = when (viewType) {
            TYPE_FULL -> CommentItemFullBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            TYPE_COLLAPSED -> CommentItemCollapsedBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            TYPE_HEADER -> StoryItemCompleteBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            else -> throw IllegalArgumentException("Unsupported View Type")
        }

        return when (viewType) {
            TYPE_FULL -> FullCommentViewHolder(viewBinding as CommentItemFullBinding)
            TYPE_COLLAPSED -> CollapsedCommentViewHolder(viewBinding as CommentItemCollapsedBinding)
            TYPE_HEADER -> HeaderViewHolder(viewBinding as StoryItemCompleteBinding)
            else -> throw IllegalArgumentException("Do not support this view type")
        }
    }

    override fun getItemCount() = differ.currentList.size

    override fun getItemViewType(position: Int) =
        when (differ.currentList[position].state) {
            FULL -> TYPE_FULL
            COLLAPSED -> TYPE_COLLAPSED
            HEADER -> TYPE_HEADER
            else -> throw IllegalArgumentException("${differ.currentList[position].state}")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_FULL -> bindFullCommentViewHolder(holder as FullCommentViewHolder, position)
            TYPE_COLLAPSED -> bindCollapsedCommentViewHolder(
                holder as CollapsedCommentViewHolder,
                position
            )
            TYPE_HEADER -> bindHeaderViewHolder(holder as HeaderViewHolder, position)
        }
    }

    private fun bindFullCommentViewHolder(holder: FullCommentViewHolder, position: Int) {
        val item = differ.currentList[position] as CommentViewItem

        holder.viewBinding.run {
            author.text = item.author
            time.text = item.time
            divider.visibleOrInvisible(item.showTopDivider)
            text.text = item.text
            makeLinksClickable(text)
            text.setOnLongClickListener {
                // Adding Linkify stops on click listeners on the text view so we add one if the
                // cursor is not on text
                if (text.isNotLink()) {
                    item.clickCommentListener.invoke(item.id)
                }
                true
            }
            commentItem.setOnLongClickListener {
                item.clickCommentListener.invoke(item.id)
                true
            }

            addDepthMargins(item.depth, commentItem.context, commentItem, TYPE_FULL, R.id.author)
        }
    }

    private fun bindCollapsedCommentViewHolder(holder: CollapsedCommentViewHolder, position: Int) {
        val item = differ.currentList[position] as CommentViewItem

        holder.viewBinding.run {
            authorAndHiddenChildren.text = item.authorAndHiddenChildren
            time.text = item.time
            divider.visibleOrInvisible(item.showTopDivider)
            commentItemCollapsed.setOnClickListener {
                item.clickCommentListener.invoke(item.id)
                true
            }
            commentItemCollapsed.setOnLongClickListener {
                item.clickCommentListener.invoke(item.id)
                true
            }
            addDepthMargins(
                item.depth,
                commentItemCollapsed.context,
                commentItemCollapsed,
                TYPE_COLLAPSED,
                R.id.authorAndHiddenChildren
            )
        }
    }

    private fun bindHeaderViewHolder(holder: HeaderViewHolder, position: Int) {
        val item = differ.currentList[position] as HeaderViewItem

        holder.viewBinding.run {
            author.text = item.author
            actionBar.commentsButton.text = item.comments
            score.text = item.score
            scoreText.text = item.scoreText
            time.text = item.time
            title.text = item.title
            url.text = item.url
            actionBar.commentsButton.visibleOrGone(false)
            actionBar.articleButton.setOnClickListener {
                item.storyViewerCallback(item.id)
            }
        }
    }

    private fun makeLinksClickable(textView: TextView) {
        Linkify.addLinks(textView, Linkify.WEB_URLS)
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun getLayoutFromViewType(view: View, type: Int) =
        when (type) {
            TYPE_FULL -> view.findViewById(R.id.commentItem) as ConstraintLayout
            TYPE_COLLAPSED -> view.findViewById(R.id.commentItemCollapsed) as ConstraintLayout
            TYPE_HEADER -> view.findViewById(R.id.storyItemMain) as ConstraintLayout
            else -> throw IllegalArgumentException("Do not support $type")
        }

    private fun removeRecycledDepthMargins(
        view: View,
        type: Int,
        startBoundView: Int
    ) {
        val layout = getLayoutFromViewType(view, type)

        // Remove all margins from already in the recycled view
        globalMarginIds.forEach {
            val margin = view.findViewById<View>(it) ?: return@forEach
            layout.removeView(margin)
        }

        // Add constraint back in for left most view
        val set = ConstraintSet()
        set.clone(layout)

        set.connect(
            startBoundView,
            ConstraintSet.START,
            R.id.contentStart,
            ConstraintSet.END,
            0
        )

        set.applyTo(layout)
    }

    private fun addDepthMargins(
        depth: Int,
        context: Context,
        view: View,
        type: Int,
        startBoundView: Int
    ) {
        removeRecycledDepthMargins(view, type, startBoundView)

        if (depth == 0) return

        val layout = getLayoutFromViewType(view, type)

        val set = ConstraintSet()
        val localMarginIds = mutableListOf<Int>()
        val pixelOffset = convertDpToPixels(PADDING, view.context).toInt()

        for (i in 1..depth) {
            val margin = View(context)

            margin.id = View.generateViewId()
            margin.background = context.getDrawable(R.color.divider)
            margin.layoutParams = ConstraintLayout.LayoutParams(2, 0)

            layout.addView(margin)

            set.clone(layout)

            initialiseMargins(set, margin.id, pixelOffset)

            if (i == 1) {
                set.connect(
                    startBoundView,
                    ConstraintSet.START,
                    margin.id,
                    ConstraintSet.END,
                    pixelOffset
                )
            } else {
                set.connect(
                    localMarginIds[i - 2],
                    ConstraintSet.START,
                    margin.id,
                    ConstraintSet.END,
                    pixelOffset
                )
            }

            set.applyTo(layout)

            localMarginIds.add(margin.id)
            globalMarginIds.add(margin.id)
        }
    }

    private fun initialiseMargins(
        set: ConstraintSet,
        id: Int,
        pixelOffset: Int
    ) {
        // Margin start constrained to parent start with offset
        set.connect(
            id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START,
            pixelOffset
        )
        // Margin top constrained to parent top
        set.connect(id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        // Margin bottom constraint to parent bottom
        set.connect(id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
    }

    private fun TextView.isNotLink() =
        this.selectionStart == -1 && this.selectionEnd == -1

    private object DiffCallback : DiffUtil.ItemCallback<ViewItem>() {

        override fun areItemsTheSame(oldItem: ViewItem, newItem: ViewItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: ViewItem,
            newItem: ViewItem
        ) = oldItem == newItem
    }

    companion object {
        const val PADDING = 12f

        // View types
        const val TYPE_FULL = 1
        const val TYPE_COLLAPSED = 2
        const val TYPE_HEADER = 3
    }
}

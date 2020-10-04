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
import com.jamie.hn.core.extensions.visibleOrInvisible
import com.jamie.hn.core.ui.convertDpToPixels
import kotlinx.android.synthetic.main.comment_item_collapsed.view.*
import kotlinx.android.synthetic.main.comment_item_full.view.*
import kotlinx.android.synthetic.main.comment_item_full.view.divider
import kotlinx.android.synthetic.main.story_item.view.author
import kotlinx.android.synthetic.main.story_item.view.time

class CommentsListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class FullCommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    class CollapsedCommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private var differ = AsyncListDiffer(this, DiffCallback)
    private val globalMarginIds = mutableListOf<Int>()

    fun data(newData: List<CommentViewItem>) {
        differ.submitList(newData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = if (viewType == TYPE_FULL) {
            R.layout.comment_item_full
        } else {
            R.layout.comment_item_collapsed
        }

        val view = LayoutInflater.from(parent.context)
            .inflate(layout, parent, false)

        return when (viewType) {
            TYPE_FULL -> FullCommentViewHolder(view)
            TYPE_COLLAPSED -> CollapsedCommentViewHolder(view)
            else -> throw IllegalArgumentException("Do not support this view type")
        }
    }

    override fun getItemCount() = differ.currentList.size

    override fun getItemViewType(position: Int) =
        when (differ.currentList[position].state) {
            FULL -> TYPE_FULL
            COLLAPSED -> TYPE_COLLAPSED
            else -> throw IllegalArgumentException()
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val type = getItemViewType(position)

        if (type == TYPE_FULL) {
            bindFullCommentViewHolder(holder, position)
        } else {
            bindCollapsedCommentViewHolder(holder, position)
        }
    }

    private fun bindFullCommentViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = differ.currentList[position]

        holder.itemView.run {
            author.text = item.author
            time.text = item.time
            divider.visibleOrInvisible(item.showTopDivider)
            text.text = item.text
            makeLinksClickable(text)
            text.setOnLongClickListener {
                // Adding Linkify stops on click listeners on the text view so we add one if the
                // cursor is not on text
                if (text.isNotLink()) {
                    item.longClickCommentListener.invoke(item.id)
                }
                true
            }
            setOnLongClickListener {
                item.longClickCommentListener.invoke(item.id)
                true
            }

            addDepthMargins(item.depth, context, this, TYPE_FULL, R.id.author)
        }
    }

    private fun bindCollapsedCommentViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = differ.currentList[position]

        holder.itemView.run {
            authorAndHiddenChildren.text = item.authorAndHiddenChildren
            time.text = item.time
            divider.visibleOrInvisible(item.showTopDivider)
            setOnLongClickListener {
                item.longClickCommentListener.invoke(item.id)
                true
            }

            addDepthMargins(item.depth, context, this, TYPE_COLLAPSED, R.id.authorAndHiddenChildren)
        }
    }

    private fun makeLinksClickable(textView: TextView) {
        Linkify.addLinks(textView, Linkify.WEB_URLS)
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun getLayoutFromViewType(view: View, type: Int) =
        if (type == TYPE_FULL) {
            view.findViewById(R.id.commentItem) as ConstraintLayout
        } else {
            view.findViewById(R.id.commentItemCollapsed) as ConstraintLayout
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

    private object DiffCallback : DiffUtil.ItemCallback<CommentViewItem>() {

        override fun areItemsTheSame(oldItem: CommentViewItem, newItem: CommentViewItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: CommentViewItem,
            newItem: CommentViewItem
        ) = oldItem == newItem
    }

    companion object {
        const val PADDING = 12f

        // View types
        const val TYPE_FULL = 1
        const val TYPE_COLLAPSED = 2
    }
}

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
import com.jamie.hn.comments.ui.repository.model.CurrentState.COLLAPSED
import com.jamie.hn.comments.ui.repository.model.CurrentState.FULL
import com.jamie.hn.core.extensions.visibleOrInvisible
import com.jamie.hn.core.ui.convertDpToPixels
import kotlinx.android.synthetic.main.comment_item_collapsed.view.*
import kotlinx.android.synthetic.main.story_item.view.author
import kotlinx.android.synthetic.main.story_item.view.time
import kotlinx.android.synthetic.main.comment_item_full.view.*
import kotlinx.android.synthetic.main.comment_item_full.view.divider
import kotlin.IllegalArgumentException

class CommentsListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class FullCommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    class CollapsedCommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private var data = listOf<CommentViewItem>()

    fun data(newData: List<CommentViewItem>) {
        this.data = newData
        notifyDataSetChanged()
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

    override fun getItemCount() = data.size

    override fun getItemViewType(position: Int) =
        when (data[position].state) {
            FULL -> TYPE_FULL
            COLLAPSED -> TYPE_COLLAPSED
            else -> throw IllegalArgumentException()
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        val type = getItemViewType(position)
        var leftMostBoundView: Int

        leftMostBoundView = if (type == TYPE_FULL) {
            bindFullCommentViewHolder(holder, position)
            R.id.author
        } else {
            bindCollapsedCommentViewHolder(holder, position)
            R.id.authorAndHiddenChildren
        }

        addDepthMargins(data[position].depth, holder.itemView.context, holder.itemView, type, leftMostBoundView)
    }

    private fun bindFullCommentViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.run {
            author.text = data[position].author
            time.text = data[position].time
            divider.visibleOrInvisible(data[position].showTopDivider)
            text.text = data[position].text
            makeLinksClickable(text)
            text.setOnLongClickListener {
                // Adding Linkify stops on click listeners on the text view so we add one if the
                // cursor is not on text
                if (text.isNotLink()) {
                    data[position].longClickCommentListener.invoke(data[position].id)
                }
                true
            }
            setOnLongClickListener {
                data[position].longClickCommentListener.invoke(data[position].id)
                true
            }
        }
    }

    private fun bindCollapsedCommentViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.run {
            authorAndHiddenChildren.text = data[position].authorAndHiddenChildren
            time.text = data[position].time
            divider.visibleOrInvisible(data[position].showTopDivider)
            setOnLongClickListener {
                data[position].longClickCommentListener.invoke(data[position].id)
                true
            }
        }
    }

    private fun makeLinksClickable(textView: TextView) {
        Linkify.addLinks(textView, Linkify.WEB_URLS)
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun addDepthMargins(
        depth: Int,
        context: Context,
        view: View,
        type: Int,
        startBoundView: Int
    ) {

        val layout: ConstraintLayout = if (type == TYPE_FULL) {
            view.findViewById(R.id.commentItem)
        } else {
            view.findViewById(R.id.commentItemCollapsed)
        }

        val set = ConstraintSet()
        val marginIds = mutableListOf<Int>()
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
                    marginIds[i - 2],
                    ConstraintSet.START,
                    margin.id,
                    ConstraintSet.END,
                    pixelOffset
                )
            }

            set.applyTo(layout)

            marginIds.add(margin.id)
        }
    }

    private fun initialiseMargins(
        set: ConstraintSet,
        id: Int,
        pixelOffset: Int
    ) {
        set.connect(
            id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START,
            pixelOffset
        )
        set.connect(id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        set.connect(id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
    }

    private fun TextView.isNotLink() =
        this.selectionStart == -1 && this.selectionEnd == -1

    companion object {
        const val PADDING = 12f

        // View types
        const val TYPE_FULL = 1
        const val TYPE_COLLAPSED = 2
    }
}

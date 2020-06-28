package com.jamie.hn.comments.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jamie.hn.R
import com.jamie.hn.core.extensions.visibleOrGone
import kotlinx.android.synthetic.main.comment_list_fragment.*
import kotlinx.android.synthetic.main.comment_list_fragment.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class CommentsFragment : Fragment(R.layout.comment_list_fragment) {
    private val storyId: Long
        get() = arguments?.get("storyId") as Long

    private lateinit var commentsListAdapter: CommentsListAdapter

    private val viewModel: CommentsListViewModel by viewModel {
        parametersOf(storyId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        commentsListAdapter = CommentsListAdapter()

        view.commentsList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = commentsListAdapter
        }

        viewModel.init()

        viewModel.commentsViewState().observe(viewLifecycleOwner, Observer {
            progressBar.visibleOrGone(it.refreshing)
            commentSwipeLayout.isRefreshing = it.refreshing
            commentsList.visibleOrGone(!it.refreshing)
            commentsListAdapter.data(it.comments)
        })

        commentSwipeLayout.setOnRefreshListener {
            viewModel.refreshList()
        }
    }
}

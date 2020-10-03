package com.jamie.hn.comments.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
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

        initialiseLiveDataObservers()
    }

    private fun initialiseLiveDataObservers() {
        viewModel.commentsViewState().observe(viewLifecycleOwner, Observer {
            progressBar.visibleOrGone(it.refreshing)
            commentSwipeLayout.isRefreshing = it.refreshing
            commentsList.visibleOrGone(!it.refreshing)
            commentsListAdapter.data(it.comments)
            commentListError.visibleOrGone(false)
        })

        viewModel.networkErrorCachedResults().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                showNetworkFailureCachedResults(commentsList)
            }
        })

        viewModel.networkErrorNoCacheResults().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                progressBar.visibleOrGone(false)
                commentSwipeLayout.isRefreshing = false
                showNetworkFailureNoCacheResults()
            }
        })

        viewModel.urlClicked().observe(viewLifecycleOwner, Observer<Uri> {
            urlClickedCallback(it)
        })

        commentSwipeLayout.setOnRefreshListener {
            viewModel.userManuallyRefreshed()
        }
    }

    private fun showNetworkFailureCachedResults(view: View) {
        Snackbar.make(view, R.string.comments_network_error, Snackbar.LENGTH_LONG).show()
    }

    private fun showNetworkFailureNoCacheResults() {
        commentListError.visibleOrGone(true)
    }

    private fun urlClickedCallback(uri: Uri) {
        val browserIntent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(browserIntent)
    }
}

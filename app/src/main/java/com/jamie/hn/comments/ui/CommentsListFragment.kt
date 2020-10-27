package com.jamie.hn.comments.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.jamie.hn.R
import com.jamie.hn.core.extensions.visibleOrGone
import com.jamie.hn.databinding.CommentListFragmentBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class CommentsListFragment : Fragment(R.layout.comment_list_fragment) {
    private val storyId: Int
        get() = arguments?.get("storyId") as Int

    private var binding: CommentListFragmentBinding? = null
    private lateinit var commentsListAdapter: CommentsListAdapter

    private val viewModel: CommentsListViewModel by viewModel {
        parametersOf(storyId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CommentListFragmentBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        commentsListAdapter = CommentsListAdapter()

        binding?.commentsList?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = commentsListAdapter
        }

        binding?.topAppBar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.openArticle -> {
                    viewModel.openArticle()
                    true
                }
                R.id.share -> {
                    true
                }
                else -> false
            }
        }

        viewModel.init()

        initialiseLiveDataObservers(view)
    }

    private fun initialiseLiveDataObservers(view: View) {
        viewModel.commentsViewState().observe(viewLifecycleOwner, Observer { item ->
            binding?.let {
                it.progressBar.visibleOrGone(item.refreshing)
                it.commentSwipeLayout.isRefreshing = item.refreshing
                it.commentsList.visibleOrGone(!item.refreshing)
                it.commentListError?.visibleOrGone(false)
            }
            commentsListAdapter?.data(item.comments)
        })

        viewModel.networkErrorCachedResults().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                showNetworkFailureCachedResults(binding?.commentsList)
            }
        })

        viewModel.networkErrorNoCacheResults().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                binding?.progressBar?.visibleOrGone(false)
                binding?.commentSwipeLayout?.isRefreshing = false
                showNetworkFailureNoCacheResults()
            }
        })

        viewModel.urlClicked().observe(viewLifecycleOwner, Observer<String> {
            urlClickedCallback(Uri.parse(it))
        })

        binding?.commentSwipeLayout?.setOnRefreshListener {
            viewModel.userManuallyRefreshed()
        }

        viewModel.navigateToArticle().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { url ->
                val action = CommentsListFragmentDirections.actionCommentsListToArticleViewer(url)
                view.findNavController().navigate(action)
            }
        })
    }

    private fun showNetworkFailureCachedResults(view: View?) {
        if (view == null) return
        Snackbar.make(view, R.string.comments_network_error, Snackbar.LENGTH_LONG).show()
    }

    private fun showNetworkFailureNoCacheResults() {
        binding?.commentListError?.visibleOrGone(true)
    }

    private fun urlClickedCallback(uri: Uri) {
        val browserIntent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(browserIntent)
    }
}

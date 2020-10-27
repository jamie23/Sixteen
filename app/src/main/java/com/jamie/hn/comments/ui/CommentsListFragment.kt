package com.jamie.hn.comments.ui

import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.ACTION_VIEW
import android.content.Intent.EXTRA_TEXT
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        commentsListAdapter = CommentsListAdapter()

        binding?.let {
            it.commentsList.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                adapter = commentsListAdapter
            }

            it.topAppBar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.openArticle -> {
                        viewModel.openArticle()
                        true
                    }
                    R.id.share -> {
                        viewModel.shareURL()
                        true
                    }
                    else -> false
                }
            }

            it.commentSwipeLayout.setOnRefreshListener {
                viewModel.userManuallyRefreshed()
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

        viewModel.navigateToArticle().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { url ->
                val action = CommentsListFragmentDirections.actionCommentsListToArticleViewer(url)
                view.findNavController().navigate(action)
            }
        })

        viewModel.articleTitle().observe(viewLifecycleOwner, Observer {
            binding?.topAppBar?.title = it
        })

        viewModel.shareUrl().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { text ->
                val sendIntent = Intent().apply {
                    action = ACTION_SEND
                    putExtra(EXTRA_TEXT, text)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
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
        val browserIntent = Intent(ACTION_VIEW, uri)
        startActivity(browserIntent)
    }
}

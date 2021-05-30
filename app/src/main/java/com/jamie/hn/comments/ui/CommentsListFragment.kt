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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.jamie.hn.R
import com.jamie.hn.core.StoriesListType
import com.jamie.hn.core.StoryType
import com.jamie.hn.core.extensions.visibleOrGone
import com.jamie.hn.core.ui.Article
import com.jamie.hn.core.ui.Ask
import com.jamie.hn.core.ui.Jobs
import com.jamie.hn.core.ui.New
import com.jamie.hn.core.ui.SharedNavigationViewModel
import com.jamie.hn.core.ui.Show
import com.jamie.hn.core.ui.Top
import com.jamie.hn.databinding.CommentListFragmentBinding
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class CommentsListFragment : Fragment(R.layout.comment_list_fragment) {

    private val viewModel: CommentsListViewModel by viewModel {
        parametersOf(storyId)
    }
    private val sharedNavigationViewModel by sharedViewModel<SharedNavigationViewModel>()

    private val storyId: Int
        get() = arguments?.get("storyId") as Int

    private val storyListType: StoriesListType
        get() = arguments?.get("storiesListType") as StoriesListType

    private val storyType: StoryType
        get() = arguments?.get("storyType") as StoryType

    private var binding: CommentListFragmentBinding? = null
    private lateinit var commentsListAdapter: CommentsListAdapter
    private var currentSortState: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CommentListFragmentBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                    R.id.sort -> {
                        showSortComments()
                        true
                    }
                    R.id.share -> {
                        showShareOptions()
                        true
                    }
                    else -> false
                }
            }

            it.commentSwipeLayout.setOnRefreshListener {
                viewModel.userManuallyRefreshed()
            }
        }

        viewModel.init(storyListType, storyType)
        initialiseLiveDataObservers(view)
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun initialiseLiveDataObservers(view: View) {
        viewModel.commentsViewState().observe(viewLifecycleOwner, Observer { item ->
            binding?.let {
                it.progressBar.visibleOrGone(item.refreshing)
                it.commentSwipeLayout.isRefreshing = item.refreshing
                it.commentsList.visibleOrGone(!item.refreshing)
                it.commentListError.visibleOrGone(false)
            }
            commentsListAdapter.data(item.comments)
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
                sharedNavigationViewModel.navigate(Article(url))
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

        viewModel.sortState().observe(viewLifecycleOwner, Observer<Int> {
            currentSortState = it
        })

        binding?.topAppBar?.setNavigationOnClickListener {
            sharedNavigationViewModel.navigationIconSelected()
        }

        sharedNavigationViewModel.navigateNextScreen().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { nextScreen ->
                val action =
                    when (nextScreen) {
                        is Article ->
                            CommentsListFragmentDirections.actionCommentsListToArticleViewer(
                                nextScreen.url
                            )
                        is Top, Ask, Jobs, New, Show ->
                            CommentsListFragmentDirections.actionCommentsListToStoriesList(
                                nextScreen
                            )
                        else -> throw IllegalArgumentException("Unsupported screen chosen")
                    }
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
        val browserIntent = Intent(ACTION_VIEW, uri)
        startActivity(browserIntent)
    }

    private fun showShareOptions() {
        val context = context ?: return
        val shareOptions = arrayOf(
            getString(R.string.comments_app_bar_share_article),
            getString(R.string.comments_app_bar_share_comments),
            getString(R.string.comments_app_bar_share_article_comments)
        )

        MaterialAlertDialogBuilder(context)
            .setTitle(resources.getString(R.string.comments_app_bar_share))
            .setItems(shareOptions) { _, which ->
                viewModel.share(which)
            }.show()
    }

    private fun showSortComments() {
        val context = context ?: return
        val sortOptions = arrayOf(
            getString(R.string.comments_standard),
            getString(R.string.comments_newest),
            getString(R.string.comments_oldest)
        )
        var sortSelected: Int = -1

        MaterialAlertDialogBuilder(context)
            .setTitle(resources.getString(R.string.comments_app_bar_sort))
            .setNeutralButton(resources.getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(resources.getString(R.string.confirm)) { _, _ ->
                // We sort at the point of VM retrieving the comments from the repository
                viewModel.updateSortState(sortSelected)
                viewModel.automaticallyRefreshed()
            }
            .setSingleChoiceItems(sortOptions, currentSortState) { _, which ->
                sortSelected = which
            }.show()
    }
}

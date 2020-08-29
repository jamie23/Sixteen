package com.jamie.hn.stories.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.jamie.hn.R
import com.jamie.hn.core.extensions.visibleOrGone
import kotlinx.android.synthetic.main.story_list_fragment.*
import kotlinx.android.synthetic.main.story_list_fragment.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class StoryListFragment : Fragment(R.layout.story_list_fragment) {
    private val viewModel: StoryListViewModel by viewModel()

    private lateinit var storyListAdapter: StoryListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storyListAdapter = StoryListAdapter()

        view.storyList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = storyListAdapter
        }

        viewModel.automaticallyRefreshed()

        viewModel.storyListViewState().observe(viewLifecycleOwner, Observer {
            progressBar.visibleOrGone(it.refreshing)
            storySwipeLayout.isRefreshing = it.refreshing
            storyList.visibleOrGone(!it.refreshing)
            storyListError.visibleOrGone(it.showNoCachedStoryNetworkError)
            storyListAdapter.data(it.stories)
        })

        storySwipeLayout.setOnRefreshListener {
            viewModel.userManuallyRefreshed()
        }

        viewModel.navigateToComments().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { storyId ->
                val action = StoryListFragmentDirections.actionStoriesListToCommentsList(storyId)
                view.findNavController().navigate(action)
            }
        })

        viewModel.navigateToArticle().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { url ->
                val action = StoryListFragmentDirections.actionStoriesListToArticleViewer(url)
                view.findNavController().navigate(action)
            }
        })

        viewModel.cachedStoriesNetworkError().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                showNetworkFailureError(storyList)
            }
        })
    }

    private fun showNetworkFailureError(view: View) {
        Snackbar.make(view, R.string.stories_network_cached_error, Snackbar.LENGTH_LONG).show()
    }
}

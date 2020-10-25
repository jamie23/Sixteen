package com.jamie.hn.stories.ui

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
import com.jamie.hn.databinding.StoryListFragmentBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class StoryListFragment : Fragment(R.layout.story_list_fragment) {
    private val viewModel: StoryListViewModel by viewModel()

    private var binding: StoryListFragmentBinding? = null

    private lateinit var storyListAdapter: StoryListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = StoryListFragmentBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storyListAdapter = StoryListAdapter()

        binding?.let {
            it.storyList.setHasFixedSize(true)
            it.storyList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            it.storyList.adapter = storyListAdapter
        }

        viewModel.automaticallyRefreshed()

        viewModel.storyListViewState().observe(viewLifecycleOwner, Observer {
            binding?.progressBar?.visibleOrGone(it.refreshing)
            binding?.storySwipeLayout?.isRefreshing = it.refreshing
            binding?.storyList?.visibleOrGone(!it.refreshing)
            binding?.storyListError?.visibleOrGone(it.showNoCachedStoryNetworkError)
            storyListAdapter.data(it.stories)
        })

        binding?.storySwipeLayout?.setOnRefreshListener {
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
                Snackbar.make(view, R.string.stories_network_cached_error, Snackbar.LENGTH_LONG).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

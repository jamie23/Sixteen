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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.jamie.hn.R
import com.jamie.hn.core.extensions.visibleOrGone
import com.jamie.hn.core.ui.Article
import com.jamie.hn.core.ui.Ask
import com.jamie.hn.core.ui.Comments
import com.jamie.hn.core.ui.Jobs
import com.jamie.hn.core.ui.New
import com.jamie.hn.core.ui.Screen
import com.jamie.hn.core.ui.SharedNavigationViewModel
import com.jamie.hn.core.ui.Show
import com.jamie.hn.core.ui.Top
import com.jamie.hn.databinding.StoryListFragmentBinding
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class StoryListFragment : Fragment(R.layout.story_list_fragment) {

    private val viewModel: StoryListViewModel by viewModel()
    private val sharedNavigationViewModel by sharedViewModel<SharedNavigationViewModel>()

    private var binding: StoryListFragmentBinding? = null

    private lateinit var storyListAdapter: StoryListAdapter
    private var currentSortState: Int = 0
    private val currentScreen: Screen
        get() = (arguments?.get("nextScreen") ?: Top) as Screen

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

        sharedNavigationViewModel.currentScreen = currentScreen
        storyListAdapter = StoryListAdapter()

        binding?.let {
            it.storyList.setHasFixedSize(true)
            it.storyList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            it.storyList.adapter = storyListAdapter
        }

        binding?.storySwipeLayout?.setOnRefreshListener {
            viewModel.userManuallyRefreshed()
        }

        binding?.topAppBar?.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.sortStories) {
                showSortStories()
                true
            } else {
                false
            }
        }

        binding?.topAppBar?.setNavigationOnClickListener {
            sharedNavigationViewModel.navigationIconSelected()
        }

        viewModel.storyListViewState().observe(viewLifecycleOwner, Observer { item ->
            binding?.let {
                it.progressBar.visibleOrGone(item.refreshing)
                it.storySwipeLayout.isRefreshing = item.refreshing
                it.storyList.visibleOrGone(!item.refreshing)
                it.storyListError.visibleOrGone(item.showNoCachedStoryNetworkError)
            }
            storyListAdapter.data(item.stories)
        })

        viewModel.navigateToComments().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { storyData ->
                sharedNavigationViewModel.navigate(
                    Comments(
                        storyData.storyId,
                        storyData.storiesListType,
                        storyData.storyType
                    )
                )
            }
        })

        viewModel.navigateToArticle().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { url ->
                sharedNavigationViewModel.navigate(Article(url))
            }
        })

        viewModel.cachedStoriesNetworkError().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                Snackbar.make(view, R.string.stories_network_cached_error, Snackbar.LENGTH_LONG)
                    .show()
            }
        })

        viewModel.sortState().observe(viewLifecycleOwner, Observer {
            currentSortState = it
        })

        viewModel.toolbarTitle().observe(viewLifecycleOwner, Observer {
            binding?.topAppBar?.title = it
        })

        sharedNavigationViewModel.navigateNextScreen().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { nextScreen ->
                val action =
                    when (nextScreen) {
                        is Comments ->
                            StoryListFragmentDirections.actionStoriesListToCommentsList(
                                nextScreen.storyId, nextScreen.storiesListType, nextScreen.storyType
                            )
                        is Article ->
                            StoryListFragmentDirections.actionStoriesListToArticleViewer(nextScreen.url)
                        is Top, Ask, Jobs, New, Show ->
                            StoryListFragmentDirections.actionStoriesListToStoriesList(nextScreen)
                    }

                view.findNavController().navigate(action)
            }
        })

        viewModel.initialise(currentScreen)
    }

    private fun showSortStories() {
        val context = context ?: return
        val sortOptions = arrayOf(
            getString(R.string.stories_standard),
            getString(R.string.stories_newest),
            getString(R.string.stories_oldest)
        )
        var sortSelected: Int = -1

        MaterialAlertDialogBuilder(context)
            .setTitle(resources.getString(R.string.stories_app_bar_sort))
            .setNeutralButton(resources.getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(resources.getString(R.string.confirm)) { _, _ ->
                // We sort at the point of VM retrieving the stories from the repository
                viewModel.updateSortState(sortSelected)
                viewModel.userManuallyRefreshed()
            }
            .setSingleChoiceItems(sortOptions, currentSortState) { _, which ->
                sortSelected = which
            }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

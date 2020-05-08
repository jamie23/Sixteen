package com.jamie.hn.articles.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
import com.jamie.hn.R
import com.jamie.hn.core.extensions.visibleOrGone
import kotlinx.android.synthetic.main.article_list_fragment.*
import kotlinx.android.synthetic.main.article_list_fragment.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ArticleListFragment : Fragment(R.layout.article_list_fragment) {
    private val viewModel: ArticlesListViewModel by viewModel()

    private lateinit var articleListAdapter: ArticleListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        articleListAdapter = ArticleListAdapter()

        view.articleList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = articleListAdapter
        }

        viewModel.automaticallyRefreshed()

        viewModel.articleViewState().observe(viewLifecycleOwner, Observer {
            progressBar.visibleOrGone(it.refreshing)
            articleSwipeLayout.isRefreshing = it.refreshing
            articleList.visibleOrGone(!it.refreshing)
            articleListAdapter.data(it.articles)
            articleListAdapter.stateRestorationPolicy = PREVENT_WHEN_EMPTY
        })

        articleSwipeLayout.setOnRefreshListener {
            viewModel.userManuallyRefreshed()
        }

        viewModel.navigateToComments().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { article ->
                val action = ArticleListFragmentDirections.actionArticlesListToCommentsList(article)
                view.findNavController().navigate(action)
            }
        })

        viewModel.navigateToArticle().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { url ->
                val action = ArticleListFragmentDirections.actionArticlesListToArticleViewer(url)
                view.findNavController().navigate(action)
            }
        })
    }
}

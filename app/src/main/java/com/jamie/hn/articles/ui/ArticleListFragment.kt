package com.jamie.hn.articles.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jamie.hn.R
import kotlinx.android.synthetic.main.article_list_fragment.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ArticleListFragment : Fragment() {
    private val viewModel: ArticlesListViewModel by viewModel()

    private lateinit var articleListAdapter: ArticleListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.article_list_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        articleListAdapter = ArticleListAdapter()

        view.articleList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = articleListAdapter
        }

        viewModel.init()

        viewModel.articles().observe(viewLifecycleOwner, Observer {
            articleListAdapter.data(it)
        })

        viewModel.navigateToComments().observe(viewLifecycleOwner, Observer {
           it.getContentIfNotHandled()?.let { id ->
                println(id)
           }
        })
    }
}

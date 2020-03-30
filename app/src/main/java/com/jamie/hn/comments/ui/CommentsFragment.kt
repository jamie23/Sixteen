package com.jamie.hn.comments.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jamie.hn.R
import com.jamie.hn.articles.domain.Article
import kotlinx.android.synthetic.main.comment_list_fragment.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class CommentsFragment : Fragment(R.layout.comment_list_fragment) {
    private val article: Article
        get() = arguments?.get("article") as Article

    private lateinit var commentListAdapter: CommentListAdapter

    private val viewModel: CommentsListViewModel by viewModel {
        parametersOf(article)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        commentListAdapter = CommentListAdapter()

        view.commentsList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = commentListAdapter
        }

        viewModel.init()
    }
}

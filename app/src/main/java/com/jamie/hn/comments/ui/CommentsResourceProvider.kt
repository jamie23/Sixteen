package com.jamie.hn.comments.ui

import android.content.res.Resources
import com.jamie.hn.R

class CommentsResourceProvider(
    private val resources: Resources
) {
    fun hidden() = resources.getString(R.string.comments_hidden)
    fun article() = resources.getString(R.string.comments_share_article)
    fun comments() = resources.getString(R.string.comments_share_comments)
}

package com.jamie.hn.comments.ui

import android.content.res.Resources
import com.jamie.hn.R

class CommentsResourceProvider(
    private val resources: Resources
) {
    fun children() = resources.getString(R.string.comments_children)
}

package com.jamie.hn.articles.ui

import android.content.res.Resources
import com.jamie.hn.R

class ArticleResourceProvider(
    private val resources: Resources
) {
    val days = resources.getString(R.string.days)
    val hours = resources.getString(R.string.hours)
    val minutes = resources.getString(R.string.minutes)

    fun comments(numComments: Int) =
        resources.getQuantityString(R.plurals.comments_plural, numComments, numComments)
}

package com.jamie.hn.stories.ui

import android.content.res.Resources
import com.jamie.hn.R

class StoryResourceProvider(
    private val resources: Resources
) {
    fun comments(numComments: Int) =
        resources.getQuantityString(R.plurals.comments_plural, numComments, numComments)

    fun score(score: Int) =
        resources.getQuantityString(R.plurals.score_plural, score)

    fun topTitle() =
        resources.getString(R.string.stories_title_top)

    fun askTitle() =
        resources.getString(R.string.stories_title_ask)

    fun jobsTitle() =
        resources.getString(R.string.stories_title_jobs)

    fun newTitle() =
        resources.getString(R.string.stories_title_new)

    fun showTitle() =
        resources.getString(R.string.stories_title_show)
}

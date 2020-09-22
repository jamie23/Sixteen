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
}

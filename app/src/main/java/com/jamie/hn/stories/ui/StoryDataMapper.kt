package com.jamie.hn.stories.ui

import com.jamie.hn.core.StoryType
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.core.ui.CoreDataMapper

class StoryDataMapper(
    private val coreDataMapper: CoreDataMapper,
    private val resourceProvider: StoryResourceProvider
) {

    fun toStoryViewItem(
        story: Story,
        commentsCallback: (Int) -> Unit,
        storyViewerCallback: (Int) -> Unit
    ) =
        StoryViewItem(
            id = story.id,
            author = story.author,
            comments = comments(story.commentCount),
            score = story.score.toString(),
            scoreText = scoreText(story.score),
            time = coreDataMapper.time(story.time),
            title = story.title,
            url = story.domain,
            showNavigateToArticle = story.type != StoryType.ASK,
            commentsCallback = commentsCallback,
            storyViewerCallback = storyViewerCallback
        )

    private fun comments(numComments: Int) = resourceProvider.comments(numComments)
    private fun scoreText(score: Int) = resourceProvider.score(score)
}

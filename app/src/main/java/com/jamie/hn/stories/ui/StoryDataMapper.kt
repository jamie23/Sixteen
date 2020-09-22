package com.jamie.hn.stories.ui

import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.core.ui.CoreDataMapper

class StoryDataMapper(
    private val coreDataMapper: CoreDataMapper,
    private val resourceProvider: StoryResourceProvider
) {

    fun toStoryViewItem(
        story: Story,
        commentsCallback: (Long) -> Unit,
        storyViewerCallback: (Long) -> Unit
    ): StoryViewItem {
        return StoryViewItem(
            story.id,
            story.author,
            comments(story.commentCount),
            story.score.toString(),
            scoreText(story.score),
            coreDataMapper.time(story.time),
            story.title,
            story.domain,
            commentsCallback,
            storyViewerCallback
        )
    }

    private fun comments(numComments: Int) = resourceProvider.comments(numComments)
    private fun scoreText(score: Int) = resourceProvider.score(score)
}

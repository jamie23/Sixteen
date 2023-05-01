package com.jamie.hn.stories.repository.local.fake

import com.jamie.hn.comments.domain.model.Comment
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.stories.repository.local.LocalSource
import org.joda.time.DateTime

class FakeLocalStorage(
    override var topStoryList: List<Story> = emptyList(),
    override var askStoryList: List<Story> = emptyList(),
    override var jobsStoryList: List<Story> = emptyList(),
    override var newStoryList: List<Story> = emptyList(),
    override var showStoryList: List<Story> = emptyList()
) : LocalSource {
    init {
        topStoryList = fakeStories()
    }

    private fun fakeStories() = listOf(
        Story(
            author = "jamie23",
            comments = fakeComments(),
            commentCount = 6,
            domain = "github.com",
            score = 42,
            time = DateTime.now(),
            title = "Irish developer builds app to read hacker news",
            url = "github.com/jamie23"
        ),
        Story(
            author = "hackerhunter",
            comments = fakeComments(),
            commentCount = 36,
            domain = "dropbox.com",
            score = 82,
            time = DateTime.now(),
            title = "Dropbox to shut after hacker news tell them that anyone can build Dropbox themselves",
            url = "dropbox.com"
        ),
        Story(
            author = "technerd",
            comments = fakeComments(),
            commentCount = 1,
            domain = "ddg.gg",
            score = 50,
            time = DateTime.now(),
            title = "Revolutionary DDG IA takes market by storm",
            url = "https://github.com/duckduckgo/zeroclickinfo-goodies/pull/1495"
        ),
        Story(
            author = "cybercrimeexpert",
            comments = fakeComments(),
            commentCount = 2,
            domain = "malwarebyte.com",
            score = 42,
            time = DateTime.now(),
            title = "Major Data Breach Exposes Millions of User Records",
            url = "malwarebytes.com"
        ),
        Story(
            author = "hacktivist",
            comments = fakeComments(),
            commentCount = 36,
            domain = "dropbox.com",
            score = 82,
            time = DateTime.now(),
            title = "New Cybersecurity Report Shows Increase in Ransomware Attacks",
            url = "stripe.com"
        ),
        Story(
            author = "cyberdefender",
            comments = fakeComments(),
            commentCount = 5,
            domain = "darkreading.com",
            score = 5,
            time = DateTime.now(),
            title = "Critical Vulnerabilities Found in Popular Software",
            url = "https://github.com/duckduckgo/zeroclickinfo-goodies/pull/1495"
        ),
        Story(
            author = "securityguru",
            comments = fakeComments(),
            commentCount = 104,
            domain = "msn.com",
            score = 14,
            time = DateTime.now(),
            title = "Google's Latest AI-Powered Cybersecurity Tool Aims to Predict and Prevent Cyberattacks",
            url = "msn.com"
        )
    )

    private fun fakeComments() = listOf(
        Comment(
            author = "hacker007",
            comments = listOf(
                Comment(
                    author = "hackerhunter",
                    comments = listOf(
                        Comment(
                            author = "hackerhunter",
                            commentCount = 0,
                            text = "I am a nested comment",
                            time = DateTime.now()
                        )
                    ),
                    commentCount = 1,
                    text = "I am a nested comment",
                    time = DateTime.now()
                )
            ),
            commentCount = 1,
            text = "I am a top level comment",
            time = DateTime.now()
        ),
        exampleTopLevelComment(
            author = "technerd"
        ),
        exampleTopLevelComment(
            author = "cybercrimeexpert"
        ),
        exampleTopLevelComment(
            author = "securityguru"
        )
    )

    private fun exampleTopLevelComment(author: String) = Comment(
        author = author,
        commentCount = 0,
        text = "I'm a top level comment",
        time = DateTime.now()
    )
}

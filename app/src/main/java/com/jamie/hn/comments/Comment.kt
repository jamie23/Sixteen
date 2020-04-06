package com.jamie.hn.comments

import com.google.gson.annotations.SerializedName
import com.jamie.hn.core.Item

data class Comment(
    val by: String = "",
    val id: Long = 0,
    @SerializedName("kids")
    override val childrenIds: List<Long> = listOf(),
    val text: String = "",
    val time: Long = 0,
    var listChildComments: List<Comment> = listOf()
) : Item

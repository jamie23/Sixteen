package com.jamie.hn.comments.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime

@Parcelize
data class Comment(
    val author: String = "",
    val comments: List<Comment> = listOf(),
    val commentCount: Int,
    val text: String = "",
    val time: DateTime
) : Parcelable

package com.jamie.hn.core.ui

import android.os.Parcelable
import com.jamie.hn.core.StoriesListType
import kotlinx.android.parcel.Parcelize

sealed class Screen : Parcelable

@Parcelize
object Top : Screen()

@Parcelize
object Ask : Screen()

@Parcelize
object Jobs : Screen()

@Parcelize
object New : Screen()

@Parcelize
object Show : Screen()

@Parcelize
data class Comments(
    val storyId: Int,
    val storiesListType: StoriesListType
) : Screen()

@Parcelize
data class Article(val url: String) : Screen()

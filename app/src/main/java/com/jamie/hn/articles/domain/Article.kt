package com.jamie.hn.articles.domain

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.jamie.hn.core.Item
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Article(
    val by: String = "",
    val descendants: Int = 0,
    val id: Long = 0,
    val score: Int = 0,
    val time: Long = 0,
    val title: String = "",
    val url: String? = "",
    @SerializedName("kids")
    override val childrenIds: List<Long> = emptyList()
) : Item, Parcelable

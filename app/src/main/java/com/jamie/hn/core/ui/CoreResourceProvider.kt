package com.jamie.hn.core.ui

import android.content.res.Resources
import com.jamie.hn.R

data class CoreResourceProvider(
    val resources: Resources
) {
    val days = resources.getString(R.string.days)
    val hours = resources.getString(R.string.hours)
    val minutes = resources.getString(R.string.minutes)
}

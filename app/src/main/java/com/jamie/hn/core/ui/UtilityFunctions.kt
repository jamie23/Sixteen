package com.jamie.hn.core.ui

import android.content.Context
import android.util.TypedValue

fun convertDpToPixels(dp: Float, context: Context) =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        context.resources.displayMetrics
    )


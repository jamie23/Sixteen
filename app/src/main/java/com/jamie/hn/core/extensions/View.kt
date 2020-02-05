package com.jamie.hn.core.extensions

import android.view.View

fun View.visible(visible: Boolean) {
    visibility = when (visible) {
        true -> View.VISIBLE
        false -> View.INVISIBLE
    }
}
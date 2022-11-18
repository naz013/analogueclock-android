package com.github.naz013.clockapp.util

fun String.takeWithDots(size: Int): String {
    return if (this.length > size) {
        substring(0, size) + "..."
    } else {
        this
    }
}
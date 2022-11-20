package com.github.naz013.analoguewatch

import android.graphics.Rect

internal fun Rect.widthF(): Float {
    return width().toFloat()
}

internal fun Rect.centerXf(): Float {
    return centerX().toFloat()
}

internal fun Rect.centerYf(): Float {
    return centerY().toFloat()
}
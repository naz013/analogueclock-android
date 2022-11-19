package com.github.naz013.clockapp.animation

import android.animation.ValueAnimator
import android.view.animation.AccelerateInterpolator
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.github.naz013.clockapp.Params

class TextAnimator(
    private val animationListener: (String) -> Unit
) : DefaultLifecycleObserver {

    private var animation: ValueAnimator? = null

    fun animate(from: String, to: String) {
        animation?.cancel()

        ValueAnimator.ofInt(timeToInt(from), timeToInt(to)).apply {
            duration = Params.ANIMATION_DURATION
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                animationListener.invoke(intToTime(it.animatedValue as Int))
            }
        }
            .also { animation = it }
            .start()
    }

    private fun timeToInt(value: String): Int {
        return value.replace(":", "").trimStart('0').toInt()
    }

    private fun intToTime(value: Int): String {
        val withZeros = value.toString().padStart(4, '0')
        return withZeros.substring(0, 2) + ":" + withZeros.substring(2, 4)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        animation?.cancel()
    }
}
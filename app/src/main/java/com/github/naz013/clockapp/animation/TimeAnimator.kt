package com.github.naz013.clockapp.animation

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import androidx.core.animation.addListener
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.github.naz013.clockapp.Params
import com.github.naz013.analoguewatch.TimeData

class TimeAnimator(
    private val animationListener: (TimeData) -> Unit,
    private val animationEndListener: () -> Unit
) : DefaultLifecycleObserver {

    private var animation: ValueAnimator? = null

    fun animate(from: TimeData, to: TimeData) {
        animation?.cancel()

        val hourProp = PropertyValuesHolder.ofInt("hour", from.hour, to.hour)
        val minuteProp = PropertyValuesHolder.ofInt("minute", from.minute, to.minute)
        val secondProp = PropertyValuesHolder.ofInt("second", from.second, to.second)
        animation = ObjectAnimator.ofPropertyValuesHolder(hourProp, minuteProp, secondProp)
            .apply {
                duration = Params.ANIMATION_DURATION
                addUpdateListener { readData(it) }
                addListener(
                    onEnd = { animationEndListener.invoke() }
                )
            }.also { it.start() }
    }

    private fun readData(animator: ValueAnimator) {
        val hour = animator.getAnimatedValue("hour") as Int
        val minute = animator.getAnimatedValue("minute") as Int
        val second = animator.getAnimatedValue("second") as Int
        animationListener.invoke(TimeData(hour, minute, second))
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        animation?.cancel()
    }
}
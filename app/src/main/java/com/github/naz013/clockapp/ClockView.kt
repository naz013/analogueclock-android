package com.github.naz013.clockapp

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.Px
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

class ClockView constructor(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var mClockRect: Rect? = null
    private val mInnerCirclesRects: Array<Rect?> = arrayOfNulls<Rect>(2)
    private val mHourArrow: Path = Path()
    private val mMinuteArrow: Path = Path()
    private val mSecondArrow: Path = Path()
    private val mLabelPoints: Array<Point?> = arrayOfNulls<Point>(4)

    @ColorInt
    private var mBgColor: Int = Color.WHITE
    private var mCirclesColor: Int = Color.BLACK
    private var mArrowsColor: Int = Color.BLACK
    private var mHourLabelsColor: Int = Color.BLACK
    private var mShadowColor: Int = Color.RED

    private var mShowHourLabels = true
    private var mShowCircles = true
    private var mShowShadow = true

    private var mHour = 3
    private var mMinute = 0
    private var mSecond = 0

    private val mBgPaint: Paint = Paint()
    private val mShadowPaint: Paint = Paint()
    private val mLabelPaint: Paint = Paint()
    private val mArrowPaint: Paint = Paint()
    private val mCirclePaint: Paint = Paint()

    init {
        initView(context, attrs, defStyleAttr)
    }

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    fun setShowHourLabels(showHourLabels: Boolean) {
        mShowHourLabels = showHourLabels
        this.invalidate()
    }

    fun isShowHourLabels(): Boolean {
        return mShowHourLabels
    }

    fun setShowCircles(showCircles: Boolean) {
        mShowCircles = showCircles
        this.invalidate()
    }

    fun isShowCircles(): Boolean {
        return mShowShadow
    }

    fun setShowShadow(showShadow: Boolean) {
        mShowShadow = showShadow
        this.invalidate()
    }

    fun isShowShadow(): Boolean {
        return mShowShadow
    }

    fun setBgColor(@ColorInt bgColor: Int) {
        mBgColor = bgColor
        mShadowPaint.color = bgColor
        this.invalidate()
    }

    fun setCirclesColor(@ColorInt circlesColor: Int) {
        mCirclesColor = circlesColor
        mCirclePaint.color = circlesColor
        this.invalidate()
    }

    fun setArrowsColor(@ColorInt arrowsColor: Int) {
        mArrowsColor = arrowsColor
        mArrowPaint.color = arrowsColor
        this.invalidate()
    }

    fun setHourLabelsColor(@ColorInt hourLabelsColor: Int) {
        mHourLabelsColor = hourLabelsColor
        mShadowPaint.color = hourLabelsColor
        this.invalidate()
    }

    fun setShadowColor(@ColorInt shadowColor: Int) {
        mShadowColor = shadowColor
        mShadowPaint.color = shadowColor
        this.invalidate()
    }

    fun getTime(): Long {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, mHour)
        calendar.set(Calendar.MINUTE, mMinute)
        calendar.set(Calendar.SECOND, mSecond)
        return calendar.timeInMillis
    }

    fun setTimeMillis(millis: Long) {
        initTime(millis)
        this.invalidate()
    }

    fun setTime(hourOfDay: Int, minute: Int) {
        this.setTime(hourOfDay, minute, 0)
    }

    fun setTime(hourOfDay: Int, minute: Int, second: Int) {
        mHour = hourOfDay
        mMinute = minute
        mSecond = second
        this.invalidate()
    }

    private fun initView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) {
        initTime(System.currentTimeMillis())
        val textSize = 25f
        mShadowPaint.isAntiAlias = true
        mShadowPaint.color = mShadowColor
        mShadowPaint.maskFilter = BlurMaskFilter(
            dp2px(SHADOW_RADIUS).toFloat(),
            BlurMaskFilter.Blur.OUTER
        )
        mShadowPaint.style = Paint.Style.FILL
        setLayerType(LAYER_TYPE_HARDWARE, mShadowPaint)

        mArrowPaint.isAntiAlias = true
        mArrowPaint.color = mArrowsColor
        mArrowPaint.style = Paint.Style.FILL

        mLabelPaint.isAntiAlias = true
        mLabelPaint.color = mHourLabelsColor
        mLabelPaint.style = Paint.Style.FILL_AND_STROKE
        mLabelPaint.textSize = textSize

        mCirclePaint.isAntiAlias = true
        mCirclePaint.color = mCirclesColor
        mCirclePaint.style = Paint.Style.STROKE

        mBgPaint.isAntiAlias = true
        mBgPaint.color = mBgColor
        mBgPaint.style = Paint.Style.FILL
    }

    private fun initTime(millis: Long) {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        mHour = calendar.get(Calendar.HOUR_OF_DAY)
        mMinute = calendar.get(Calendar.MINUTE)
        mSecond = calendar.get(Calendar.SECOND)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        log("onDraw: canvas=$canvas")
        log("onDraw: w=$width, h=$height")
        if (canvas == null) return
        val millis = System.currentTimeMillis()
//        canvas?.drawColor(Color.RED)
        if (mShowShadow) {
            drawClockShadow(canvas)
        }
        drawClockBackground(canvas)
        if (mShowCircles) {
            drawInnerCircles(canvas)
        }
        if (mShowHourLabels) {
            drawHourLabels(canvas)
        }
        drawHourArrow(canvas)
        drawMinuteArrow(canvas)
        drawSecondArrow(canvas)
        log("onDraw: duration=${System.currentTimeMillis() - millis}")
    }

    private fun drawSecondArrow(canvas: Canvas) {
        mClockRect?.takeIf { !mSecondArrow.isEmpty }
            ?.also { rect ->
                canvas.save()
                canvas.rotate(secondAngle(), rect.centerXf(), rect.centerYf())
                canvas.drawPath(mSecondArrow, mArrowPaint)
                canvas.restore()
            }
    }

    private fun drawMinuteArrow(canvas: Canvas) {
        mClockRect?.takeIf { !mMinuteArrow.isEmpty }
            ?.also { rect ->
                canvas.save()
                canvas.rotate(minuteAngle(), rect.centerXf(), rect.centerYf())
                canvas.drawPath(mMinuteArrow, mArrowPaint)
                canvas.restore()
            }
    }

    private fun drawHourArrow(canvas: Canvas) {
        mClockRect?.takeIf { !mHourArrow.isEmpty }
            ?.also { rect ->
                canvas.save()
                canvas.rotate(hourAngle(), rect.centerXf(), rect.centerYf())
                canvas.drawPath(mHourArrow, mArrowPaint)
                canvas.restore()
            }
    }

    private fun drawHourLabels(canvas: Canvas) {
        var p: Point? = mLabelPoints[0]
        if (p != null) {
            drawText(canvas, p, "12")
        }
        p = mLabelPoints[1]
        if (p != null) {
            drawText(canvas, p, "3")
        }
        p = mLabelPoints[2]
        if (p != null) {
            drawText(canvas, p, "6")
        }
        p = mLabelPoints[3]
        if (p != null) {
            drawText(canvas, p, "9")
        }
    }

    private fun drawInnerCircles(canvas: Canvas) {
        for (rect in mInnerCirclesRects) {
            if (rect != null) {
                canvas.drawCircle(rect.centerXf(), rect.centerYf(), rect.width() / 2f, mCirclePaint)
            }
        }
    }

    private fun drawClockBackground(canvas: Canvas) {
        mClockRect?.also { rect ->
            canvas.drawCircle(
                rect.centerXf(),
                rect.centerYf(),
                rect.width() / 2f,
                mBgPaint
            )
        }
    }

    private fun drawClockShadow(canvas: Canvas) {
        mClockRect?.also { rect ->
            canvas.drawCircle(
                rect.centerXf(),
                rect.centerYf(),
                rect.width() / 2f,
                mShadowPaint
            )
        }
    }

    private fun drawText(canvas: Canvas, p: Point, text: String) {
        val r = Rect()
        mLabelPaint.textAlign = Paint.Align.LEFT
        mLabelPaint.getTextBounds(text, 0, text.length, r)
        val x: Float = p.x - r.width() / 2f - r.left
        val y: Float = p.y + r.height() / 2f - r.bottom
        canvas.drawText(text, x, y, mLabelPaint)
    }

    private fun hourAngle(): Float {
        var angle = 0.0f
        var hour = mHour
        if (validateValue(hour, 0, 23)) {
            if (hour > 11) {
                hour -= 12
            }
            var minutes = hourToMinutes(hour)
            if (validateValue(mMinute, 0, 59)) {
                minutes += mMinute
            }
            angle = minutes.toFloat() * 0.5f
        }
        log("hourAngle: angle=$angle, hour=$mHour")
        return angle
    }

    private fun minuteAngle(): Float {
        if (mMinute == 0) return 0.0f
        var angle = 0.0f
        val minute = mMinute
        if (validateValue(minute, 0, 59)) {
            angle = minute.toFloat() * 6f
        }
        log("minuteAngle: angle=$angle, minute=$mMinute")
        return angle
    }

    private fun secondAngle(): Float {
        if (mSecond == 0) return 0.0f
        var angle = 0.0f
        val second = mSecond
        if (validateValue(second, 0, 59)) {
            angle = second.toFloat() * 6f
        }
        log("secondAngle: angle=$angle, minute=$mSecond")
        return angle
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val measuredWidth = measuredWidth
        setMeasuredDimension(measuredWidth, measuredWidth)
        log("onMeasure: width=$measuredWidth, height=$measuredHeight")
        processCalculations(measuredWidth)
    }

    private fun processCalculations(width: Int) {
        if (width <= 0) return
        val margin = (width.toFloat() * 0.05f / 2f).toInt()
        val clockRect = Rect(margin, margin, width - margin, width - margin)
        mClockRect = clockRect

        log("processCalculations: clockRect=$mClockRect")

        val middleCircleMargin = (clockRect.widthF() * 0.33f / 2f).toInt()
        val smallCircleMargin = (clockRect.widthF() * 0.66f / 2f).toInt()
        mInnerCirclesRects[0] = Rect(
            clockRect.left + middleCircleMargin, clockRect.top + middleCircleMargin,
            clockRect.right - middleCircleMargin, clockRect.bottom - middleCircleMargin
        )
        mInnerCirclesRects[1] = Rect(
            clockRect.left + smallCircleMargin, clockRect.top + smallCircleMargin,
            clockRect.right - smallCircleMargin, clockRect.bottom - smallCircleMargin
        )
        val mLabelLength = (clockRect.widthF() * 0.85f / 2f).toInt()
        mLabelPoints[0] =
            circlePoint(clockRect.centerX(), clockRect.centerY(), mLabelLength, 270f)
        mLabelPoints[1] = circlePoint(clockRect.centerX(), clockRect.centerY(), mLabelLength, 0f)
        mLabelPoints[2] = circlePoint(clockRect.centerX(), clockRect.centerY(), mLabelLength, 90f)
        mLabelPoints[3] =
            circlePoint(clockRect.centerX(), clockRect.centerY(), mLabelLength, 180f)
        val mHourArrowWidth = (clockRect.widthF() * HOUR_ARROW_WIDTH).toInt()
        val mHourArrowLength = (clockRect.widthF() / 2f * HOUR_ARROW_LENGTH).toInt()
        val mMinuteArrowWidth = (clockRect.widthF() * MINUTE_ARROW_WIDTH).toInt()
        val mMinuteArrowLength = (clockRect.widthF() / 2f * MINUTE_ARROW_LENGTH).toInt()
        val mSecondArrowWidth = (clockRect.widthF() * SECOND_ARROW_WIDTH).toInt()
        val mSecondArrowLength = (clockRect.widthF() / 2f * SECOND_ARROW_LENGTH).toInt()
        create(
            mSecondArrow,
            clockRect.centerX(),
            clockRect.centerY(),
            mSecondArrowWidth,
            mSecondArrowLength
        )
        create(
            mMinuteArrow,
            clockRect.centerX(),
            clockRect.centerY(),
            mMinuteArrowWidth,
            mMinuteArrowLength
        )
        create(
            mHourArrow,
            clockRect.centerX(),
            clockRect.centerY(),
            mHourArrowWidth,
            mHourArrowLength
        )
    }

    private fun create(path: Path, cx: Int, cy: Int, width: Int, length: Int) {
        path.reset()
        path.moveTo(cx + width / 2f, cy.toFloat())
        path.lineTo(cx.toFloat(), cy.toFloat() - length)
        path.lineTo(cx - width / 2f, cy.toFloat())
        path.lineTo(cx.toFloat(), cy + length * 0.1f)
        path.lineTo(cx + width / 2f, cy.toFloat())
        path.close()
    }

    private fun circlePoint(cx: Int, cy: Int, length: Int, angle: Float): Point {
        val rad = Math.toRadians(angle.toDouble())
        val circleX = cx + length * cos(rad)
        val circleY = cy + length * sin(rad)
        return Point(circleX.toInt(), circleY.toInt())
    }

    @Px
    private fun dp2px(dp: Int): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager?
        var display: Display? = null
        if (wm != null) {
            display = wm.defaultDisplay
        }
        val displaymetrics = DisplayMetrics()
        display?.getMetrics(displaymetrics)
        return (dp * displaymetrics.density + 0.5f).toInt()
    }

    private fun hourToMinutes(hour: Int): Int {
        return hour * 60
    }

    private fun validateValue(value: Int, from: Int, to: Int): Boolean {
        return value in from..to
    }

    private fun log(message: String) {
        Log.d(TAG, message)
    }

    companion object {
        private const val TAG = "ClockView"
        private const val SHADOW_RADIUS = 15
        private const val HOUR_ARROW_WIDTH = 0.05f
        private const val MINUTE_ARROW_WIDTH = 0.03f
        private const val SECOND_ARROW_WIDTH = 0.02f
        private const val HOUR_ARROW_LENGTH = 0.55f
        private const val MINUTE_ARROW_LENGTH = 0.75f
        private const val SECOND_ARROW_LENGTH = 0.35f
    }
}

private fun Rect.widthF(): Float {
    return width().toFloat()
}

private fun Rect.centerXf(): Float {
    return centerX().toFloat()
}

private fun Rect.centerYf(): Float {
    return centerY().toFloat()
}

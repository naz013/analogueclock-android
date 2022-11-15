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

    private var _time: ClockTime = ClockTime()
    private var _colors: ClockColors = ClockColors()
    private var _params: ClockParams = ClockParams()

    private var _clockRect: Rect? = null
    private val _hourTick: Path = Path()
    private val _minuteTick: Path = Path()
    private val _secondTick: Path = Path()
    private val _labelPoints: Array<Point?> = arrayOfNulls(4)

    private val _backgroundPaint: Paint = Paint()
    private val _shadowPaint: Paint = Paint()
    private val _labelPaint: Paint = Paint()
    private val _tickPaint: Paint = Paint()

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

    fun getTimeMillis(): Long {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, _time.hour)
        calendar.set(Calendar.MINUTE, _time.minute)
        calendar.set(Calendar.SECOND, _time.second)
        return calendar.timeInMillis
    }

    fun setMillis(millis: Long) {
        initTime(millis)
        this.invalidate()
    }

    fun setTime(hourOfDay: Int, minute: Int) {
        this.setTime(hourOfDay, minute, 0)
    }

    fun setTime(hourOfDay: Int, minute: Int, second: Int) {
        _time = ClockTime(hourOfDay, minute, second)
        this.invalidate()
    }

    private fun initView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) {
        initTime(System.currentTimeMillis())
        val textSize = 25f
        _shadowPaint.isAntiAlias = true
        _shadowPaint.maskFilter = BlurMaskFilter(
            dp2px(SHADOW_RADIUS).toFloat(),
            BlurMaskFilter.Blur.OUTER
        )
        _shadowPaint.style = Paint.Style.FILL
        setLayerType(LAYER_TYPE_HARDWARE, _shadowPaint)

        _tickPaint.isAntiAlias = true
        _tickPaint.style = Paint.Style.FILL

        _labelPaint.isAntiAlias = true
        _labelPaint.style = Paint.Style.FILL_AND_STROKE
        _labelPaint.textSize = textSize

        _backgroundPaint.isAntiAlias = true
        _backgroundPaint.style = Paint.Style.FILL
    }

    private fun initTime(millis: Long) {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        _time = ClockTime(
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND)
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        log("onDraw: canvas=$canvas")
        log("onDraw: w=$width, h=$height")
        if (canvas == null) return
        val millis = System.currentTimeMillis()
//        canvas?.drawColor(Color.RED)
        if (_params.showShadow) {
            drawClockShadow(canvas)
        }
        drawClockBackground(canvas)
        if (_params.showHourLabels) {
            drawHourLabels(canvas)
        }
        drawHourArrow(canvas)
        drawMinuteArrow(canvas)
        if (_params.showSecondsTick) {
            drawSecondArrow(canvas)
        }
        log("onDraw: duration=${System.currentTimeMillis() - millis}")
    }

    private fun drawSecondArrow(canvas: Canvas) {
        _clockRect?.takeIf { !_secondTick.isEmpty }
            ?.also { rect ->
                canvas.save()
                canvas.rotate(secondAngle(), rect.centerXf(), rect.centerYf())
                _tickPaint.color = _colors.secondsTickColor
                canvas.drawPath(_secondTick, _tickPaint)
                canvas.restore()
            }
    }

    private fun drawMinuteArrow(canvas: Canvas) {
        _clockRect?.takeIf { !_minuteTick.isEmpty }
            ?.also { rect ->
                canvas.save()
                canvas.rotate(minuteAngle(), rect.centerXf(), rect.centerYf())
                _tickPaint.color = _colors.minuteTickColor
                canvas.drawPath(_minuteTick, _tickPaint)
                canvas.restore()
            }
    }

    private fun drawHourArrow(canvas: Canvas) {
        _clockRect?.takeIf { !_hourTick.isEmpty }
            ?.also { rect ->
                canvas.save()
                canvas.rotate(hourAngle(), rect.centerXf(), rect.centerYf())
                _tickPaint.color = _colors.hourTickColor
                canvas.drawPath(_hourTick, _tickPaint)
                canvas.restore()
            }
    }

    private fun drawHourLabels(canvas: Canvas) {
        var p: Point? = _labelPoints[0]
        if (p != null) {
            drawText(canvas, p, "12")
        }
        p = _labelPoints[1]
        if (p != null) {
            drawText(canvas, p, "3")
        }
        p = _labelPoints[2]
        if (p != null) {
            drawText(canvas, p, "6")
        }
        p = _labelPoints[3]
        if (p != null) {
            drawText(canvas, p, "9")
        }
    }

    private fun drawClockBackground(canvas: Canvas) {
        _clockRect?.also { rect ->
            _backgroundPaint.color = _colors.clockColor
            canvas.drawCircle(
                rect.centerXf(),
                rect.centerYf(),
                rect.width() / 2f,
                _backgroundPaint
            )
        }
    }

    private fun drawClockShadow(canvas: Canvas) {
        _clockRect?.also { rect ->
            _shadowPaint.color = _colors.shadowColor
            canvas.drawCircle(
                rect.centerXf(),
                rect.centerYf(),
                rect.width() / 2f,
                _shadowPaint
            )
        }
    }

    private fun drawText(canvas: Canvas, p: Point, text: String) {
        val r = Rect()
        _labelPaint.textAlign = Paint.Align.LEFT
        _labelPaint.getTextBounds(text, 0, text.length, r)
        _labelPaint.color = _colors.labelsColor
        val x: Float = p.x - r.width() / 2f - r.left
        val y: Float = p.y + r.height() / 2f - r.bottom
        canvas.drawText(text, x, y, _labelPaint)
    }

    private fun hourAngle(): Float {
        var angle = 0.0f
        var hour = _time.hour
        if (validateValue(hour, 0, 23)) {
            if (hour > 11) {
                hour -= 12
            }
            var minutes = hourToMinutes(hour)
            if (validateValue(_time.minute, 0, 59)) {
                minutes += _time.minute
            }
            angle = minutes.toFloat() * 0.5f
        }
        log("hourAngle: angle=$angle, hour=${_time.hour}")
        return angle
    }

    private fun minuteAngle(): Float {
        if (_time.minute == 0) return 0.0f
        var angle = 0.0f
        val minute = _time.minute
        if (validateValue(minute, 0, 59)) {
            angle = minute.toFloat() * 6f
        }
        log("minuteAngle: angle=$angle, minute=${_time.minute}")
        return angle
    }

    private fun secondAngle(): Float {
        if (_time.second == 0) return 0.0f
        var angle = 0.0f
        val second = _time.second
        if (validateValue(second, 0, 59)) {
            angle = second.toFloat() * 6f
        }
        log("secondAngle: angle=$angle, minute=${_time.second}")
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
        _clockRect = clockRect

        log("processCalculations: clockRect=$_clockRect")

        val mLabelLength = (clockRect.widthF() * 0.85f / 2f).toInt()
        _labelPoints[0] =
            circlePoint(clockRect.centerX(), clockRect.centerY(), mLabelLength, 270f)
        _labelPoints[1] = circlePoint(clockRect.centerX(), clockRect.centerY(), mLabelLength, 0f)
        _labelPoints[2] = circlePoint(clockRect.centerX(), clockRect.centerY(), mLabelLength, 90f)
        _labelPoints[3] =
            circlePoint(clockRect.centerX(), clockRect.centerY(), mLabelLength, 180f)
        val mHourArrowWidth = (clockRect.widthF() * HOUR_ARROW_WIDTH).toInt()
        val mHourArrowLength = (clockRect.widthF() / 2f * HOUR_ARROW_LENGTH).toInt()
        val mMinuteArrowWidth = (clockRect.widthF() * MINUTE_ARROW_WIDTH).toInt()
        val mMinuteArrowLength = (clockRect.widthF() / 2f * MINUTE_ARROW_LENGTH).toInt()
        val mSecondArrowWidth = (clockRect.widthF() * SECOND_ARROW_WIDTH).toInt()
        val mSecondArrowLength = (clockRect.widthF() / 2f * SECOND_ARROW_LENGTH).toInt()
        create(
            _secondTick,
            clockRect.centerX(),
            clockRect.centerY(),
            mSecondArrowWidth,
            mSecondArrowLength
        )
        create(
            _minuteTick,
            clockRect.centerX(),
            clockRect.centerY(),
            mMinuteArrowWidth,
            mMinuteArrowLength
        )
        create(
            _hourTick,
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

data class ClockParams(
    val showHourLabels: Boolean = false,
    val showShadow: Boolean = false,
    val showSecondsTick: Boolean = true
)

data class ClockColors(
    @ColorInt val clockColor: Int = Color.LTGRAY,
    @ColorInt val hourTickColor: Int = Color.RED,
    @ColorInt val minuteTickColor: Int = Color.GREEN,
    @ColorInt val secondsTickColor: Int = Color.BLUE,
    @ColorInt val labelsColor: Int = Color.BLACK,
    @ColorInt val shadowColor: Int = Color.BLACK
)

internal data class ClockTime(
    val hour: Int = 0,
    val minute: Int = 0,
    val second: Int = 0
)

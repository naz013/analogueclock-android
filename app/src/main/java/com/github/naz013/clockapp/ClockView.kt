package com.github.naz013.clockapp

import android.content.Context
import android.content.res.TypedArray
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
    private var _innerCircleRadius: Float = 5f
    private var _pinRadius: Float = 1f

    private val _backgroundPaint: Paint = Paint()
    private val _shadowPaint: Paint = Paint()
    private val _labelPaint: Paint = Paint()
    private val _tickPaint: Paint = Paint()
    private val _innerCirclePaint: Paint = Paint()
    private val _pinPaint: Paint = Paint()

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

        if (attrs != null && context != null) {
            val a: TypedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.ClockView, defStyleAttr, 0)
            try {
                _params = _params.copy(
                    showHourLabels = a.getBoolean(R.styleable.ClockView_clock_showHourLabel, _params.showHourLabels),
                    showSecondsTick = a.getBoolean(R.styleable.ClockView_clock_showSecondsTick, _params.showSecondsTick),
                    showShadow = a.getBoolean(R.styleable.ClockView_clock_showShadow, _params.showShadow),
                    pinCircleSize = a.getFloat(R.styleable.ClockView_clock_innerCircleSize, _params.pinCircleSize),
                    secondTickWidth = a.getFloat(R.styleable.ClockView_clock_secondsTickWidth, _params.secondTickWidth),
                    minuteTickWidth = a.getFloat(R.styleable.ClockView_clock_minuteTickWidth, _params.minuteTickWidth),
                    hourTickWidth = a.getFloat(R.styleable.ClockView_clock_hourTickWidth, _params.hourTickWidth)
                )

                _colors = _colors.copy(
                    clockColor = a.getColor(R.styleable.ClockView_clock_backgroundColor, _colors.clockColor),
                    pinColor = a.getColor(R.styleable.ClockView_clock_pinColor, _colors.pinColor),
                    innerCircleColor = a.getColor(R.styleable.ClockView_clock_innerCircleColor, _colors.innerCircleColor),
                    secondsTickColor = a.getColor(R.styleable.ClockView_clock_secondsTickColor, _colors.secondsTickColor),
                    minuteTickColor = a.getColor(R.styleable.ClockView_clock_minuteTickColor, _colors.minuteTickColor),
                    hourTickColor = a.getColor(R.styleable.ClockView_clock_hourTickColor, _colors.hourTickColor),
                    labelsColor = a.getColor(R.styleable.ClockView_clock_labelsColor, _colors.labelsColor)
                )
            } catch (e: Exception) {
                log("init: " + e.localizedMessage)
            } finally {
                a.recycle()
            }
        }

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

        _innerCirclePaint.isAntiAlias = true
        _innerCirclePaint.style = Paint.Style.FILL

        _pinPaint.isAntiAlias = true
        _pinPaint.style = Paint.Style.FILL
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
        drawInnerCircle(canvas)
        drawPin(canvas)
        log("onDraw: duration=${System.currentTimeMillis() - millis}")
    }

    private fun drawPin(canvas: Canvas) {
        _clockRect?.also { rect ->
            _pinPaint.color = _colors.pinColor
            canvas.drawCircle(
                rect.centerXf(),
                rect.centerYf(),
                _pinRadius,
                _pinPaint
            )
        }
    }

    private fun drawInnerCircle(canvas: Canvas) {
        _clockRect?.also { rect ->
            _innerCirclePaint.color = _colors.innerCircleColor
            canvas.drawCircle(
                rect.centerXf(),
                rect.centerYf(),
                _innerCircleRadius,
                _innerCirclePaint
            )
        }
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

        _innerCircleRadius = clockRect.widthF() / 2f * _params.pinCircleSize
        _pinRadius = clockRect.widthF() / 2f * 0.03f



        val mLabelLength = (clockRect.widthF() * 0.85f / 2f).toInt()
        _labelPoints[0] =
            circlePoint(clockRect.centerX(), clockRect.centerY(), mLabelLength, 270f)
        _labelPoints[1] = circlePoint(clockRect.centerX(), clockRect.centerY(), mLabelLength, 0f)
        _labelPoints[2] = circlePoint(clockRect.centerX(), clockRect.centerY(), mLabelLength, 90f)
        _labelPoints[3] =
            circlePoint(clockRect.centerX(), clockRect.centerY(), mLabelLength, 180f)

        val mHourArrowWidth = clockRect.widthF() * _params.hourTickWidth
        val mHourArrowLength = clockRect.widthF() / 2f * HOUR_ARROW_LENGTH

        val mMinuteArrowWidth = clockRect.widthF() * _params.minuteTickWidth
        val mMinuteArrowLength = clockRect.widthF() / 2f * MINUTE_ARROW_LENGTH

        val mSecondArrowWidth = clockRect.widthF() * _params.secondTickWidth
        val mSecondArrowLength = clockRect.widthF() / 2f * SECOND_ARROW_LENGTH
        create(
            _secondTick,
            clockRect.centerXf(),
            clockRect.centerYf(),
            mSecondArrowWidth,
            mSecondArrowLength
        )
        create(
            _minuteTick,
            clockRect.centerXf(),
            clockRect.centerYf(),
            mMinuteArrowWidth,
            mMinuteArrowLength
        )
        create(
            _hourTick,
            clockRect.centerXf(),
            clockRect.centerYf(),
            mHourArrowWidth,
            mHourArrowLength
        )
    }

    private fun create(path: Path, cx: Float, cy: Float, width: Float, length: Float) {
        path.reset()
        path.moveTo(cx + length, cy - width / 2f) // top right
        path.lineTo(cx + length, cy + width / 2f) // bottom right
        path.lineTo(cx, cy + width / 2f) // bottom left
        path.lineTo(cx, cy - width / 2f) // top left
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
        private const val HOUR_ARROW_LENGTH = 0.60f
        private const val MINUTE_ARROW_LENGTH = 0.75f
        private const val SECOND_ARROW_LENGTH = 0.80f
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
    val showSecondsTick: Boolean = true,
    val pinCircleSize: Float = 0.07f, // 0.05 - 0.15
    val hourTickWidth: Float = 0.04f, // 0.035 - 0.045
    val minuteTickWidth: Float = 0.03f, // 0.01 - 0.03
    val secondTickWidth: Float = 0.003f // 0.001 - 0.008
)

data class ClockColors(
    @ColorInt val clockColor: Int = Color.LTGRAY,
    @ColorInt val hourTickColor: Int = Color.RED,
    @ColorInt val minuteTickColor: Int = Color.GREEN,
    @ColorInt val secondsTickColor: Int = Color.BLUE,
    @ColorInt val labelsColor: Int = Color.BLACK,
    @ColorInt val shadowColor: Int = Color.BLACK,
    @ColorInt val pinColor: Int = Color.DKGRAY,
    @ColorInt val innerCircleColor: Int = Color.CYAN
)

internal data class ClockTime(
    val hour: Int = 0,
    val minute: Int = 0,
    val second: Int = 0
)

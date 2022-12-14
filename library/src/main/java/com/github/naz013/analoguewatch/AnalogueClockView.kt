package com.github.naz013.analoguewatch

import android.content.Context
import android.content.res.TypedArray
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.PointF
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

class AnalogueClockView constructor(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var _time: ClockTime = ClockTime()
    private var _colors: ClockColors = ClockColors()
    private var _params: ClockParams = ClockParams()

    private var _clockRect: Rect? = null
    private var _hourTick: Tick? = null
    private var _minuteTick: Tick? = null
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

//    fun setCatModeEnabled(enabled: Boolean) {
//        _params = _params.copy(catModeEnabled = enabled)
//        createMinuteTick()
//        createHourTick()
//        this.invalidate()
//    }

//    fun isCatModeEnabled(): Boolean = _params.catModeEnabled

    fun getTime(): TimeData {
        return TimeData(_time.hour, _time.minute, _time.second)
    }

    fun setTime(timeData: TimeData) {
        _time = ClockTime(timeData.hour, timeData.minute, timeData.second)
        this.invalidate()
    }

    private fun initView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) {
        initTime(System.currentTimeMillis())

        if (attrs != null && context != null) {
            val a: TypedArray =
                context.theme.obtainStyledAttributes(
                    attrs,
                    R.styleable.AnalogueClockView,
                    defStyleAttr,
                    0
                )
            try {
                _params = _params.copy(
                    showHourLabels = a.getBoolean(
                        R.styleable.AnalogueClockView_clock_showHourLabel,
                        _params.showHourLabels
                    ),
                    showSecondsTick = a.getBoolean(
                        R.styleable.AnalogueClockView_clock_showSecondsTick,
                        _params.showSecondsTick
                    ),
                    showShadow = a.getBoolean(
                        R.styleable.AnalogueClockView_clock_showShadow,
                        _params.showShadow
                    ),
//                    catModeEnabled = a.getBoolean(
//                        R.styleable.AnalogueClockView_clock_catModeEnabled,
//                        _params.catModeEnabled
//                    ),
                    showInnerCircleBorder = a.getBoolean(
                        R.styleable.AnalogueClockView_clock_showInnerCircleBorder,
                        _params.showInnerCircleBorder
                    ),
                    showLabelForTick = a.getBoolean(
                        R.styleable.AnalogueClockView_clock_showLabelForTick,
                        _params.showLabelForTick
                    ),
                    pinCircleSize = a.getFloat(
                        R.styleable.AnalogueClockView_clock_innerCircleSize,
                        _params.pinCircleSize
                    ),
                    secondTickWidth = a.getFloat(
                        R.styleable.AnalogueClockView_clock_secondsTickWidth,
                        _params.secondTickWidth
                    ),
                    minuteTickWidth = a.getFloat(
                        R.styleable.AnalogueClockView_clock_minuteTickWidth,
                        _params.minuteTickWidth
                    ),
                    hourTickWidth = a.getFloat(
                        R.styleable.AnalogueClockView_clock_hourTickWidth,
                        _params.hourTickWidth
                    ),
                    labelTextSize = a.getDimension(
                        R.styleable.AnalogueClockView_clock_labelsTextSize,
                        _params.labelTextSize
                    )
                )

                _colors = _colors.copy(
                    clockColor = a.getColor(
                        R.styleable.AnalogueClockView_clock_backgroundColor,
                        _colors.clockColor
                    ),
                    pinColor = a.getColor(
                        R.styleable.AnalogueClockView_clock_pinColor,
                        _colors.pinColor
                    ),
                    innerCircleColor = a.getColor(
                        R.styleable.AnalogueClockView_clock_innerCircleColor,
                        _colors.innerCircleColor
                    ),
                    secondsTickColor = a.getColor(
                        R.styleable.AnalogueClockView_clock_secondsTickColor,
                        _colors.secondsTickColor
                    ),
                    minuteTickColor = a.getColor(
                        R.styleable.AnalogueClockView_clock_minuteTickColor,
                        _colors.minuteTickColor
                    ),
                    hourTickColor = a.getColor(
                        R.styleable.AnalogueClockView_clock_hourTickColor,
                        _colors.hourTickColor
                    ),
                    labelsColor = a.getColor(
                        R.styleable.AnalogueClockView_clock_labelsColor,
                        _colors.labelsColor
                    ),
                    shadowColor = a.getColor(
                        R.styleable.AnalogueClockView_clock_shadowColor,
                        _colors.shadowColor
                    )
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
        _labelPaint.textSize = _params.labelTextSize

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
        if (canvas == null) return
        if (_params.showShadow) {
            drawClockShadow(canvas)
        }
        drawClockBackground(canvas)
        if (_params.showHourLabels) {
            drawHourLabels(canvas)
        }
        drawMinuteArrow(canvas)
        drawHourArrow(canvas)
        if (_params.showSecondsTick) {
            drawSecondArrow(canvas)
        }
        drawInnerCircle(canvas)
        drawPin(canvas)
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
            if (_params.showInnerCircleBorder) {
                _innerCirclePaint.color = _colors.hourTickColor
                canvas.drawCircle(
                    rect.centerXf(),
                    rect.centerYf(),
                    _innerCircleRadius,
                    _innerCirclePaint
                )
            }
            _innerCirclePaint.color = _colors.innerCircleColor
            canvas.drawCircle(
                rect.centerXf(),
                rect.centerYf(),
                _innerCircleRadius * 0.97f,
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
        _clockRect?.takeIf { _minuteTick != null }
            ?.also { rect ->
                canvas.save()
                canvas.rotate(minuteAngle(), rect.centerXf(), rect.centerYf())
                _hourTick?.updateColors(_colors.minuteTickColor, _colors.clockColor)
                _minuteTick?.draw(canvas)
                canvas.restore()
            }
    }

    private fun drawHourArrow(canvas: Canvas) {
        _clockRect?.takeIf { _hourTick != null }
            ?.also { rect ->
                canvas.save()
                canvas.rotate(hourAngle(), rect.centerXf(), rect.centerYf())
                _hourTick?.updateColors(_colors.hourTickColor, _colors.clockColor)
                _hourTick?.draw(canvas)
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
                rect.width() / 2f * 0.95f,
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
            angle = 270f + minutes.toFloat() * 0.5f
        }
        return angle
    }

    private fun minuteAngle(): Float {
        var angle = 0.0f
        val minute = _time.minute
        if (validateValue(minute, 0, 59)) {
            angle = 270f + minute.toFloat() * 6f
        }
        return angle
    }

    private fun secondAngle(): Float {
        var angle = 0.0f
        val second = _time.second
        if (validateValue(second, 0, 59)) {
            angle = 270f + second.toFloat() * 6f
        }
        return angle
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val measuredWidth = measuredWidth
        setMeasuredDimension(measuredWidth, measuredWidth)
        processCalculations(measuredWidth)
    }

    private fun processCalculations(width: Int) {
        if (width <= 0) return
        val margin = (width.toFloat() * 0.05f / 2f).toInt()
        val clockRect = Rect(margin, margin, width - margin, width - margin)
        _clockRect = clockRect

        _innerCircleRadius = clockRect.widthF() / 2f * _params.pinCircleSize
        _pinRadius = clockRect.widthF() / 2f * 0.03f

        val mLabelLength = (clockRect.widthF() * 0.85f / 2f).toInt()
        _labelPoints[0] =
            circlePoint(clockRect.centerX(), clockRect.centerY(), mLabelLength, 270f)
        _labelPoints[1] = circlePoint(clockRect.centerX(), clockRect.centerY(), mLabelLength, 0f)
        _labelPoints[2] = circlePoint(clockRect.centerX(), clockRect.centerY(), mLabelLength, 90f)
        _labelPoints[3] =
            circlePoint(clockRect.centerX(), clockRect.centerY(), mLabelLength, 180f)

        val mSecondArrowWidth = clockRect.widthF() * _params.secondTickWidth
        val mSecondArrowLength = clockRect.widthF() / 2f * SECOND_ARROW_LENGTH

        create(
            _secondTick,
            clockRect.centerXf(),
            clockRect.centerYf(),
            mSecondArrowWidth,
            mSecondArrowLength
        )
        createMinuteTick()
        createHourTick()
    }

    private fun createHourTick() {
        val clockRect = _clockRect ?: return

        val mHourArrowWidth = clockRect.widthF() * _params.hourTickWidth
        val mHourArrowLength = clockRect.widthF() / 2f * HOUR_ARROW_LENGTH

        _hourTick = if (_params.catModeEnabled) {
            PawTick(
                clockRect.centerXf(),
                clockRect.centerYf(),
                mHourArrowWidth,
                mHourArrowLength,
                _colors.hourTickColor,
                _colors.clockColor
            )
        } else {
            RectangleTick(
                clockRect.centerXf(),
                clockRect.centerYf(),
                mHourArrowWidth,
                mHourArrowLength,
                _colors.hourTickColor
            )
        }
    }

    private fun createMinuteTick() {
        val clockRect = _clockRect ?: return

        val mMinuteArrowWidth = clockRect.widthF() * _params.minuteTickWidth
        val mMinuteArrowLength = clockRect.widthF() / 2f * MINUTE_ARROW_LENGTH

        _minuteTick = if (_params.catModeEnabled) {
            PawTick(
                clockRect.centerXf(),
                clockRect.centerYf(),
                mMinuteArrowWidth,
                mMinuteArrowLength,
                _colors.minuteTickColor,
                _colors.clockColor
            )
        } else {
            RectangleTick(
                clockRect.centerXf(),
                clockRect.centerYf(),
                mMinuteArrowWidth,
                mMinuteArrowLength,
                _colors.minuteTickColor
            )
        }
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

    internal data class ClockParams(
        val showHourLabels: Boolean = false,
        val showLabelForTick: Boolean = false,
        val showShadow: Boolean = false,
        val showSecondsTick: Boolean = true,
        val showInnerCircleBorder: Boolean = true,
        val catModeEnabled: Boolean = false,
        val pinCircleSize: Float = 0.07f, // 0.05 - 0.15
        val hourTickWidth: Float = 0.04f, // 0.035 - 0.045
        val minuteTickWidth: Float = 0.03f, // 0.01 - 0.03
        val secondTickWidth: Float = 0.003f, // 0.001 - 0.008
        val labelTextSize: Float = 25f
    )

    internal data class ClockColors(
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

    internal class PawTick(
        private val cx: Float,
        private val cy: Float,
        private val width: Float,
        private val length: Float,
        private val primaryColor: Int,
        private val secondaryColor: Int
    ) : Tick {

        private val catPawPath = createCatPawPath()
        private val primaryPath: Path = createPrimaryPath()

        private val secondaryPaint: Paint = Paint().apply {
            color = secondaryColor
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        private val primaryPaint: Paint = Paint().apply {
            color = primaryColor
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        override fun draw(canvas: Canvas) {
            canvas.drawPath(primaryPath, primaryPaint)
//            canvas.drawPath(catPawPath, secondaryPaint)
        }

        private fun createCatPawPath(): Path {
            // paw pillows circles

            return Path().apply {
                reset()

                close()
            }
        }

        private fun createPrimaryPath(): Path {
            // solid part of paw
            val a = length * 0.8f
            val b = length * 0.95f
            val c = length * 0.97f
            val d = length * 0.99f

            val z = width / 2f
            val f = z * 0.9f
            val g = width / 5f

            val path = Path()

            val p0 = PointF(cx, cy)
            path.moveTo(p0)

            val p1 = PointF(cx, cy - z)
            path.lineTo(p1)

            val p2 = PointF(cx + a, cy - f)
            path.lineTo(p2)

            val p3 = PointF(cx + b, cy - z)
            path.lineTo(p3)

            val p4 = PointF(cx + c, cy - z + g)
            val p4Curve = PointF(
                p4.x,
                (p3.y + p4.y) / 2f
            )

            path.cubicTo(p3, p4Curve, p4)

            val p5 = PointF(cx + d, cy - z + (2f * g))
            val p5Curve = PointF(
                p5.x,
                (p4.y + p5.y) / 2f
            )

            path.cubicTo(p4, p5Curve, p5)

            val p6 = PointF(cx + c, cy + z - g)
            val p6Curve = PointF(
                p5.x,
                (p5.y + p6.y) / 2f
            )

            path.cubicTo(p5, p6Curve, p6)

            val p7 = PointF(cx + b, cy + z)
            val p7Curve = PointF(
                p6.x,
                (p6.y + p7.y) / 2f
            )

            path.cubicTo(p6, p7Curve, p7)

            val p8 = PointF(cx + a, cy + f)
            path.lineTo(p8)

            val p9 = PointF(cx, cy + z)
            path.lineTo(p9)

            path.lineTo(p0)

            return path
        }

        override fun updateColors(primaryColor: Int, secondaryColor: Int) {
            primaryPaint.color = primaryColor
            secondaryPaint.color = secondaryColor
        }
    }

    internal class RectangleTick(
        private val cx: Float,
        private val cy: Float,
        private val width: Float,
        private val length: Float,
        private val primaryColor: Int
    ) : Tick {

        private val primaryPath: Path = Path().apply {
            reset()
            moveTo(cx + length, cy - width / 2f) // top right
            lineTo(cx + length, cy + width / 2f) // bottom right
            lineTo(cx, cy + width / 2f) // bottom left
            lineTo(cx, cy - width / 2f) // top left
            close()
        }
        private val primaryPaint: Paint = Paint().apply {
            color = primaryColor
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        override fun draw(canvas: Canvas) {
            canvas.drawPath(primaryPath, primaryPaint)
        }

        override fun updateColors(primaryColor: Int, secondaryColor: Int) {
            primaryPaint.color = primaryColor
        }
    }

    internal interface Tick {
        fun draw(canvas: Canvas)
        fun updateColors(primaryColor: Int, secondaryColor: Int)
    }

    companion object {
        private const val TAG = "ClockView"
        private const val SHADOW_RADIUS = 15
        private const val HOUR_ARROW_LENGTH = 0.60f
        private const val MINUTE_ARROW_LENGTH = 0.75f
        private const val SECOND_ARROW_LENGTH = 0.80f
    }
}

private fun Path.cubicTo(p1: PointF, p2: PointF, p3: PointF) {
    this.cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
}

private fun Path.lineTo(p: PointF) {
    this.lineTo(p.x, p.y)
}

private fun Path.moveTo(p: PointF) {
    this.moveTo(p.x, p.y)
}

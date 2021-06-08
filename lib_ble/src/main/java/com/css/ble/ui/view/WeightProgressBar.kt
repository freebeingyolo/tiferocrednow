package com.css.ble.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi

class WeightProgressBar : View {
    private var mBackPaint: Paint
    private var mFrontPaint: Paint
    private val mStrokeWidth = 10f
    private val mRadius = 200f
    private var mRect: RectF? = null
    private var mProgress = 0

    //目标值，想改多少就改多少
    private val mTargetProgress = 0
    private val mMax = 100
    private var mWidth = 0
    private var mHeight = 0

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    init {
        mBackPaint = Paint()
        mBackPaint.color = Color.parseColor("#E4E4E4")
        mBackPaint.isAntiAlias = true
        mBackPaint.style = Paint.Style.STROKE
        mBackPaint.strokeWidth = mStrokeWidth

        mFrontPaint = Paint()
        mFrontPaint.color = Color.parseColor("#F1672B")
        mFrontPaint.isAntiAlias = true
        mFrontPaint.style = Paint.Style.STROKE
        mFrontPaint.strokeWidth = mStrokeWidth

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = getRealSize(widthMeasureSpec);
        mHeight = getRealSize(heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        initRect()
        val angle = mProgress / mMax.toFloat() * 360
        canvas!!.drawCircle((mWidth / 2).toFloat(), (mHeight / 2).toFloat(), mRadius, mBackPaint)
        canvas.drawArc(mRect!!, -90f, angle, false, mFrontPaint)

        if (mProgress < mTargetProgress) {
            mProgress += 1
            invalidate()
        }
    }

    fun getRealSize(measureSpec: Int): Int {
        var result = 1
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)
        result = if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.UNSPECIFIED) {
            //自己计算
            (mRadius * 2 + mStrokeWidth).toInt()
        } else {
            size
        }
        return result
    }

    fun setProgress(progress: Int) {
        mProgress = progress
        invalidate()
    }

    private fun initRect() {
        if (mRect == null) {
            mRect = RectF()
            val viewSize = (mRadius * 2).toInt()
            val left = (mWidth - viewSize) / 2
            val top = (mHeight - viewSize) / 2
            val right = left + viewSize
            val bottom = top + viewSize
            mRect!!.set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
        }
    }
}
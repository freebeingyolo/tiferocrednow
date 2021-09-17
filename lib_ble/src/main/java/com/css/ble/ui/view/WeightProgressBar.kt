package com.css.ble.ui.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import com.css.base.R

class WeightProgressBar : View {
    private var mBackPaint: Paint
    private var mFrontPaint: Paint
    private val mStrokeWidth = 10f
    private var mRadius = 200f
    private var mRect: RectF? = null
    private var mProgress = 50

    //目标值，想改多少就改多少
    private val mTargetProgress = 0
    private var mMax = 100
    private var mWidth = 0
    private var mHeight = 0

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.WeightProgressBar)
        parseAttrs(ta)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val ta =
            context.obtainStyledAttributes(attrs, R.styleable.WeightProgressBar, defStyleAttr, 0)
        parseAttrs(ta)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        val ta = context.obtainStyledAttributes(
            attrs,
            R.styleable.WeightProgressBar,
            defStyleAttr,
            defStyleRes
        )
        parseAttrs(ta)
    }

    private fun parseAttrs(ta: TypedArray) {
        try {
            for (i in 0 until ta.indexCount) {
                when (val id = ta.getIndex(i)) {
                    R.styleable.WeightProgressBar_max -> {
                        val p = ta.getInteger(id, 0)
                        mMax = p
                    }
                    R.styleable.WeightProgressBar_progress -> {
                        val p = ta.getInteger(id, 0)
                        mProgress = p
                    }
                    R.styleable.WeightProgressBar_backColor -> {
                        val color = ta.getColor(id, 0)
                        mBackPaint.color = color
                    }
                    R.styleable.WeightProgressBar_backColor -> {
                        val color = ta.getColor(id, 0)
                        mFrontPaint.color = color
                    }
                }
            }
        } finally {
            ta.recycle()
        }
    }

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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        initRect()
        val angle = mProgress / mMax.toFloat() * 360
        canvas.drawCircle((mWidth / 2).toFloat(), (mHeight / 2).toFloat(), mRadius, mBackPaint)
        canvas.drawArc(mRect!!, -90f, angle, false, mFrontPaint)

        /*if (mProgress < mTargetProgress) {
            mProgress += 1
            invalidate()
        }*/
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
            mRadius = (Math.min(mWidth, mHeight) - mStrokeWidth) / 2f
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
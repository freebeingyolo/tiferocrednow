package com.css.base.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.css.base.R

class ElectricityView : View {
    private var mBatteryPaint: Paint? = null//电池画笔
    private var mPowerPaint: Paint? = null//电量画笔
    private var mBatteryStroke = 2f //电池框画笔宽度
    private var mCapWidth = 5f
    private val mBatteryRect: RectF by lazy { RectF() }//电池矩形
    private val mCapRect: RectF by lazy { RectF() } //电池盖矩形
    private val mPowerRect: RectF by lazy { RectF() }//电量矩形
    private var mSpecWidthSize = 0
    private var mSpecHeightSize: Int = 0
    private var mBatteryColor = Color.parseColor("#000000")//电池框颜色
    private var mPowerColor = Color.parseColor("#000000")//电量颜色
    private var mLowPowerColor = Color.parseColor("#ff0000") //低电颜色
    private var mPower = 100 //当前电量（满电100）
    private var mLowerPower = 10 //低电量判定

    private val TAG = "ElectricityView"

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.ElectricityView)
        parseAttrs(ta)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.ElectricityView, defStyleAttr, 0)
        parseAttrs(ta)
    }

    private fun parseAttrs(ta: TypedArray) {
        try {
            for (i in 0 until ta.indexCount) {
                when (val id = ta.getIndex(i)) {
                    R.styleable.ElectricityView_progress -> {
                        val p = ta.getInteger(id, 0)
                        setProgress(p)
                    }
                    R.styleable.ElectricityView_lowPower -> {
                        val p = ta.getInteger(id, 0)
                        this.mLowerPower = p
                    }
                    R.styleable.ElectricityView_lowPowerColor -> {
                        val v = ta.getColor(id, 0)
                        mLowPowerColor = v
                    }
                    R.styleable.ElectricityView_powerColor -> {
                        val v = ta.getColor(id, 0)
                        mPowerColor = v
                    }
                    R.styleable.ElectricityView_batteryColor -> {
                        val v = ta.getColor(id, 0)
                        mBatteryColor = v
                    }
                }
            }
        } finally {
            ta.recycle()
        }
    }

    init {
        initPaint()
    }

    private fun initPaint() {
        /**
         * 设置电池画笔
         */
        mBatteryPaint = Paint()
        mBatteryPaint!!.color = mBatteryColor
        mBatteryPaint!!.isAntiAlias = true
        mBatteryPaint!!.style = Paint.Style.STROKE
        mBatteryPaint!!.strokeWidth = mBatteryStroke

        /**
         * 设置电量画笔
         */
        mPowerPaint = Paint()
        mPowerPaint!!.isAntiAlias = true
        mPowerPaint!!.style = Paint.Style.FILL_AND_STROKE

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mSpecWidthSize = MeasureSpec.getSize(widthMeasureSpec);//宽
        mSpecHeightSize = MeasureSpec.getSize(heightMeasureSpec);//高
        mCapWidth = mSpecWidthSize * 0.1f;
        setMeasuredDimension(mSpecWidthSize, mSpecHeightSize);
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mPower <= mLowerPower) {
            mPowerPaint!!.color = mLowPowerColor
        } else {
            mPowerPaint!!.color = mPowerColor
        }

        /**
         * 设置电池矩形
         * 2 间隔距离
         */
        mBatteryRect.set(
            mBatteryStroke, 2f, mSpecWidthSize - mBatteryStroke - mCapWidth,
            (mSpecHeightSize - 4).toFloat()
        )


        /**
         * 设置电池盖矩形
         */
        mCapRect.set(
            mSpecWidthSize - mCapWidth - mBatteryStroke,//贴合
            (mSpecHeightSize - 2) * 0.25f,
            (mSpecWidthSize - mBatteryStroke).toFloat(),
            (mSpecHeightSize - 4) * 0.75f
        )
        /**
         * 设置电量矩形
         */
        val right: Float = mBatteryRect.left + 2 - mPowerPaint!!.strokeWidth + (mBatteryRect.width() - 4) * mPower / 100f
        mPowerRect.set(
            mBatteryRect.left + 2,
            2 + mBatteryStroke,
            right,
            mSpecHeightSize - (2 + mBatteryStroke) - 2
        )
        canvas.drawRoundRect(mBatteryRect, 5f, 5f, mBatteryPaint!!)
        canvas.drawRoundRect(mCapRect, 5f, 5f, mBatteryPaint!!) // 画电池盖
        canvas.drawRoundRect(mPowerRect, 0f, 0f, mPowerPaint!!) // 画电量
    }


    fun setProgress(power: Int) {
        this.mPower = Math.max(Math.min(100,power),0)
        invalidate()
    }
}
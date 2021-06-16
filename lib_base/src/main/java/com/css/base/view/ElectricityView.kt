package com.css.base.view

import android.R.attr.textSize
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import com.css.base.R

class ElectricityView : View {
    private var mBatteryPaint: Paint? = null//电池画笔
    private var mPowerPaint: Paint? = null//电量画笔
    private var mBatteryStroke = 2f //电池框宽度
    private var mBatteryRect: RectF? = null//电池矩形
    private var mCapRect: RectF? = null //电池盖矩形
    private var mPowerRect: RectF? = null//电量矩形
    private var mSpecWidthSize = 0
    private var mSpecHeightSize: Int = 0
    private var mBatteryColor =  Color.parseColor("#000000")//电池框颜色
    private var mPowerColor =  Color.parseColor("#000000")//电量颜色
    private var mLowPowerColor =  Color.parseColor("#000000") //低电颜色
    private var mPower = 0 //当前电量（满电100）
    private var mCapWidth = 5f


    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

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
        mPowerPaint!!.style = Paint.Style.FILL

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mSpecWidthSize = MeasureSpec.getSize(widthMeasureSpec);//宽
        mSpecHeightSize = MeasureSpec.getSize(heightMeasureSpec);//高
        setMeasuredDimension(mSpecWidthSize, mSpecHeightSize);
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (mPower <= 20) {
            mPowerPaint!!.color = mLowPowerColor
        } else {
            mPowerPaint!!.color = mPowerColor
        }

        /**
         * 设置电池矩形
         * 2 间隔距离
         */
        mBatteryRect = RectF(
            2f, 2f, mSpecWidthSize  - 10 - mCapWidth,
            (mSpecHeightSize - 4).toFloat()
        )

        /**
         * 设置电池盖矩形
         */
        mCapRect = RectF(
            mSpecWidthSize  - 10 - mCapWidth,
            (mSpecHeightSize - 2) * 0.25f,
            (mSpecWidthSize - 10).toFloat(),
            (mSpecHeightSize - 4) * 0.75f
        )
        /**
         * 设置电量矩形
         */
        var right: Float = if (mPower < 20) {
            (mSpecWidthSize  - 10 - mCapWidth - 2) / 100.0f * 20
        } else {
            (mSpecWidthSize  - 10 - mCapWidth - 2) / 100.0f * mPower
        }
        mPowerRect = RectF(
            mBatteryStroke + 2,
            2 + mBatteryStroke,
            right,
            mSpecHeightSize - (2 + mBatteryStroke) - 2
        )



        canvas!!.drawRoundRect(mBatteryRect!!, 5f, 5f, mBatteryPaint!!)

        canvas.drawRoundRect(mCapRect!!, 5f, 5f, mBatteryPaint!!) // 画电池盖

        canvas.drawRoundRect(mPowerRect!!, 5f, 5f, mPowerPaint!!) // 画电量

    }

    fun setProgress(power: Int) {
        if (power < 0) {
            mPower = 0;
        } else if (power > 100) {
            mPower = 100;
        }
        this.mPower = power;
        invalidate();
    }
}
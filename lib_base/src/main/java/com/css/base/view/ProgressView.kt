package com.css.base.view

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.css.base.R

class ProgressView : View {
    private var preWidth = 0
    private var preHeight = 0
    private var sideLength = 0
    private var quenLinePaint: Paint? = null
    //线条数量
    private val mCount = 66
    private var mProgress = 0f
    var animator : ObjectAnimator
    private var paint1: Paint
    private var paint2: Paint

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        animator = ObjectAnimator.ofFloat(this, "progress", 0f, mProgress)!!
        animator.duration = 1500
        quenLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        quenLinePaint!!.color = Color.WHITE
        animator.interpolator = FastOutSlowInInterpolator()
        paint1 = Paint(Paint.ANTI_ALIAS_FLAG)
        paint1.color = Color.WHITE

        paint2 = Paint(Paint.ANTI_ALIAS_FLAG)
        paint2.color = resources.getColor(R.color.color_8b8c8d)

    }

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

    fun getProgress(): Float {
        return mProgress
    }

    fun setProgress(progress: Float) {
        mProgress = progress
        animator = ObjectAnimator.ofFloat(this, "progress", 0f, mProgress)!!
        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.end()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        preWidth = MeasureSpec.getSize(widthMeasureSpec)
        preHeight = MeasureSpec.getSize(heightMeasureSpec)
        val max: Int = Math.max(preWidth, preHeight)
        if (max < 240) {
            sideLength = 240 //保证刻度清晰可见,设置边长下限
        } else {
            sideLength = max
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //画圆
        val oval1 = RectF(0f, 0f, sideLength.toFloat(), sideLength.toFloat()) //绘制区域
        canvas.drawArc(oval1, 135f, 270f, true, quenLinePaint!!) //绘制圆弧从135度开始绘制270度
        canvas.drawCircle(
            (sideLength / 2).toFloat(),
            (sideLength / 2).toFloat(),
            (sideLength / 2 - 20).toFloat(),
            paint1
        )
        //绘制刻度线,通过两次不同大小圆的遮罩
        val oval2 = RectF(0f, 0f, (sideLength).toFloat(), (sideLength).toFloat()) //
        val i1 = (270.0f - 140) / mCount
        var startAngle = 135f
        for (i in 0..mCount) {
            startAngle += i1 + 2
            if (i < (mProgress / 1.5).toInt()) {
                paint2.color = resources.getColor(R.color.color_e1251b)
            } else {
                paint2.color = resources.getColor(R.color.color_8b8c8d)
            }
            canvas.drawArc(oval2, startAngle, 1f, true, paint2)
        }
        canvas.drawCircle(
            (sideLength / 2).toFloat(),
            (sideLength / 2).toFloat(), (sideLength / 2 - 40).toFloat(), paint1
        )
        canvas.save()
        canvas.restore()
    }

}
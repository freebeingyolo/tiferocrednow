package com.css.ble.ui.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.Nullable
import com.css.ble.R


/**
 * @author yuedong
 * @date 2021-06-01
 */
class CircleAnnulusProgressBar : View {
    /**
     * 默认的外圆的颜色
     */
    private val DEFAULT_OUTER_CIRCLE_COLOR: Int = Color.parseColor("#FFFFFF")

    /**
     * 默认的内饼形的颜色
     */
    private val DEFAULT_PIE_CIRCLE: Int = Color.parseColor("#FFFFFF")
    //-------------------- 自定义属性 -------------------
    /**
     * 默认的外圆轮廓宽度，默认是1dp
     */
    private var mOuterCircleBorderWidth = 0f

    /**
     * 外圆的颜色，默认白色
     */
    private var mOuterCircleColor = 0

    /**
     * 内饼图填充的颜色，默认是白色
     */
    private var mPieColor = 0
    //-------------------- 绘制相关对象 -------------------
    /**
     * 外圆的画笔
     */
    private lateinit var mOuterCirclePaint: Paint

    /**
     * 内部的饼形的画笔
     */
    private lateinit var mPiePaint: Paint

    /**
     * View绘制区域，去除了padding
     */
    private lateinit var mRect: RectF
    //-------------------- View宽高等参数 -------------------
    /**
     * View的宽，包括padding
     */
    private var mWidth = 0

    /**
     * View的高，包括padding
     */
    private var mHeight = 0

    /**
     * 设置的上下左右padding值
     */
    private var mPaddingTop = 0
    private var mPaddingBottom = 0
    private var mPaddingLeft = 0
    private var mPaddingRight = 0

    /**
     * 外圆的半径，已经处理了padding
     */
    private var mRadius = 0f

    /**
     * 当前进度，默认为最大值
     */
    private var mProgress = 0

    /**
     * 设置的最大进度，默认为100
     */
    private var mMax = 0
    //-------------------- 对外使用的对象 -------------------
    /**
     * 监听器集合
     */
    private var mListeners: ArrayList<OnProgressUpdateListener>? = null

    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(context: Context?, @Nullable attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, @Nullable attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        //由于dp转换操作必须在初始化后context才不为空，所以在这里初始化默认的外圆轮廓宽度
        DEFAULT_OUTER_CIRCLE_BORDER_WIDTH = dip2px(getContext(), 1f)

        //取出Xml设置的自定义属性，当前进度，最大进度
        if (attrs != null) {
            val array: TypedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CircleAnnulusProgressBar)
            //Xml设置的进度
            mProgress = array.getInt(R.styleable.CircleAnnulusProgressBar_progress, DEFAULT_PROGRESS)
            //Xml设置的最大值
            mMax = array.getInt(R.styleable.CircleAnnulusProgressBar_max, DEFAULT_MAX)
            //Xml设置的外圆颜色，先读取直接写#FFFFFF等样式的，如果没有，则读取使用引用方式的，就是@color/white这样的
            val resultOuterCircleColor: Int =
                array.getColor(R.styleable.CircleAnnulusProgressBar_outer_circle_color, DEFAULT_OUTER_CIRCLE_COLOR)
            mOuterCircleColor = if (resultOuterCircleColor != DEFAULT_OUTER_CIRCLE_COLOR) {
                resultOuterCircleColor
            } else {
                val outerCircleResId: Int = array.getResourceId(R.styleable.CircleAnnulusProgressBar_outer_circle_color, R.color.white)
                getContext().getResources().getColor(outerCircleResId)
            }
            //Xml设置的内饼图颜色，同上，先读取直接写颜色值的，没有再读取使用引用方式的
            val resultPieCircleColor: Int = array.getColor(R.styleable.CircleAnnulusProgressBar_pie_color, DEFAULT_PIE_CIRCLE)
            mPieColor = if (resultPieCircleColor != DEFAULT_PIE_CIRCLE) {
                resultPieCircleColor
            } else {
                val pieColorResId: Int = array.getResourceId(R.styleable.CircleAnnulusProgressBar_pie_color, R.color.white)
                getContext().getResources().getColor(pieColorResId)
            }
            //读取设置的外圆轮廓宽度，读取dimension
            mOuterCircleBorderWidth = array.getDimensionPixelSize(
                R.styleable.CircleAnnulusProgressBar_outer_circle_border_width,
                DEFAULT_OUTER_CIRCLE_BORDER_WIDTH
            ).toFloat()
            //记得回收资源
            array.recycle()
        } else {
            //没有在Xml中设置属性，使用默认属性
            //当前进度
            mProgress = DEFAULT_PROGRESS
            //最大值
            mMax = DEFAULT_MAX
            //外圆的颜色
            mOuterCircleColor = DEFAULT_OUTER_CIRCLE_COLOR
            //内饼形的颜色
            mPieColor = DEFAULT_PIE_CIRCLE
            //外圆的宽度
            mOuterCircleBorderWidth = DEFAULT_OUTER_CIRCLE_BORDER_WIDTH.toFloat()
        }
        //外层圆的画笔
        mOuterCirclePaint = Paint()
        mOuterCirclePaint.setColor(mOuterCircleColor)
        mOuterCirclePaint.setStyle(Paint.Style.STROKE)
        mOuterCirclePaint.setStrokeWidth(mOuterCircleBorderWidth)
        mOuterCirclePaint.setAntiAlias(true)
        //中间进度饼形画笔
        mPiePaint = Paint()
        mPiePaint.setColor(mPieColor)
        mPiePaint.setStyle(Paint.Style.FILL)
        mPiePaint.setAntiAlias(true)
    }

    protected override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        //取出总宽高
        mWidth = w
        mHeight = h
        //取出设置的padding值
        mPaddingTop = getPaddingTop()
        mPaddingBottom = getPaddingBottom()
        mPaddingLeft = getPaddingLeft()
        mPaddingRight = getPaddingRight()
        //计算外圆直径，取宽高中最小的为圆的直径，这里要处理添加padding的情况。
        val diameter = (Math.min(mWidth, mHeight) - mPaddingLeft - mPaddingRight).toFloat()
        //直径除以2算出半径
        mRadius = (diameter / 2 * 0.98).toFloat()

        //建立一个Rect保存View的范围，后面画饼形也需要用到
        mRect = RectF(mPaddingLeft.toFloat(), mPaddingTop.toFloat(), (mWidth - mPaddingRight).toFloat(),
            (mHeight - mPaddingBottom).toFloat()
        )
    }

    protected override fun onFinishInflate() {
        super.onFinishInflate()
        setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent))
    }

    protected override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //取出宽的模式和大小
        val widthMode: Int = MeasureSpec.getMode(widthMeasureSpec)
        var widthSize: Int = MeasureSpec.getSize(widthMeasureSpec)
        //取出高的模式和大小
        val heightMode: Int = MeasureSpec.getMode(heightMeasureSpec)
        var heightSize: Int = MeasureSpec.getSize(heightMeasureSpec)

        //设置的宽高不相等时，将宽高都进行校正，取最小的为标准
        if (widthSize != heightSize) {
            val finalSize = Math.min(widthSize, heightSize)
            widthSize = finalSize
            heightSize = finalSize
        }

        //默认宽高值
        val defaultWidth = dip2px(getContext(), 55f)
        val defaultHeight = dip2px(getContext(), 55f)
        if (widthMode != MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
            //当宽高都设置wrapContent时设置我们的默认值
            if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(defaultWidth, defaultHeight)
            } else if (widthMode == MeasureSpec.AT_MOST) {
                //宽、高任意一个为wrapContent都设置我们默认值
                setMeasuredDimension(defaultWidth, heightSize)
            } else if (heightMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(widthSize, defaultHeight)
            }
        } else {
            setMeasuredDimension(widthSize, heightSize)
        }
    }

    protected override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //获取当前的进度
        var curProgress = getProgress()
        //越界处理
        if (curProgress < 0) {
            curProgress = 0
        }
        if (curProgress > mMax) {
            curProgress = mMax
        }
        //画外圆，这里使用宽和高都可以，因为我们限定宽和高都是相等的
        //这里圆心坐标一直都在View的宽高的中间，就算有padding都是不会变的，变的只是半径，半径初始化前已经去处理了padding，这里要注意
        canvas.drawCircle(mWidth / 2f, mWidth / 2f, mRadius, mOuterCirclePaint)
        //要进行画布缩放操作，先保存图层，因为缩放、平移等操作是叠加的，所以使用完必须恢复，否则下次的onDraw就会累加缩放
        canvas.save()
        //用缩放画布，进行缩放中心的饼图，设置缩放中心是控件的中心
        canvas.scale(0.90f, 0.90f, mWidth / 2f, mHeight / 2f)
        //计算当前进度对应的角度
        val angle = 360 * (curProgress * 1.0f / getMax())
        //画饼图，-90度就是12点方向开始
        canvas.drawArc(mRect, -90f, angle, true, mPiePaint)
        //还原画布图层
        canvas.restore()
        //回调进度给外面的监听器
        for (listener in mListeners!!) {
            listener.onProgressUpdate(curProgress)
        }
    }

    /**
     * 设置进度
     *
     * @param progress 要设置的进度
     */
    @Synchronized
    fun setProgress(progress: Int) {
        var progress = progress
        if (progress < 0) {
            progress = 0
        }
        mProgress = progress
        //设置进度可能是子线程，所以将重绘调用交给主线程
        postInvalidate()
    }

    /**
     * 获取当前进度
     *
     * @return 当前的进度
     */
    fun getProgress(): Int {
        return mProgress
    }

    /**
     * 设置最大值
     *
     * @param max 要设置的最大值
     */
    @Synchronized
    fun setMax(max: Int) {
        var max = max
        if (max < 0) {
            max = 0
        }
        mMax = max
        //设置进度可能是子线程，所以将重绘调用交给主线程
        postInvalidate()
    }

    /**
     * 进度更新的回调监听
     */
    interface OnProgressUpdateListener {
        //当进度更新时回调
        fun onProgressUpdate(progress: Int)
    }

    /**
     * 设置更新回调
     *
     * @param listener 监听器实例
     */
    fun addOnProgressUpdateListener(listener: OnProgressUpdateListener) {
        if (mListeners == null) {
            mListeners = ArrayList()
        }
        mListeners!!.add(listener)
    }

    /**
     * 获取设置的最大值
     *
     * @return 设置的最大值
     */
    fun getMax(): Int {
        return mMax
    }

    private fun sp2px(context: Context, spVal: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            spVal, context.getResources().getDisplayMetrics()
        ).toInt()
    }

    companion object {
        //-------------------- 默认值 -------------------
        /**
         * 默认的当前进度，默认为0
         */
        private const val DEFAULT_PROGRESS = 0

        /**
         * 默认的最大值，默认为100
         */
        private const val DEFAULT_MAX = 100

        /**
         * 默认的外圆轮廓宽度，默认是1dp
         */
        private var DEFAULT_OUTER_CIRCLE_BORDER_WIDTH = 3

        //------------------ 一些尺寸转换方法 ------------------
        fun dip2px(context: Context, dipValue: Float): Int {
            val scale: Float = context.getResources().getDisplayMetrics().density
            return (dipValue * scale + 0.5f).toInt()
        }

        fun px2dp(context: Context, pxValue: Float): Int {
            val scale: Float = context.getResources().getDisplayMetrics().density
            return (pxValue / scale + 0.5f).toInt()
        }
    }
}
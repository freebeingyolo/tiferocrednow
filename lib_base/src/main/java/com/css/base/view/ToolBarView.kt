package com.css.base.view

import android.content.Context
import android.database.DatabaseUtils
import android.graphics.Bitmap
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.SizeUtils
import com.css.base.R
import com.css.base.databinding.PopupCommonEditBinding
import com.css.base.databinding.ViewToolbarBinding
import com.css.base.uibase.inner.OnToolBarClickListener

import java.io.Serializable

/**
 * @author Ruis
 * @date 2021/5/6
 */
class ToolBarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    Toolbar(context, attrs, defStyleAttr), View.OnClickListener {

    enum class ToolBarBg : Serializable {
         WHITE/*白色*/, GRAY/*灰色*/
    }

    enum class ViewType {
        LEFT_IMAGE, LEFT_TEXT, CENTER_TEXT, RIGHT_TEXT, RIGHT_IMAGE
    }

    private var imgLeft: AppCompatImageView
    private var tvCenter: AppCompatTextView
    private var tvRight: AppCompatTextView
    private var imgRight: AppCompatImageView
    private var bottomLine: View
    private var binding: ViewToolbarBinding

    private var listener: OnToolBarClickListener? = object : OnToolBarClickListener {
        override fun onClickToolBarView(view: View, event: ViewType) {
            when (event) {
                ViewType.LEFT_IMAGE -> ActivityUtils.getActivityByContext(context).onBackPressed()
            }
        }
    }

    fun setToolBarClickListener(listener: OnToolBarClickListener): ToolBarView {
        this.listener = listener
        return this
    }

    init {
        binding = ViewToolbarBinding.inflate(LayoutInflater.from(context),this)
        val view = binding.root
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.toolbarView)
        val leftTvStr1 = typedArray.getString(R.styleable.toolbarView_leftText)
        val leftTvStr2 = typedArray.getResourceId(R.styleable.toolbarView_leftText, 0)
        val centerTvStr1 = typedArray.getString(R.styleable.toolbarView_centerText)
        val centerTvStr2 = typedArray.getResourceId(R.styleable.toolbarView_centerText, 0)
        val centerTvColor1 = typedArray.getColor(R.styleable.toolbarView_centerTextColor, 0)
        val centerTvSize =
            typedArray.getDimensionPixelSize(R.styleable.toolbarView_centerTextSize, SizeUtils.sp2px(18F))

        val rightTvStr1 = typedArray.getString(R.styleable.toolbarView_rightText)
        val rightTvStr2 = typedArray.getResourceId(R.styleable.toolbarView_rightText, 0)
        val rightTvColor1 = typedArray.getColor(R.styleable.toolbarView_rightTextColor, 0)
        val rightTvSize = typedArray.getDimensionPixelSize(R.styleable.toolbarView_rightTextSize, SizeUtils.sp2px(16F))

        val rightIb = typedArray.getResourceId(R.styleable.toolbarView_rightImageView, 0)
        val leftIb = typedArray.getResourceId(R.styleable.toolbarView_leftImageView, 0)

        val showBottomLine = typedArray.getBoolean(R.styleable.toolbarView_showBottomLine, false)

        typedArray.recycle()

        imgLeft = view.findViewById(R.id.img_left)
        tvCenter = view.findViewById(R.id.tv_center)
        tvRight = view.findViewById(R.id.tv_right)
        imgRight = view.findViewById(R.id.img_right)
        bottomLine = view.findViewById(R.id.bottom_line)
        imgLeft.setOnClickListener(this)
        tvRight.setOnClickListener(this)
        imgRight.setOnClickListener(this)

        showLine(showBottomLine)
        //left text view
        if (!TextUtils.isEmpty(leftTvStr1)) {
            setText(ViewType.LEFT_TEXT, leftTvStr1)
        } else if (leftTvStr2 != 0) {
            setText(ViewType.LEFT_TEXT, context.resources.getString(leftTvStr2))
        }

        //center text view
        if (!TextUtils.isEmpty(centerTvStr1)) {
            setText(ViewType.CENTER_TEXT, centerTvStr1)
        } else if (centerTvStr2 != 0) {
            setText(ViewType.CENTER_TEXT, context.resources.getString(centerTvStr2))
        }
        setTextPxSize(ViewType.CENTER_TEXT, centerTvSize)
        if (centerTvColor1 != 0) {
            setTextColorInt(ViewType.CENTER_TEXT, centerTvColor1)
        }

        //right text view
        if (!TextUtils.isEmpty(rightTvStr1)) {
            setText(ViewType.RIGHT_TEXT, rightTvStr1)
        } else if (rightTvStr2 != 0) {
            setText(ViewType.RIGHT_TEXT, context.resources.getString(rightTvStr2))
        }
        setTextPxSize(ViewType.RIGHT_TEXT, rightTvSize)
        if (rightTvColor1 != 0) {
            setTextColorInt(ViewType.RIGHT_TEXT, rightTvColor1)
        }

        //left image bottom
        if (leftIb != 0) {
            setImageView(ViewType.LEFT_IMAGE, leftIb)
        }
        //right image bottom
        if (rightIb != 0) {
            setImageView(ViewType.RIGHT_IMAGE, rightIb)
        }

    }

    private fun showLine(isVisible: Boolean) {
        bottomLine.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun setImageScaleType(type: ViewType, scaleType: ImageView.ScaleType) {
        val view = getView(type) as AppCompatImageView?
        view?.scaleType = scaleType
    }

    private fun setImageView(type: ViewType, src: Int) {
        val view = getView(type) as AppCompatImageView?
        view?.visibility = View.VISIBLE
        view?.setImageResource(src)
    }

    private fun setImageView(type: ViewType, bm: Bitmap) {
        val view = getView(type) as AppCompatImageView?
        view?.visibility = View.VISIBLE
        view?.setImageBitmap(bm)
    }

    private fun setText(type: ViewType, text: String?) {
        val view = getView(type) as AppCompatTextView?
        view?.visibility = View.VISIBLE
        view?.text = text ?: ""
    }

    private fun setTextPxSize(type: ViewType, size: Int) {
        val view = getView(type) as AppCompatTextView?
        view!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.toFloat())
    }

    private fun setTextColor(type: ViewType, @ColorRes color: Int) {
        setTextColorInt(type, getResourcesColor(color))
    }

    private fun setTextColorInt(type: ViewType, @ColorInt color: Int) {
        val view = getView(type) as AppCompatTextView?
        view?.setTextColor(color)
    }

    @ColorInt
    private fun getResourcesColor(@ColorRes color: Int): Int {
        return ContextCompat.getColor(context, color)
    }

    private fun getView(viewType: ViewType): View? {
        var map = mapOf(
            ViewType.LEFT_IMAGE to imgLeft,
            ViewType.LEFT_TEXT to binding.tvLeft,
            ViewType.CENTER_TEXT to tvCenter,
            ViewType.RIGHT_IMAGE to imgRight,
            ViewType.RIGHT_TEXT to tvRight,
        )
        return map[viewType];
    }

    fun setToolBarViewVisible(isVisible: Boolean, vararg events: ViewType): ToolBarView {
        for (event in events) {
            when (event) {
                ViewType.LEFT_IMAGE -> setLeftImageVisible(isVisible)
                ViewType.LEFT_TEXT -> setVisible(event, isVisible)
                ViewType.CENTER_TEXT -> setCenterTextVisible(isVisible)
                ViewType.RIGHT_TEXT -> setRightTextVisible(isVisible)
                ViewType.RIGHT_IMAGE -> setRightImageVisible(isVisible)
            }
        }
        return this
    }

    private fun setLeftImageVisible(isVisible: Boolean): ToolBarView {
        setVisible(ViewType.LEFT_IMAGE, isVisible)
        return this
    }

    private fun setCenterTextVisible(isVisible: Boolean): ToolBarView {
        setVisible(ViewType.CENTER_TEXT, isVisible)
        return this
    }

    private fun setRightTextVisible(isVisible: Boolean): ToolBarView {
        setVisible(ViewType.RIGHT_TEXT, isVisible)
        return this
    }

    private fun setRightImageVisible(isVisible: Boolean): ToolBarView {
        setVisible(ViewType.RIGHT_IMAGE, isVisible)
        return this
    }

    private fun setVisible(type: ViewType, isVisible: Boolean) {
        val view = getView(type)
        view?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    fun setLeftText(@StringRes resId: Int): ToolBarView {
        setText(ViewType.LEFT_TEXT, context.resources.getText(resId).toString())
        return this
    }

    fun setLeftText(text: String?): ToolBarView {
        setText(ViewType.LEFT_TEXT, text)
        return this
    }

    fun setLeftTextColor(@ColorRes color: Int): ToolBarView {
        setTextColor(ViewType.LEFT_TEXT, color)
        return this
    }

    fun setLeftTextColorInt(@ColorInt color: Int): ToolBarView {
        setTextColorInt(ViewType.LEFT_TEXT, color)
        return this
    }

    fun setCenterText(@StringRes resId: Int): ToolBarView {
        setText(ViewType.CENTER_TEXT, context.resources.getText(resId).toString())
        return this
    }

    fun setCenterText(text: String?): ToolBarView {
        setText(ViewType.CENTER_TEXT, text)
        return this
    }

    fun setCenterTextColor(@ColorRes color: Int): ToolBarView {
        setTextColor(ViewType.CENTER_TEXT, color)
        return this
    }

    fun setCenterTextColorInt(@ColorInt color: Int): ToolBarView {
        setTextColorInt(ViewType.CENTER_TEXT, color)
        return this
    }

    fun setRightText(@StringRes resId: Int): ToolBarView {
        setText(ViewType.RIGHT_TEXT, context.resources.getText(resId).toString())
        return this
    }

    fun setRightText(text: String?): ToolBarView {
        setText(ViewType.RIGHT_TEXT, text)
        return this
    }

    fun setRightTextColor(@ColorRes color: Int): ToolBarView {
        setTextColor(ViewType.RIGHT_TEXT, color)
        return this
    }

    fun setRightTextColorInt(@ColorInt color: Int): ToolBarView {
        setTextColorInt(ViewType.RIGHT_TEXT, color)
        return this
    }

    fun setRightImageScaleType(scaleType: ImageView.ScaleType): ToolBarView {
        setImageScaleType(ViewType.RIGHT_IMAGE, scaleType)
        return this
    }

    fun setRightImage(bm: Bitmap): ToolBarView {
        setImageView(ViewType.RIGHT_IMAGE, bm)
        return this
    }

    fun setRightImage(@DrawableRes drawable: Int): ToolBarView {
        setImageView(ViewType.RIGHT_IMAGE, drawable)
        return this
    }

    fun showBottomLine(isVisible: Boolean): ToolBarView {
        showLine(isVisible)
        return this
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.img_left -> {
                listener?.onClickToolBarView(v, ViewType.LEFT_IMAGE)
            }
            R.id.tv_right -> {
                listener?.onClickToolBarView(v, ViewType.RIGHT_TEXT)
            }
            R.id.img_right -> {
                listener?.onClickToolBarView(v, ViewType.RIGHT_IMAGE)
            }
        }
    }

}
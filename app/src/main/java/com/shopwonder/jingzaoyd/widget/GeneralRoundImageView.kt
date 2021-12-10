package com.shopwonder.jingzaoyd.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.shopwonder.jingzaoyd.R
import com.shopwonder.jingzaoyd.policy.GeneralRoundViewImpl
import com.shopwonder.jingzaoyd.policy.IRoundView
import org.jetbrains.annotations.Nullable

class GeneralRoundImageView : AppCompatImageView, IRoundView {
    private var generalRoundViewImpl: GeneralRoundViewImpl? = null

    @JvmOverloads
    constructor(context: Context, @Nullable attrs: AttributeSet? = null) : super(context, attrs) {
        init(this, context, attrs)
    }

    constructor(
        context: Context,
        @Nullable attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(this, context, attrs)
    }

    override fun setCornerRadius(cornerRadius: Float) {
        generalRoundViewImpl?.setCornerRadius(cornerRadius)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        generalRoundViewImpl?.onLayout(changed, left, top, right, bottom)
    }

    protected override fun dispatchDraw(canvas: Canvas?) {
        generalRoundViewImpl?.beforeDispatchDraw(canvas)
        super.dispatchDraw(canvas)
        generalRoundViewImpl?.afterDispatchDraw(canvas)
    }

    private fun init(view: GeneralRoundImageView, context: Context, attrs: AttributeSet?) {
        generalRoundViewImpl = GeneralRoundViewImpl(
            view,
            context,
            attrs,
            R.styleable.GeneralRoundImageView,
            R.styleable.GeneralRoundImageView_corner_radius
        )
    }
}
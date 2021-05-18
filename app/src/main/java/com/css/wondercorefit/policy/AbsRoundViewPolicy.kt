package com.css.wondercorefit.policy

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.css.wondercorefit.utils.dip2px

/**
 * 圆角策略抽象类
 * @author chenPan
 *
 * @time Created by 2021/05/18
 *
 */
abstract class AbsRoundViewPolicy(
    view: View,
    context: Context,
    attributeSet: AttributeSet?,
    attrs: IntArray,
    attrIndex: Int
) :
    IRoundViewPolicy {
    var mCornerRadius: Float = 0f
    val mContainer: View = view

    init {
        initialize(context, attributeSet, attrs, attrIndex)
    }

    private fun initialize(context: Context, attributeSet: AttributeSet?, attrs: IntArray, attrIndex: Int) {
        val typedArray = context.obtainStyledAttributes(attributeSet, attrs)
        //get corner radius from xml file, default value is DEFAULT_CORNER_DP_SIZE
        mCornerRadius = typedArray.getDimension(
            attrIndex,
            dip2px(context, DEFAULT_CORNER_DP_SIZE)
        )
        typedArray.recycle()
    }

    override fun setCornerRadius(cornerRadius: Float) {
        this.mCornerRadius = cornerRadius
    }

    companion object {
        const val DEFAULT_CORNER_DP_SIZE = 4f
    }

}
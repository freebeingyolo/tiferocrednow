package com.css.ble.ui.view

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Nullable
import com.css.ble.R

/**
 * @author yuedong
 * @date 2021-06-01
 */
class LoadingView : View {

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
        if (attrs != null) {
            val array: TypedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CircleAnnulusProgressBar)


            array.recycle()
        }
    }

}
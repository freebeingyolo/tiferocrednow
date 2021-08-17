package com.css.base.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.css.base.R
import razerdp.basepopup.BasePopupWindow

class LoadingDialog : BasePopupWindow {
    constructor(context: Context) : super(context)

    /**
     * 通过dialog构造的弹窗，可以显示在dialog之上
     */
    constructor(dialog: Dialog) : super(dialog)

    var imageView: ImageView

    init {
        popupGravity = Gravity.CENTER
        imageView = findViewById(R.id.image)
        val rotateAnimation: Animation = AnimationUtils.loadAnimation(context, R.anim.rotate_anim)
        val lin = LinearInterpolator()
        rotateAnimation.interpolator = lin
        imageView.startAnimation(rotateAnimation)
    }

    override fun onCreateContentView(): View {
        return createPopupById(R.layout.popup_loading)
    }

    override fun onDismiss() {
        super.onDismiss()
        imageView.clearAnimation()
    }
}
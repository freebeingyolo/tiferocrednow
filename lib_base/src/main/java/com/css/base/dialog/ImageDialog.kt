package com.css.base.dialog

import android.app.Dialog
import android.content.Context
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.alibaba.android.arouter.launcher.ARouter
import com.css.base.R
import com.css.service.router.ARouterConst
import razerdp.basepopup.BasePopupWindow

class ImageDialog : BasePopupWindow  {

    private var mImageView : AppCompatImageView
    private var mContent :TextView
    private var mTimer: CountDownTimer? = null
    constructor(context: Context) : super(context)

    /**
     * 通过dialog构造的弹窗，可以显示在dialog之上
     */
    constructor(dialog: Dialog) : super(dialog)
    init {
        popupGravity = Gravity.CENTER
        mImageView = findViewById(R.id.image)
        mContent = findViewById(R.id.tv_center)
        mTimer = object : CountDownTimer(1 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {

                dismiss()
            }
        }
        mTimer!!.start()
    }
    override fun onCreateContentView(): View {
        return createPopupById(R.layout.popup_common_image)
    }

    fun setContent(content: CharSequence?) {
        content?.let {
            mContent.text = it
        }
    }

    fun setContent(content: Int?) {
        content?.let {
            mContent.setText(it)
        }
    }
    fun setImage(resources:Int){

    }
}
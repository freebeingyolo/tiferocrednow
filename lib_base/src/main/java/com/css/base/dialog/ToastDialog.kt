package com.css.base.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import androidx.appcompat.widget.AppCompatTextView
import com.alibaba.android.arouter.launcher.ARouter
import com.css.base.R
import com.css.service.router.ARouterConst
import razerdp.basepopup.BasePopupWindow
import razerdp.util.animation.AnimationHelper
import razerdp.util.animation.TranslationConfig

class ToastDialog : BasePopupWindow {
    private var mTvTime: AppCompatTextView
    private var mTimer: CountDownTimer? = null

    constructor(context: Context) : super(context)

    /**
     * 通过dialog构造的弹窗，可以显示在dialog之上
     */
    constructor(dialog: Dialog) : super(dialog)

    init {
        popupGravity = Gravity.BOTTOM
        setBackground(Color.TRANSPARENT)
        mTvTime = findViewById(R.id.time)
        mTimer = object : CountDownTimer(3 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                var seconds = millisUntilFinished / 1000 + 1
                mTvTime.text = "${seconds}s"
            }

            override fun onFinish() {
                ARouter.getInstance()
                    .build(ARouterConst.PATH_APP_BLE_DEVICELIST)
                    .navigation()
                dismiss()
            }
        }
        mTimer!!.start()
    }

    override fun onCreateContentView(): View {
        return createPopupById(R.layout.popup_to_binding_toast_layout)
    }
    override fun onCreateShowAnimation(): Animation {
        return AnimationHelper.asAnimation().withTranslation(TranslationConfig.FROM_TOP).toShow()
    }

    override fun onCreateDismissAnimation(): Animation {
        return AnimationHelper.asAnimation().withTranslation(TranslationConfig.TO_BOTTOM).toShow()
    }

}
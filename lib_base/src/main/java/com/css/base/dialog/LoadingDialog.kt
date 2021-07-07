package com.css.base.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import com.css.base.R
import razerdp.basepopup.BasePopupWindow

class LoadingDialog : BasePopupWindow {
    constructor(context: Context) : super(context)

    /**
     * 通过dialog构造的弹窗，可以显示在dialog之上
     */
    constructor(dialog: Dialog) : super(dialog)

    init {
        popupGravity = Gravity.CENTER
    }

    override fun onCreateContentView(): View {
        return createPopupById(R.layout.popup_loading)
    }
}
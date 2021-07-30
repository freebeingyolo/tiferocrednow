package com.css.base.dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.animation.Animation
import androidx.appcompat.widget.AppCompatTextView
import com.css.base.R
import com.css.base.dialog.inner.DialogClickListener
import com.css.service.utils.DoubleClickUtils
import razerdp.basepopup.BasePopupWindow
import razerdp.util.animation.AnimationHelper
import razerdp.util.animation.TranslationConfig

class CenterCommonAlertDialog  : BasePopupWindow, View.OnClickListener {
    private var listener: DialogClickListener? = null
    private var tvContent: AppCompatTextView
    private var tvLeft: AppCompatTextView
    private var tvRight: AppCompatTextView

    constructor(context: Context) : super(context)

    /**
     * 通过dialog构造的弹窗，可以显示在dialog之上
     */
    constructor(dialog: Dialog) : super(dialog)

    init {
        tvContent = findViewById(R.id.tv_tip_content)
        tvLeft = findViewById(R.id.tv_left)
        tvRight = findViewById(R.id.tv_right)
        tvLeft.setOnClickListener(this)
        tvRight.setOnClickListener(this)
    }

    override fun onCreateContentView(): View {
        return createPopupById(R.layout.popup_login_tip)
    }

    fun setListener(listener: DialogClickListener?) {
        this.listener = listener
    }


    fun setContent(content: CharSequence?) {
        content?.let {
            tvContent.text = it
        }
    }

    fun setContent(content: Int?) {
        content?.let {
            tvContent.setText(it)
        }
    }

    fun setLeftBtn(left: CharSequence?) {
        tvLeft.text = left
    }

    fun setLeftBtn(left: Int?) {
        left?.let {
            tvLeft.setText(it)
        }
    }

    fun setRightBtn(right: CharSequence?) {
        tvRight.text = right
    }

    fun setRightBtn(right: Int?) {
        right?.let {
            tvRight.setText(it)
        }
    }


    override fun onClick(v: View) {
        if (DoubleClickUtils.instance.isInvalidClick()) return
        when (v.id) {
            R.id.tv_left -> {
                dismiss()
                listener?.onLeftBtnClick(v)
            }
            R.id.tv_right -> {
                dismiss()
                listener?.onRightBtnClick(v)
            }

        }
    }
}
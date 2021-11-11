package com.yxsh.uibase.dialog

import android.app.Dialog
import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import com.css.base.R
import com.css.base.dialog.inner.DialogClickListener
import com.css.service.utils.DoubleClickUtils
import razerdp.basepopup.BasePopupWindow

class ScrollDialog : BasePopupWindow, View.OnClickListener {

    private var listener: DialogClickListener? = null
    private var tvTitle: AppCompatTextView
    private var tvContent: AppCompatTextView
    private var btnTop: Button
    private var btnBottom: Button

    constructor(context: Context) : super(context)

    /**
     * 通过dialog构造的弹窗，可以显示在dialog之上
     */
    constructor(dialog: Dialog) : super(dialog)

    init {
        tvTitle = findViewById(R.id.tv_title)
        tvContent = findViewById(R.id.tv_content)
        btnTop = findViewById(R.id.btn_top)
        btnBottom = findViewById(R.id.btn_bottom)
        tvContent.setMovementMethod(LinkMovementMethod.getInstance())
        btnTop.setOnClickListener(this)
        btnBottom.setOnClickListener(this)
    }

    override fun onCreateContentView(): View {
        return createPopupById(R.layout.popup_scroll)
    }

    fun setListener(listener: DialogClickListener?) {
        this.listener = listener
    }

    fun setTitle(title: CharSequence) {
        title?.let {
            tvTitle.text = title
        }
    }

    fun setContent(content: CharSequence) {
        content?.let {
            tvContent.text = content
        }
    }
    
    fun setTopBtn(top: CharSequence?) {
        top?.let {
            btnTop.text = top
        }
    }

    fun setBottomBtn(bottom: CharSequence?) {
        bottom?.let {
            btnBottom.text = bottom
        }
    }

    override fun onClick(v: View) {
        if (DoubleClickUtils.instance.isInvalidClick()) return
        when (v.id) {
            R.id.btn_top -> {
                dismiss()
                listener?.onLeftBtnClick(v)
            }
            R.id.btn_bottom -> {
                dismiss()
                listener?.onRightBtnClick(v)
            }
        }
    }


}
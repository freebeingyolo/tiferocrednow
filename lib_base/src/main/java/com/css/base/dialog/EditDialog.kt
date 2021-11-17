package com.css.base.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.SizeUtils
import com.css.base.R
import com.css.base.dialog.inner.DialogClickListener
import com.css.service.utils.DoubleClickUtils
import razerdp.basepopup.BasePopupWindow


/**
 * @author Ruis
 * @date 2020/1/29
 */
class EditDialog : BasePopupWindow, View.OnClickListener {
    private var listener: DialogClickListener? = null
    private var tvTitle: AppCompatTextView
    private var etContent: AppCompatEditText
    private var tvLeft: AppCompatTextView
    private var tvRight: AppCompatTextView
    private var tvHint: AppCompatTextView
    private var ivClean: AppCompatImageView

    private constructor(context: Context) : super(context)

    /**
     * 通过dialog构造的弹窗，可以显示在dialog之上
     */
    private constructor(dialog: Dialog) : super(dialog)

    init {
        setAdjustInputMethod(true)
        tvTitle = findViewById(R.id.tv_title)
        etContent = findViewById(R.id.et_content)
        tvLeft = findViewById(R.id.tv_left)
        tvRight = findViewById(R.id.tv_right)
        ivClean = findViewById(R.id.iv_clean)
        tvHint = findViewById(R.id.tv_hint)
        tvLeft.setOnClickListener(this)
        ivClean.setOnClickListener(this)
        tvRight.setOnClickListener(this)
//        StringUtils.lengthFilter(context, etContent, 12, "设备名称12个字符以内、不可包含符号，无法保存设备名称")
        etContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (etContent.text.isNullOrEmpty()) {
//                    tvHint.visibility = View.VISIBLE
                    ivClean.visibility = View.GONE
                } else {
//                    tvHint.visibility = View.GONE
                    ivClean.visibility = View.VISIBLE
                }
            }

        })
    }

    override fun onCreateContentView(): View {
        return createPopupById(R.layout.popup_common_edit)
    }

    fun setListener(listener: DialogClickListener?) {
        this.listener = listener
    }

    fun setTitleVisibility(visibility: Int?) {
        visibility?.let {
            tvTitle.visibility = it
        }
    }

    fun setTitle(titleResId: Int?) {
        titleResId?.let {
            tvTitle.setText(it)
        }
    }

    fun setTitle(title: CharSequence?) {
        title?.let {
            tvTitle.text = it
        }
    }

    fun setContentColor(color: Int?) {
        color?.let {
            etContent.setTextColor(it)
        }
    }

    fun setContentGravity(int: Int) {
        etContent.gravity = int
    }

    fun setContentStyle(tf: Typeface?) {
        tf?.let {
            etContent.typeface = it
        }
    }

    fun setContentMarginTop(dpTop: Int?) {
        dpTop?.let {
            val params = etContent.layoutParams as LinearLayout.LayoutParams
            params.setMargins(
                params.leftMargin,
                SizeUtils.dp2px(it.toFloat()),
                params.rightMargin,
                params.bottomMargin
            )
            etContent.layoutParams = params
        }
    }

    fun setContent(content: CharSequence?) {
        content?.let {
            etContent.setText(it)
        }
    }

    fun setContent(content: Int?) {
        content?.let {
            etContent.setText(it)
        }
    }

    fun setHint(hint: CharSequence?) {
        hint?.let {
            etContent.hint = it
        }
    }

    fun setHint(hint: Int?) {
        hint?.let {
            etContent.setHint(it)
        }
    }

    fun setInputType(v: Int) {
        var v1 = etContent.inputType
        etContent.inputType = v
    }

    fun setLeftBtn(left: CharSequence?) {
        left?.let {
            tvLeft.text = it
        }
    }

    fun setLeftBtn(left: Int?) {
        left?.let {
            tvLeft.setText(it)
        }
    }

    fun setLeftBtnColor(@ColorRes color: Int?) {
        color?.let {
            setLeftBtnColorInt(ContextCompat.getColor(context, it))
        }
    }

    fun setLeftBtnColorInt(@ColorInt color: Int?) {
        color?.let {
            tvLeft.setTextColor(it)
        }
    }


    fun setRightBtn(right: CharSequence?) {
        right?.let {
            tvRight.text = it
        }
    }

    fun setRightBtn(right: Int?) {
        right?.let {
            tvRight.setText(it)
        }
    }

    fun setRightBtnColor(@ColorRes color: Int?) {
        color?.let {
            setRightBtnColorInt(ContextCompat.getColor(context, it))
        }
    }

    fun setRightBtnColorInt(@ColorInt color: Int?) {
        color?.let {
            tvRight.setTextColor(it)
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
                listener?.onRightEditBtnClick(v, etContent.text.toString().trimEnd())
            }
            R.id.iv_clean -> {
                etContent.setText("")
            }
        }
    }

    fun show() {
        showPopupWindow()
    }

    class Builder {
        var outSideDismiss = true//设置BasePopup是否允许点击外部触发Dismiss
        var backPressEnable = true//设置BasePopup是否允许返回键dismiss
        var gravity: Int = Gravity.CENTER
        var title: CharSequence? = null
        var hint: CharSequence? = null
        var leftBtnText: CharSequence? = null
        var rightBtnText: CharSequence? = null
        var inputType = 1

        fun build(ctx: Context) = run {
            val ret = EditDialog(ctx).apply {
                setUp(this)
            }
            ret
        }

        fun build(dlg: Dialog) = run {
            val ret = EditDialog(dlg).apply {
                setUp(this)
            }
            ret
        }

        private fun setUp(dlg: EditDialog) {
            dlg.setOutSideDismiss(outSideDismiss)
            dlg.setBackPressEnable(backPressEnable)
            dlg.popupGravity = gravity
            dlg.setTitle(title)
            dlg.setHint(hint)
            dlg.setLeftBtn(leftBtnText)
            dlg.setRightBtn(rightBtnText)
            dlg.setInputType(inputType)
        }
    }
}
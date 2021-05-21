package com.css.base.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.text.InputFilter
import android.text.Spanned
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.SizeUtils
import com.css.base.R
import com.css.base.dialog.inner.DialogClickListener
import com.css.service.utils.DoubleClickUtils

import razerdp.basepopup.BasePopupWindow
import java.util.regex.Matcher
import java.util.regex.Pattern

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

    constructor(context: Context) : super(context)

    /**
     * 通过dialog构造的弹窗，可以显示在dialog之上
     */
    constructor(dialog: Dialog) : super(dialog)

    init {
        setAdjustInputMethod(true)
        tvTitle = findViewById(R.id.tv_title)
        etContent = findViewById(R.id.et_content)
        tvLeft = findViewById(R.id.tv_left)
        tvRight = findViewById(R.id.tv_right)
        tvLeft.setOnClickListener(this)
        tvRight.setOnClickListener(this)
        setEditTextInhibitInputSpeChat(etContent)
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
                dismiss()
                listener?.onRightEditBtnClick(v, etContent.text.toString().trimEnd())
            }
        }
    }

    /**
     * 禁止EditText输入特殊字符
     * @param editText
     */
    fun setEditTextInhibitInputSpeChat(editText: EditText) {
        val filter: InputFilter = InputFilter { source, start, end, dest, dstart, dend ->
            val speChat = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]"
            val pattern: Pattern = Pattern.compile(speChat)
            val matcher: Matcher = pattern.matcher(source.toString())
            if (matcher.find()) "" else null
        }
        editText.setFilters(arrayOf<InputFilter>(filter))
    }


}
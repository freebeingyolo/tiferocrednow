package com.css.base.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import com.css.base.dialog.inner.DialogClickListener
import com.yxsh.uibase.dialog.ConfirmDialog
import com.yxsh.uibase.dialog.SingleDialog
import razerdp.basepopup.BasePopupWindow

/**
 * 通用的弹窗统一处理类（后续新增的样式可在此处扩展）
 * author Ruis
 * date 2019/11/7
 */
class CommonAlertDialog {

    var context: Context
    var dialog: BasePopupWindow? = null

    // 通过dialog构造的弹窗，可以显示在dialog之上
    var attachDialog: Dialog? = null

    var type: DialogType? = null
    var titleResId: Int? = null
    var title: CharSequence? = null
    var listener: DialogClickListener? = null
    var onDismissListener: BasePopupWindow.OnDismissListener? = null
    var contentResId: Int? = null
    var content: CharSequence? = null
    var contentGravity: Int? = null
    var hintResId: Int? = null
    var hint: CharSequence? = null
    var leftBtnTextResId: Int? = null
    var leftBtnText: CharSequence? = null
    var leftBtnColor: Int? = null
    var leftBtnColorInt: Int? = null
    var rightBtnTextResId: Int? = null
    var rightBtnText: CharSequence? = null
    var rightBtnColor: Int? = null
    var rightBtnColorInt: Int? = null
    var confirmBtnTextResId: Int? = null
    var confirmBtnText: CharSequence? = null
    var confirmBtnColor: Int? = null
    var confirmBtnColorInt: Int? = null
    var imageResources: Int = 0
    var openBackgroudColor: Boolean = true
    var outSideDismiss = true//设置BasePopup是否允许点击外部触发Dismiss
    var backPressEnable = true//设置BasePopup是否允许返回键dismiss

    constructor(context: Context) {
        this.context = context
    }

    /**
     * 通过dialog构造的弹窗，可以显示在dialog之上
     */
    constructor(dialog: Dialog) {
        this.context = dialog.context
        this.attachDialog = dialog
    }

    enum class DialogType {
        Confirm, // 从上往下显示: 标题、内容、两个操作按钮
        SINGLE,// 从上往下显示: 标题、内容、一个操作按钮
        Edit,//从上往下显示: 标题、编辑框、两个操作按钮
        Image//图片 文字
    }

    fun show() {
        if (dialog != null && dialog!!.isShowing) {
            return
        }
        if (attachDialog == null) {
            when (type) {
                DialogType.Confirm -> {
                    dialog = ConfirmDialog(context)
                }
                DialogType.SINGLE -> {
                    dialog = SingleDialog(context)
                }
                DialogType.Edit -> {
                    dialog = EditDialog(context)
                }
                DialogType.Image -> {
                    dialog = ImageDialog(context)
                }
            }
        } else {
            when (type) {
                DialogType.Confirm -> {
                    dialog = ConfirmDialog(attachDialog!!)
                }
                DialogType.SINGLE -> {
                    dialog = SingleDialog(attachDialog!!)
                }
                DialogType.Edit -> {
                    dialog = EditDialog(attachDialog!!)
                }
                DialogType.Image -> {
                    dialog = ImageDialog(context)
                }
            }
        }
        requireNotNull(dialog) { "使用CommonAlertDialog请传入支持的DialogType" }
        when (dialog) {
            is ConfirmDialog -> {
                val confirmDialog = dialog as ConfirmDialog
                var hasTitle = false
                if (titleResId != null) {
                    hasTitle = true
                    confirmDialog.setTitle(titleResId)
                } else if (!TextUtils.isEmpty(title)) {
                    hasTitle = true
                    confirmDialog.setTitle(title)
                }
                if (hasTitle) {
                    confirmDialog.setTitleVisibility(View.VISIBLE)
                } else {
                    confirmDialog.setTitleVisibility(View.GONE)
                    confirmDialog.setContentStyle(Typeface.DEFAULT_BOLD)
                    confirmDialog.setContentColor(Color.parseColor("#222222"))
                    confirmDialog.setContentMarginTop(26)
                }
                if (contentResId != null) {
                    confirmDialog.setContent(contentResId)
                } else if (!TextUtils.isEmpty(content)) {
                    confirmDialog.setContent(content)
                }
                if (contentGravity != null) {
                    confirmDialog.setContentGravity(contentGravity!!)
                }
                if (leftBtnTextResId != null) {
                    confirmDialog.setLeftBtn(leftBtnTextResId)
                } else if (!TextUtils.isEmpty(leftBtnText)) {
                    confirmDialog.setLeftBtn(leftBtnText)
                }
                if (rightBtnTextResId != null) {
                    confirmDialog.setRightBtn(rightBtnTextResId)
                } else if (!TextUtils.isEmpty(rightBtnText)) {
                    confirmDialog.setRightBtn(rightBtnText)
                }
                if (leftBtnColor != null) {
                    confirmDialog.setLeftBtnColor(leftBtnColor)
                } else if (leftBtnColorInt != null) {
                    confirmDialog.setLeftBtnColorInt(leftBtnColorInt)
                }
                if (rightBtnColor != null) {
                    confirmDialog.setRightBtnColor(rightBtnColor)
                } else if (rightBtnColorInt != null) {
                    confirmDialog.setRightBtnColorInt(rightBtnColorInt)
                }
                confirmDialog.setListener(listener)
            }
            is SingleDialog -> {
                val singleDialog = dialog as SingleDialog
                var hasTitle = false
                if (titleResId != null) {
                    hasTitle = true
                    singleDialog.setTitle(titleResId)
                } else if (!TextUtils.isEmpty(title)) {
                    hasTitle = true
                    singleDialog.setTitle(title)
                }
                if (hasTitle) {
                    singleDialog.setTitleVisibility(View.VISIBLE)
                } else {
                    singleDialog.setTitleVisibility(View.GONE)
                    singleDialog.setContentStyle(Typeface.DEFAULT_BOLD)
                    singleDialog.setContentColor(Color.parseColor("#222222"))
                    singleDialog.setContentMarginTop(26)
                }
                if (contentResId != null) {
                    singleDialog.setContent(contentResId)
                } else if (!TextUtils.isEmpty(content)) {
                    singleDialog.setContent(content)
                }
                if (contentGravity != null) {
                    singleDialog.setContentGravity(contentGravity!!)
                }
                if (confirmBtnTextResId != null) {
                    singleDialog.setConfirmBtn(confirmBtnTextResId)
                } else if (!TextUtils.isEmpty(confirmBtnText)) {
                    singleDialog.setConfirmBtn(confirmBtnText)
                }
                if (confirmBtnColor != null) {
                    singleDialog.setConfirmBtnColor(confirmBtnColor)
                } else if (confirmBtnColorInt != null) {
                    singleDialog.setConfirmBtnColorInt(confirmBtnColorInt)
                }
                singleDialog.setListener(listener)
            }
            is EditDialog -> {
                val editDialog = dialog as EditDialog
                var hasTitle = false
                if (titleResId != null) {
                    hasTitle = true
                    editDialog.setTitle(titleResId)
                } else if (!TextUtils.isEmpty(title)) {
                    hasTitle = true
                    editDialog.setTitle(title)
                }
                if (hasTitle) {
                    editDialog.setTitleVisibility(View.VISIBLE)
                } else {
                    editDialog.setTitleVisibility(View.GONE)
                    editDialog.setContentStyle(Typeface.DEFAULT_BOLD)
                    editDialog.setContentColor(Color.parseColor("#222222"))
                    editDialog.setContentMarginTop(26)
                }
                if (contentResId != null) {
                    editDialog.setContent(contentResId)
                } else if (!TextUtils.isEmpty(content)) {
                    editDialog.setContent(content)
                }
                if (contentGravity != null) {
                    editDialog.setContentGravity(contentGravity!!)
                }
                if (hintResId != null) {
                    editDialog.setHint(hintResId)
                } else if (!TextUtils.isEmpty(hint)) {
                    editDialog.setHint(hint)
                }
                if (leftBtnTextResId != null) {
                    editDialog.setLeftBtn(leftBtnTextResId)
                } else if (!TextUtils.isEmpty(leftBtnText)) {
                    editDialog.setLeftBtn(leftBtnText)
                }
                if (rightBtnTextResId != null) {
                    editDialog.setRightBtn(rightBtnTextResId)
                } else if (!TextUtils.isEmpty(rightBtnText)) {
                    editDialog.setRightBtn(rightBtnText)
                }
                if (leftBtnColor != null) {
                    editDialog.setLeftBtnColor(leftBtnColor)
                } else if (leftBtnColorInt != null) {
                    editDialog.setLeftBtnColorInt(leftBtnColorInt)
                }
                if (rightBtnColor != null) {
                    editDialog.setRightBtnColor(rightBtnColor)
                } else if (rightBtnColorInt != null) {
                    editDialog.setRightBtnColorInt(rightBtnColorInt)
                }
                editDialog.setListener(listener)
            }
            is ImageDialog -> {
                val imageDialog = dialog as ImageDialog
                if (contentResId != null) {
                    imageDialog.setContent(contentResId)
                } else if (!TextUtils.isEmpty(content)) {
                    imageDialog.setContent(content)
                }
                if (imageResources != 0) {
                    imageDialog.setImage(imageResources)
                }
            }
        }
        dialog!!.onDismissListener = onDismissListener
        if (openBackgroudColor) {
            dialog!!.setBackgroundColor(Color.parseColor("#4D000000"))
        } else {
            dialog!!.setBackground(0)
        }
        dialog!!.setPopupGravity(Gravity.CENTER)
            .setOutSideDismiss(outSideDismiss)
            .setBackPressEnable(backPressEnable)
        dialog!!.showPopupWindow()
    }

}
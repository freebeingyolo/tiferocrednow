package com.css.base.uibase.inner

import android.view.View
import com.css.base.view.ToolBarView

/**
 * @author Ruis
 * @date 2021/5/6
 */
interface OnToolBarClickListener {

    fun onClickToolBarView(view: View, event: ToolBarView.ViewType)
}
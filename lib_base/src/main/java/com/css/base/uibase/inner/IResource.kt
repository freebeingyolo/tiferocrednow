package com.css.base.uibase.inner

import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.Utils

/**
 *@author baoyuedong
 *@time 2021-07-22 9:33
 *@description :
 */
interface IResource {

    fun getString(@StringRes id: Int): String {
        return Utils.getApp().getString(id)
    }

    fun getColor(@ColorRes id: Int): Int {
        return ContextCompat.getColor(Utils.getApp(), id)
    }

    fun getDrawable(@DrawableRes id: Int): Drawable? {
        return ContextCompat.getDrawable(Utils.getApp(), id)
    }


}
package com.css.base.utils

import android.text.Html
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter

/**
 *@author baoyuedong
 *@time 2021-08-02 14:47
 *@description  绑定布局文件xml的属性和本地方法
 */
object BindingUtils {

    @JvmStatic
    @BindingAdapter("android:htmlText")
    fun loadHtml(textView: TextView, content: String?) {
        if (!content.isNullOrEmpty()) {
            textView.text = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                Html.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(content)
            }
        }
    }

}
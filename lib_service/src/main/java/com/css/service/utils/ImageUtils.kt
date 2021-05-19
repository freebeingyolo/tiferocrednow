package com.css.service.utils

import android.graphics.Bitmap
import android.view.View

/**
 * @author yuedong
 * @date 2021-05-18
 */
object ImageUtils {

    //获取View的Bitmap
    fun getBitmap(view: View): Bitmap {
        //打开图像缓存
        view.setDrawingCacheEnabled(true);
        //需要调用measure和layout方法
        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        //发送位置和尺寸到view及其所有的子view
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        //获得可视化组件的截图
        val bitmap = Bitmap.createBitmap(view.drawingCache)
        view.setDrawingCacheEnabled(false);
        return bitmap
    }
}
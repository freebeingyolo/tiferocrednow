package com.css.wondercorefit.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import com.css.wondercorefit.R
import com.seagazer.liteplayer.LitePlayerView
import com.seagazer.liteplayer.config.PlayerType
import com.seagazer.liteplayer.helper.DpHelper
import com.seagazer.liteplayer.helper.MediaLogger
import com.seagazer.liteplayer.listener.RenderStateChangedListener
import com.seagazer.liteplayer.listener.SimplePlayerStateChangedListener
import com.seagazer.liteplayer.widget.IOverlay

/**
 *
 * Author: chenPan
 * Date: 2020/05/20
 */
class ErrorOverlay @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr), IOverlay {

    init {
        val padding = DpHelper.dp2px(context, 8f)
        setPadding(padding, padding, padding, padding)
        textSize = DpHelper.sp2px(context, 6f).toFloat()
        setTextColor(Color.WHITE)
        setBackgroundResource(R.drawable.bg_loading_overlay)
    }

    override fun attachPlayer(player: LitePlayerView) {
        player.addView(
            this, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            })
    }

    override fun getView() = this

    override fun show() {
        if (!isShowing()) {
            MediaLogger.d("show cover")
            alpha = 1f
        }
    }

    override fun hide() {
        if (isShowing()) {
            MediaLogger.d("hide cover")
            animate().alpha(0f).start()
        }
    }

    override fun isShowing() = alpha == 1f

    override fun getPlayerStateChangedListener() = object : SimplePlayerStateChangedListener() {

        override fun onError(playerType: PlayerType, errorCode: Int) {
            text = "播放错误:$errorCode"
            show()
        }

        override fun onPlaying() {
            hide()
        }
    }

    override fun getRenderStateChangedListener(): RenderStateChangedListener? = null

    override fun displayModeChanged(isFullScreen: Boolean) {
    }

    override fun autoSensorModeChanged(isAutoSensor: Boolean) {
    }

    override fun floatWindowModeChanged(isFloatWindow: Boolean) {
    }
}
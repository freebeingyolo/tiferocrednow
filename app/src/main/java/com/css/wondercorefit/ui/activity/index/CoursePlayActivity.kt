package com.css.wondercorefit.ui.activity.index

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.facade.annotation.Route
import com.css.service.router.ARouterConst
import com.css.wondercorefit.R
import com.css.wondercorefit.utils.ConfigHolder
import com.css.wondercorefit.utils.LoadingOverlay
import com.css.wondercorefit.widget.MediaController
import com.seagazer.liteplayer.LitePlayerView
import com.seagazer.liteplayer.bean.DataSource
import com.seagazer.liteplayer.listener.PlayerViewModeChangedListener
import com.seagazer.liteplayer.widget.LiteGestureController
import com.seagazer.liteplayer.widget.LiteMediaTopbar

@Route(path = ARouterConst.PATH_APP_MAIN_COURSE)
class CoursePlayActivity : AppCompatActivity() {
    private lateinit var playerView: LitePlayerView
    private var fullScreenPlay: Int = 0
    private val handler = Handler()
    private var currentPlayIndex = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_play)
        var bundle = this.intent.extras
        currentPlayIndex = bundle!!.getString("videoLink")!!
        playerView = findViewById(R.id.player_view)
        playerView.setProgressColor(resources.getColor(R.color.colorAccent), Color.YELLOW)
        // config
        playerView.setRenderType(ConfigHolder.renderType)
        playerView.setPlayerType(ConfigHolder.playerType)
        // media controller, topbar and gesture controller
        playerView.attachMediaController(MediaController(this))
        playerView.attachMediaTopbar(LiteMediaTopbar(this))
        playerView.attachGestureController(LiteGestureController(this).apply {
            supportSeek = true
            supportBrightness = true
            supportVolume = true
        })
        playerView.addPlayerViewModeChangedListener(object : PlayerViewModeChangedListener {
            override fun onAutoSensorModeChanged(isAutoSensor: Boolean) {

            }

            override fun onFloatWindowModeChanged(isFloatWindow: Boolean) {

            }

            override fun onFullScreenModeChanged(isFullScreen: Boolean) {
                if (!isFullScreen && fullScreenPlay == 1) {
                    finish()
                }
            }

        })
        // custom loading overlay
        playerView.attachOverlay(LoadingOverlay(this))
        playerView.setAutoSensorEnable(false)
        playerView.setAutoHideOverlay(true)
        playerView.setRepeatMode(true)
        playerView.setDataSource(DataSource(currentPlayIndex))
        playerView.start()
        handler.postDelayed({
            playerView.setFullScreenMode(true)
            fullScreenPlay = 1
        }, 500)
    }

    override fun onBackPressed() {
        if (playerView.isFullScreen()) {
            playerView.setFullScreenMode(false)
        } else {
            super.onBackPressed()
        }
    }
}
package com.css.wondercorefit.ui.activity.index

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.css.wondercorefit.R
import com.css.wondercorefit.utils.*
import com.css.wondercorefit.viewmodel.CourseViewModel
import com.seagazer.liteplayer.LitePlayerView
import com.seagazer.liteplayer.bean.DataSource
import com.seagazer.liteplayer.helper.MediaLogger
import com.seagazer.liteplayer.listener.PlayerViewModeChangedListener
import com.seagazer.liteplayer.listener.SimpleRenderStateChangedListener
import com.seagazer.liteplayer.render.RenderTextureView
import com.seagazer.liteplayer.widget.LiteGestureController
import com.seagazer.liteplayer.widget.LiteMediaController
import com.seagazer.liteplayer.widget.LiteMediaTopbar

class CoursePlayActivity : AppCompatActivity() {
    private lateinit var playerView: LitePlayerView
    private val urls =
        listOf(
                Pair(VideoCacheHelper.url(CourseViewModel.urls[0]), CourseViewModel.name[0]),
                Pair(VideoCacheHelper.url(CourseViewModel.urls[1]), CourseViewModel.name[1]),
                Pair(VideoCacheHelper.url(CourseViewModel.urls[2]), CourseViewModel.name[2]),
                Pair(VideoCacheHelper.url(CourseViewModel.urls[3]), CourseViewModel.name[3]),
                Pair(VideoCacheHelper.url(CourseViewModel.urls[4]), CourseViewModel.name[4]),
                Pair(VideoCacheHelper.url(CourseViewModel.urls[5]), CourseViewModel.name[5]),
                Pair(VideoCacheHelper.url(CourseViewModel.urls[6]), CourseViewModel.name[6]),
                Pair(VideoCacheHelper.url(CourseViewModel.urls[7]), CourseViewModel.name[7]),
                Pair(VideoCacheHelper.url(CourseViewModel.urls[7]), CourseViewModel.name[8]),
                Pair(VideoCacheHelper.url(CourseViewModel.urls[9]), CourseViewModel.name[9]),
        )
    private var currentPlayIndex = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_play)
        var bundle = this.intent.extras
        currentPlayIndex = bundle!!.getInt("position")
        playerView = findViewById(R.id.player_view)
        playerView.setProgressColor(resources.getColor(R.color.colorAccent), Color.YELLOW)
        playerView.setAutoSensorEnable(false)
        // config
        playerView.setRenderType(ConfigHolder.renderType)
        playerView.setPlayerType(ConfigHolder.playerType)
        // media controller, topbar and gesture controller
        playerView.attachMediaController(LiteMediaController(this))
        playerView.attachMediaTopbar(LiteMediaTopbar(this))
        playerView.attachGestureController(LiteGestureController(this).apply {
            supportSeek = true
            supportBrightness = true
            supportVolume = true
        })
        // custom loading overlay
        playerView.attachOverlay(LoadingOverlay(this))
        playerView.setAutoSensorEnable(false)
        playerView.setAutoHideOverlay(true)
        // add render listener
        playerView.addRenderStateChangedListener(object : SimpleRenderStateChangedListener() {
            override fun onSurfaceCreated() {
                MediaLogger.d("surface创建")
            }
        })
        // new way to observe fullscreen, floatWindow and autoSensor changed
        playerView.addPlayerViewModeChangedListener(object : PlayerViewModeChangedListener {
            override fun onFullScreenModeChanged(isFullScreen: Boolean) {
                // do something when fullscreen changed
            }

            override fun onFloatWindowModeChanged(isFloatWindow: Boolean) {
                // do something when floatWindow changed
            }

            override fun onAutoSensorModeChanged(isAutoSensor: Boolean) {
                // do something when auto sensor changed
            }

        })
        playerView.setDataSource(DataSource(urls[currentPlayIndex].first, urls[currentPlayIndex].second))
        // start play
        playerView.start()
//        playerView.setFullScreenMode(true)
        val render = playerView.getRender()
        if (render is RenderTextureView) {
            val renderView = render.getRenderView()
            // to do something with this render view, like get capture of surface texture for cover
            val cover = renderView.bitmap
        }
    }
    override fun onBackPressed() {
        if (playerView.isFullScreen()) {
            playerView.setFullScreenMode(false)
        } else {
            super.onBackPressed()
        }
    }
}
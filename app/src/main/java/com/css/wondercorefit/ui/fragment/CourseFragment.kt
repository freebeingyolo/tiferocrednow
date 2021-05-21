package com.css.wondercorefit.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.css.base.uibase.BaseFragment
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.css.service.utils.SystemBarHelper
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.FragmentCourseBinding
import com.css.wondercorefit.ui.activity.index.CoursePlayActivity
import com.css.wondercorefit.utils.ConfigHolder
import com.css.wondercorefit.utils.LoadingOverlay
import com.css.wondercorefit.utils.VideoCacheHelper
import com.css.wondercorefit.viewmodel.CourseViewModel
import com.seagazer.liteplayer.LitePlayerView
import com.seagazer.liteplayer.bean.DataSource
import com.seagazer.liteplayer.list.ListItemChangedListener
import com.seagazer.liteplayer.list.ListPlayer
import com.seagazer.liteplayer.listener.PlayerViewModeChangedListener
import com.seagazer.liteplayer.widget.LiteGestureController


class CourseFragment : BaseFragment<DefaultViewModel, FragmentCourseBinding>() {
    private val TAG = "CourseFragment"
    private lateinit var recyclePlayer: ListPlayer
    private var lastPlayerHolder: RecycleAdapter.VideoHolder? = null
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(activity, 0f)
        SystemBarHelper.setHeightAndPadding(activity, mViewBinding?.topView)
        initRecycle()
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun initRecycle() {
        mViewBinding?.courseRecycle?.layoutManager = GridLayoutManager(context,2, RecyclerView.VERTICAL, false)
        val recyclerAdapter = RecycleAdapter()
        mViewBinding?.courseRecycle?.adapter = recyclerAdapter
        recyclePlayer = ListPlayer(LitePlayerView(context!!)).apply {
            displayProgress(true)
            setProgressColor(
                Color.parseColor("#D81BA2"),
                Color.parseColor("#33618A")
            )
            attachOverlay(LoadingOverlay(context!!))
            attachGestureController(LiteGestureController(context!!).apply {
                supportVolume = false
                supportBrightness = false
            })
            setRenderType(ConfigHolder.renderType)
            setPlayerType(ConfigHolder.playerType)
            // support cache player history progress
            supportHistory = true
            // sample to show and hide video cover
            // onDetachItemView always call before onAttachItemView
            listItemChangedListener = object : ListItemChangedListener {
                override fun onDetachItemView(oldPosition: Int) {
                    lastPlayerHolder?.let {
                        it.videoPoster.visibility = View.VISIBLE
                    }
                }

                override fun onAttachItemView(newPosition: Int) {
                    lastPlayerHolder?.let {
                        it.videoPoster.visibility = View.INVISIBLE
                    }
                }
            }
        }
        recyclePlayer.addPlayerViewModeChangedListener(object : PlayerViewModeChangedListener {
            override fun onAutoSensorModeChanged(isAutoSensor: Boolean) {
                // do something when auto sensor changed
            }

            override fun onFloatWindowModeChanged(isFloatWindow: Boolean) {
                // do something when floatWindow changed
            }

            override fun onFullScreenModeChanged(isFullScreen: Boolean) {
                if (!recyclePlayer.isFullScreen()) {
                    recyclePlayer.pause(true)
                }
            }

        })
        val videoScrollListener = object : ListPlayer.VideoListScrollListener {

            override fun getVideoContainer(position: Int): ViewGroup? {
                mViewBinding?.courseRecycle?.findViewHolderForAdapterPosition(position)?.let {
                    if (it is RecycleAdapter.VideoHolder) {
                        lastPlayerHolder = it
                        return it.videoContainer
                    }
                }
                return null
            }

            override fun getVideoDataSource(position: Int): DataSource? {
                return DataSource(VideoCacheHelper.url(recyclerAdapter.getVideoUrl(position)))
            }
        }
        mViewBinding?.let {
            recyclePlayer.attachToRecyclerView(
                it.courseRecycle,
                false,
                videoScrollListener
            )
        }
    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?
    ): FragmentCourseBinding = FragmentCourseBinding.inflate(inflater, viewGroup, false)

    override fun initViewModel(): DefaultViewModel =
        ViewModelProvider(this).get(DefaultViewModel::class.java)

    inner class RecycleAdapter : RecyclerView.Adapter<RecycleAdapter.VideoHolder>() {

        inner class VideoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val videoTitle: TextView = itemView.findViewById(R.id.video_index)
            val videoPoster: ImageView = itemView.findViewById(R.id.video_poster)
            val videoContainer: FrameLayout = itemView.findViewById(R.id.video_container)

            init {
                itemView.setOnClickListener {
                    val recyclerIntent = Intent(context, CoursePlayActivity::class.java)
                    var bundle = Bundle()
                    bundle.putInt("position", bindingAdapterPosition)
                    recyclerIntent.putExtras(bundle)
                    startActivity(recyclerIntent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
            return VideoHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_view_video,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount() = CourseViewModel.urls.size

        fun getVideoUrl(position: Int) = CourseViewModel.urls[position]

        override fun onBindViewHolder(holder: VideoHolder, position: Int) {
            holder.run {
                videoPoster.setImageResource(CourseViewModel.picture[position])
                videoTitle.text = CourseViewModel.name[position]
            }

        }
    }

    override fun onBackPressed() {
        if (recyclePlayer.isFullScreen()) {
            recyclePlayer.setFullScreenMode(false)
        } else {
            super.onBackPressed()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        recyclePlayer.pause(true)
    }

}
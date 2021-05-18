package com.css.wondercorefit.ui.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.css.base.uibase.BaseFragment
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.css.service.utils.SystemBarHelper
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.FragmentCourseBinding
import com.css.wondercorefit.utils.ConfigHolder
import com.css.wondercorefit.utils.LoadingOverlay
import com.css.wondercorefit.utils.VideoCacheHelper
import com.css.wondercorefit.viewmodel.CourseViewModel
import com.css.wondercorefit.widget.SimpleItemDecoration
import com.seagazer.liteplayer.LitePlayerView
import com.seagazer.liteplayer.bean.DataSource
import com.seagazer.liteplayer.list.ListItemChangedListener
import com.seagazer.liteplayer.list.ListPlayer
import com.seagazer.liteplayer.widget.LiteGestureController
import com.seagazer.liteplayer.widget.LiteMediaController


class CourseFragment : BaseFragment<DefaultViewModel, FragmentCourseBinding>() {
    private val TAG = "MainFragment"

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recyclePlayer: ListPlayer
    private var isAutoPlay = false
    private var lastPlayerHolder: RecycleAdapter.VideoHolder? = null
    private val gridLayoutManager by lazy { GridLayoutManager(context, 2) }
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(activity, 0f)
        SystemBarHelper.setHeightAndPadding(activity, mViewBinding?.topView)
        initRecycle()
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun initRecycle() {
        linearLayoutManager = GridLayoutManager(activity, 2)
        mViewBinding?.courseRecycle?.layoutManager = linearLayoutManager
        val recyclerAdapter = RecycleAdapter()
        mViewBinding?.courseRecycle?.adapter = recyclerAdapter
        recyclePlayer = ListPlayer(LitePlayerView(context!!)).apply {
            displayProgress(true)
            setProgressColor(
                Color.parseColor("#D81BA2"),
                Color.parseColor("#33618A")
            )
            attachOverlay(LoadingOverlay(context!!))
            attachMediaController(LiteMediaController(context!!))
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
                    Log.d(TAG, "detach item: $oldPosition")
                    lastPlayerHolder?.let {
                        it.videoPoster.visibility = View.VISIBLE
                    }
                }

                override fun onAttachItemView(newPosition: Int) {
                    Log.d(TAG, "attach item: $newPosition")
                    lastPlayerHolder?.let {
                        it.videoPoster.visibility = View.INVISIBLE
                    }
                }
            }
        }
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

        mViewBinding?.courseRecycle?.apply {
            adapter = recyclerAdapter
            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return 1
                }
            }
            layoutManager = gridLayoutManager
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
                    if (!isAutoPlay) {
                        recyclePlayer.onItemClick(bindingAdapterPosition)
                    }
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
//                activity?.let { Glide.with(it).load(CourseViewModel.picture[position]).apply(GlideOptionUitls.getCornerOptions(4)).into(videoPoster) }
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
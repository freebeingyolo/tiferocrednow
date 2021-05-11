package com.css.wondercorefit.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseFragment
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.css.service.utils.SystemBarHelper
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.FragmentCourseBinding
import com.css.wondercorefit.utils.ConfigHolder
import com.css.wondercorefit.utils.VideoCacheHelper
import com.css.wondercorefit.viewmodel.CourseViewModel
import com.css.wondercorefit.utils.LoadingOverlay
import com.seagazer.liteplayer.LitePlayerView
import com.seagazer.liteplayer.bean.DataSource
import com.seagazer.liteplayer.helper.MediaLogger
import com.seagazer.liteplayer.list.ListItemChangedListener
import com.seagazer.liteplayer.list.ListPlayer2
import com.seagazer.liteplayer.widget.LiteGestureController
import com.seagazer.liteplayer.widget.LiteMediaController

class CourseFragment : BaseFragment<DefaultViewModel, FragmentCourseBinding>() {
    private lateinit var listPlayer: ListPlayer2
    private var isAutoPlay = true
    private var listPlayerHolder: ListAdapter.VideoHolder? = null
    @SuppressLint("UseRequireInsteadOfGet")
    override fun initView( savedInstanceState: Bundle?) {
        super.initView( savedInstanceState)
        SystemBarHelper.immersiveStatusBar(activity, 0f)
        SystemBarHelper.setHeightAndPadding(activity, mViewBinding?.topView)

        val listAdapter = ListAdapter()
        mViewBinding?.videoListView?.adapter = listAdapter
        mViewBinding?.videoListView?.setOnItemClickListener { _, _, position, _ ->
            if (!isAutoPlay) {
                listPlayer.onItemClick(position)
            }
        }
        listPlayer = ListPlayer2(LitePlayerView(context!!)).apply {
            displayProgress(true)
            setProgressColor(R.color.colorAccent, R.color.colorPrimaryDark)
            attachOverlay(LoadingOverlay(context!!))
            attachMediaController(LiteMediaController(context!!))
            attachGestureController(LiteGestureController(context!!).apply {
                supportVolume = false
                supportBrightness = false
            })
            setRenderType(ConfigHolder.renderType)
            setPlayerType(ConfigHolder.playerType)
            supportHistory = true
            // sample to show and hide video cover
            // onDetachItemView always call before onAttachItemView
            listItemChangedListener = object : ListItemChangedListener {
                override fun onDetachItemView(oldPosition: Int) {
                    MediaLogger.e("detach item: $oldPosition")
                    listPlayerHolder?.let {
                        it.videoPoster.visibility = View.VISIBLE
                    }
                }

                override fun onAttachItemView(newPosition: Int) {
                    MediaLogger.e("attach item: $newPosition")
                    listPlayerHolder?.let {
                        it.videoPoster.visibility = View.INVISIBLE
                    }
                }
            }
        }
        val videoScrollListener = object : ListPlayer2.VideoListScrollListener {

            override fun getVideoContainer(childIndex: Int, position: Int): ViewGroup? {
                val itemView = mViewBinding?.videoListView?.getChildAt(childIndex)
                return if (itemView != null && itemView.tag != null) {
                    val videoHolder = itemView.tag as ListAdapter.VideoHolder
                    listPlayerHolder = videoHolder
                    videoHolder.videoContainer
                } else {
                    null
                }
            }

            override fun getVideoDataSource(position: Int): DataSource {
                return DataSource(VideoCacheHelper.url(listAdapter.getItem(position)))
            }
        }

        mViewBinding?.let { listPlayer.attachToListView(it.videoListView, false, videoScrollListener) }
        listPlayer.setAutoPlayMode(false)
        listPlayer.setRepeatMode(true)
        isAutoPlay = false
    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?
    ): FragmentCourseBinding=FragmentCourseBinding.inflate(inflater, viewGroup, false)

    override fun initViewModel(): DefaultViewModel=   ViewModelProvider(this).get(DefaultViewModel::class.java)

    inner class ListAdapter : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val itemView: View
            val holder: VideoHolder
            if (convertView == null) {
                itemView = LayoutInflater.from(parent!!.context).inflate(R.layout.item_view_video_list, parent, false)
                holder = VideoHolder()
                holder.videoTitle = itemView.findViewById(R.id.video_index)
                holder.videoPoster = itemView.findViewById(R.id.video_poster)
                holder.videoContainer = itemView.findViewById(R.id.video_container)
                itemView.tag = holder
            } else {
                itemView = convertView
                holder = convertView.tag as VideoHolder
            }
            holder.run {
                videoPoster.setBackgroundResource(R.drawable.timg)
                videoTitle.text = CourseViewModel.name[position]
            }
            return itemView
        }

        override fun getItem(position: Int) = CourseViewModel.urls[position]

        override fun getItemId(position: Int) = position.toLong()

        override fun getCount() = CourseViewModel.urls.size

        inner class VideoHolder {
            lateinit var videoTitle: TextView
            lateinit var videoPoster: ImageView
            lateinit var videoContainer: FrameLayout

        }

    }


    override fun onBackPressed() {
        if (listPlayer.isFullScreen()) {
            listPlayer.setFullScreenMode(false)
        } else {
            super.onBackPressed()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        listPlayer.pause(true)
    }

}
package com.css.wondercorefit.ui.fragment

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ToastUtils
import com.css.base.uibase.BaseFragment

import com.css.service.data.CourseData
import com.css.service.router.ARouterConst
import com.css.service.utils.SystemBarHelper
import com.css.wondercorefit.adapter.CourseRecycleAdapter
import com.css.wondercorefit.databinding.FragmentCourseBinding
import com.css.wondercorefit.viewmodel.CourseViewModel


class CourseFragment : BaseFragment<CourseViewModel, FragmentCourseBinding>(),
    NetworkUtils.OnNetworkStatusChangedListener {
    private val TAG = "CourseFragment"
    private var toast: Toast? = null
    var mData = ArrayList<CourseData>()
    lateinit var mAdapter: CourseRecycleAdapter
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(activity, 0f)
        SystemBarHelper.setHeightAndPadding(activity, mViewBinding?.topView)
        mAdapter = CourseRecycleAdapter(mData)
        mViewBinding?.courseRecycle?.layoutManager = GridLayoutManager(activity, 2)
        mViewBinding?.courseRecycle?.adapter = mAdapter
        mAdapter.setOnItemClickListener {
            playCourseVideo(it.videoLink)
        }
        if (NetworkUtils.isConnected()) {
            mViewBinding?.networkError?.visibility = View.GONE
            mViewBinding?.courseRecycle?.visibility = View.VISIBLE
        } else {
            mViewBinding?.networkError?.visibility = View.VISIBLE
            mViewBinding?.courseRecycle?.visibility = View.GONE
        }
        NetworkUtils.registerNetworkStatusChangedListener(this)
    }

    override fun initData() {
        super.initData()
        mViewModel.getCourseInfo()
    }

    override fun registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack()
        mViewModel.courseData.observe(viewLifecycleOwner, {
            mData.clear()
            mData.addAll(it)
            mAdapter.setItems(mData)
        })
    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?
    ): FragmentCourseBinding = FragmentCourseBinding.inflate(inflater, viewGroup, false)

    override fun initViewModel(): CourseViewModel =
        ViewModelProvider(this).get(CourseViewModel::class.java)

    private fun playCourseVideo(videoLink: String) {
        try {
            if (NetworkUtils.isConnected()) {
                ARouter.getInstance()
                    .build(ARouterConst.PATH_APP_MAIN_COURSE)
                    .with(Bundle().apply { putString("videoLink", videoLink) })
                    .navigation()
            } else {
                showNetworkErrorDialog();
            }
        } catch (e: ActivityNotFoundException) {
            ToastUtils.showShort("链接无效:${videoLink}")
        }
    }

    override fun onDisconnected() {
        mViewBinding?.networkError?.visibility = View.VISIBLE
        mViewBinding?.courseRecycle?.visibility = View.GONE
    }

    override fun onConnected(networkType: NetworkUtils.NetworkType?) {
        mViewBinding?.networkError?.visibility = View.GONE
        mViewBinding?.courseRecycle?.visibility = View.VISIBLE
        mViewModel.getCourseInfo()
    }
}
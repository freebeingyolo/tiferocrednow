package com.css.wondercorefit.ui.fragment

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ToastUtils
import com.css.base.uibase.BaseFragment

import com.css.service.data.CourseData
import com.css.service.router.ARouterConst
import com.css.service.utils.SystemBarHelper
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.FragmentCourseBinding
import com.css.wondercorefit.viewmodel.CourseViewModel


class CourseFragment : BaseFragment<CourseViewModel, FragmentCourseBinding>(), View.OnClickListener,
    NetworkUtils.OnNetworkStatusChangedListener {
    private val TAG = "CourseFragment"
    private var toast: Toast? = null
    var mData = ArrayList<CourseData>()
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(activity, 0f)
        SystemBarHelper.setHeightAndPadding(activity, mViewBinding?.topView)
        initClickListenr()
        if (NetworkUtils.isConnected()) {
            mViewBinding?.networkError?.visibility = View.GONE
            mViewBinding?.mainLayout?.visibility = View.VISIBLE
        } else {
            mViewBinding?.networkError?.visibility = View.VISIBLE
            mViewBinding?.mainLayout?.visibility = View.GONE
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
        })
    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?
    ): FragmentCourseBinding = FragmentCourseBinding.inflate(inflater, viewGroup, false)

    override fun initViewModel(): CourseViewModel =
        ViewModelProvider(this).get(CourseViewModel::class.java)

    private fun initClickListenr() {
        mViewBinding!!.ivVideo1.setOnClickListener(this)
        mViewBinding!!.ivVideo2.setOnClickListener(this)
        mViewBinding!!.ivVideo3.setOnClickListener(this)
        mViewBinding!!.ivVideo4.setOnClickListener(this)
        mViewBinding!!.ivVideo5.setOnClickListener(this)
        mViewBinding!!.ivVideo6.setOnClickListener(this)
        mViewBinding!!.ivVideo7.setOnClickListener(this)
        mViewBinding!!.ivVideo8.setOnClickListener(this)
        mViewBinding!!.ivVideo9.setOnClickListener(this)
        mViewBinding!!.ivVideo10.setOnClickListener(this)
        mViewBinding!!.ivVideo11.setOnClickListener(this)
        mViewBinding!!.ivVideo12.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_video1 -> {
                playCourseVideo("https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-4d20d503-b830-4567-b7f9-975957d322c9.mp4")
            }
            R.id.iv_video2 -> {
                playCourseVideo("")
            }
            R.id.iv_video3 -> {
                playCourseVideo("")
            }
            R.id.iv_video4 -> {
                playCourseVideo("")
            }
            R.id.iv_video5 -> {
                playCourseVideo("")
            }
            R.id.iv_video6 -> {
                playCourseVideo("")
            }
            R.id.iv_video7 -> {
                playCourseVideo("")
            }
            R.id.iv_video8 -> {
                playCourseVideo("")
            }
            R.id.iv_video9 -> {
                playCourseVideo("")
            }
            R.id.iv_video10 -> {
                playCourseVideo("")
            }
            R.id.iv_video11 -> {
                playCourseVideo("")
            }
            R.id.iv_video12 -> {
                playCourseVideo("")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NetworkUtils.unregisterNetworkStatusChangedListener(this)
    }

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
        mViewBinding?.mainLayout?.visibility = View.GONE
    }

    override fun onConnected(networkType: NetworkUtils.NetworkType?) {
        mViewBinding?.networkError?.visibility = View.GONE
        mViewBinding?.mainLayout?.visibility = View.VISIBLE
        mViewModel.getCourseInfo()
    }
}
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
        mViewBinding!!.ivBanner1.setOnClickListener(this)
        mViewBinding!!.ivBanner2.setOnClickListener(this)
        mViewBinding!!.ivBanner3.setOnClickListener(this)
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
            R.id.iv_banner1 -> {
                playCourseVideo("https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-4d20d503-b830-4567-b7f9-975957d322c9.mp4")
            }
            R.id.iv_banner2 -> {
                playCourseVideo("https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-52faf301-1575-4f8a-a3d9-c31cb5658515.mp4")
            }
            R.id.iv_banner3 -> {
                playCourseVideo("https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-016e0e6f-eb12-469a-8364-ea962d2b54c8.mp4")
            }
            R.id.iv_video1 -> {
                playCourseVideo("https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-adc4b5ee-44b8-4314-843b-530100b99ca2.mp4")
            }
            R.id.iv_video2 -> {
                playCourseVideo("https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-9bda6e85-625e-4a25-a8a5-bf591e2ad7ec.mp4")
            }
            R.id.iv_video3 -> {
                playCourseVideo("https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-eabe395c-9770-4636-afb6-8ce53df6b80c.mp4")
            }
            R.id.iv_video4 -> {
                playCourseVideo("https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-e9c5cd86-f42a-4042-8c9f-833bc6177bf5.mp4")
            }
            R.id.iv_video5 -> {
                playCourseVideo("https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-b9e7edce-34ce-4f4c-8c6c-fcb95fa0a05e.mp4")
            }
            R.id.iv_video6 -> {
                playCourseVideo("https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-a13c9004-d4a9-4d13-b8eb-c6b24bfa72f8.mp4")
            }
            R.id.iv_video7 -> {
                playCourseVideo("https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-52faf301-1575-4f8a-a3d9-c31cb5658515.mp4")
            }
            R.id.iv_video8 -> {
                playCourseVideo("https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-52faf301-1575-4f8a-a3d9-c31cb5658515.mp4")
            }
            R.id.iv_video9 -> {
                playCourseVideo("https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-187d5ceb-8de0-4e1c-9715-bdb6fdc91b76.mp4")
            }
            R.id.iv_video10 -> {
                playCourseVideo("https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-c627bc81-9310-462e-a2b1-d2d2e02977c2.mp4")
            }
            R.id.iv_video11 -> {
                playCourseVideo("https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-86d18c21-ca08-496d-b695-7a578818aba4.mp4")
            }
            R.id.iv_video12 -> {
                playCourseVideo("https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-a2fb479c-becf-492b-a6bf-497e70167c21.mp4")
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
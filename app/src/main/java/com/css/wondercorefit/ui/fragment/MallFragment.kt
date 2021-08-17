package com.css.wondercorefit.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.NetworkUtils
import com.css.base.uibase.BaseFragment
import com.css.service.data.MallData
import com.css.service.utils.SystemBarHelper
import com.css.wondercorefit.R
import com.css.wondercorefit.adapter.MallProductAdapter
import com.css.wondercorefit.adapter.MallStoreAdapter
import com.css.wondercorefit.databinding.FragmentMallBinding
import com.css.wondercorefit.viewmodel.MallViewModel

class MallFragment : BaseFragment<MallViewModel, FragmentMallBinding>(),
    NetworkUtils.OnNetworkStatusChangedListener {
    var mData = ArrayList<MallData>()
    var mStoreData = ArrayList<MallData>()
    lateinit var mAdapter: MallProductAdapter
    lateinit var mStoreAdapter: MallStoreAdapter
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(activity, 0f)
        SystemBarHelper.setHeightAndPadding(activity, mViewBinding?.topView)
        mAdapter = MallProductAdapter(mData)
        mStoreAdapter = MallStoreAdapter(mStoreData)
        mViewBinding?.productList?.layoutManager = GridLayoutManager(activity, 3)
        mViewBinding?.productList?.adapter = mAdapter
        var linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.isAutoMeasureEnabled = true;
        mViewBinding?.storeList?.layoutManager = LinearLayoutManager(activity)
        mViewBinding?.storeList?.adapter = mStoreAdapter
        mAdapter.setOnItemClickListener {
            try {
                openUrl(it.mallLink)
            } catch (e: Throwable) {
                showToast("暂无连接")
            }

        }
        mStoreAdapter.setOnItemClickListener {
            try {
                openUrl(it.mallLink)
            } catch (e: Throwable) {
                showToast("暂无连接")
            }
        }
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
        mViewModel.getMallInfo()
    }

    override fun registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack()
        mViewModel.mallData.observe(viewLifecycleOwner, {
            mData.clear()
            mStoreData.clear()
            for (item in it) {
                if (item.position == 1) {
                    mStoreData.add(item)
                }
                if (item.position == 2) {
                    mData.add(item)
                }

            }
            mAdapter.setItems(mData)
            mStoreAdapter.setItems(mStoreData)
        })
    }

    override fun initViewModel(): MallViewModel =
        ViewModelProvider(this).get(MallViewModel::class.java)

    override fun initViewBinding(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?
    ): FragmentMallBinding = FragmentMallBinding.inflate(inflater, viewGroup, false)


    private fun openUrl(url: String) {
        val uri: Uri = Uri.parse(url)
        val intent = Intent()
        intent.action = "android.intent.action.VIEW"
        intent.data = uri
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        NetworkUtils.unregisterNetworkStatusChangedListener(this)
    }

    override fun onDisconnected() {
        mViewBinding?.networkError?.visibility = View.VISIBLE
        mViewBinding?.mainLayout?.visibility = View.GONE
    }

    override fun onConnected(networkType: NetworkUtils.NetworkType?) {
        mViewBinding?.networkError?.visibility = View.GONE
        mViewBinding?.mainLayout?.visibility = View.VISIBLE
        mViewModel.getMallInfo()
    }
}
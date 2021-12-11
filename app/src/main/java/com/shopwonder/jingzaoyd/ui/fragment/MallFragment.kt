package com.shopwonder.jingzaoyd.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.css.base.uibase.BaseFragment
import com.css.service.data.MallData
import com.css.service.utils.SystemBarHelper
import com.shopwonder.jingzaoyd.R
import com.shopwonder.jingzaoyd.databinding.FragmentMallBinding
import com.shopwonder.jingzaoyd.viewmodel.MallViewModel

class MallFragment : BaseFragment<MallViewModel, FragmentMallBinding>(), View.OnClickListener,
    NetworkUtils.OnNetworkStatusChangedListener {
    var mData = ArrayList<MallData>()
    var mStoreData = ArrayList<MallData>()
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
        })
    }

    override fun initViewModel(): MallViewModel =
        ViewModelProvider(this).get(MallViewModel::class.java)

    override fun initViewBinding(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?
    ): FragmentMallBinding = FragmentMallBinding.inflate(inflater, viewGroup, false)

    private fun initClickListenr() {
        mViewBinding!!.layoutStore.setOnClickListener(this)
        mViewBinding!!.ivBanner.setOnClickListener(this)
        mViewBinding!!.ivProduct1.setOnClickListener(this)
        mViewBinding!!.ivProduct2.setOnClickListener(this)
        mViewBinding!!.ivProduct3.setOnClickListener(this)
        mViewBinding!!.ivProduct4.setOnClickListener(this)
        mViewBinding!!.ivProduct5.setOnClickListener(this)
        mViewBinding!!.ivProduct6.setOnClickListener(this)
        mViewBinding!!.ivProduct7.setOnClickListener(this)
        mViewBinding!!.ivProduct8.setOnClickListener(this)
        mViewBinding!!.ivProduct9.setOnClickListener(this)
        mViewBinding!!.ivProduct10.setOnClickListener(this)
        mViewBinding!!.ivProduct11.setOnClickListener(this)
        mViewBinding!!.ivProduct12.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.layout_store -> {
                openUrl("https://shop.m.jd.com/?shopId=1000096602")
            }
            R.id.iv_banner -> {
                openUrl("https://shop.m.jd.com/?shopId=1000096602")
            }
            R.id.iv_product1 -> {
                openUrl("https://item.m.jd.com/product/100016327406.html")
            }
            R.id.iv_product2 -> {
                openUrl("https://item.m.jd.com/product/100021255872.html")
            }
            R.id.iv_product3 -> {
                openUrl("https://item.m.jd.com/product/100020670306.html")
            }
            R.id.iv_product4 -> {
                openUrl("https://item.m.jd.com/product/100009543563.html")
            }
            R.id.iv_product5 -> {
                openUrl("https://item.m.jd.com/product/100007794845.html")
            }
            R.id.iv_product6 -> {
                openUrl("https://item.m.jd.com/product/100019547802.html")
            }
            R.id.iv_product7 -> {
                openUrl("https://item.m.jd.com/product/100021255850.html")
            }
            R.id.iv_product8 -> {
                openUrl("https://item.m.jd.com/product/100016414388.html")
            }
            R.id.iv_product9 -> {
                openUrl("https://item.m.jd.com/product/100020401214.html")
            }
            R.id.iv_product10 -> {
                openUrl("https://item.m.jd.com/product/100012143757.html")
            }
            R.id.iv_product11 -> {
                openUrl("https://item.m.jd.com/product/100020882540.html")
            }
            R.id.iv_product12 -> {
                openUrl("https://item.m.jd.com/product/100009271611.html")
            }
        }
    }


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
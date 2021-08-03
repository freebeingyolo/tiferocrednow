package com.css.wondercorefit.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.css.base.uibase.BaseFragment
import com.css.base.utils.NetworkChangeUtil
import com.css.base.utils.NetworkUtil
import com.css.service.data.MallData
import com.css.service.utils.SystemBarHelper
import com.css.wondercorefit.R
import com.css.wondercorefit.adapter.MallProductAdapter
import com.css.wondercorefit.databinding.FragmentMallBinding
import com.css.wondercorefit.viewmodel.MallViewModel

class MallFragment : BaseFragment<MallViewModel, FragmentMallBinding>(), View.OnClickListener {
    var mData = ArrayList<MallData>()
    lateinit var mAdapter: MallProductAdapter
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mViewBinding?.tvStoreDetails1?.setOnClickListener(this)
        mViewBinding?.tvStoreDetails2?.setOnClickListener(this)
        SystemBarHelper.immersiveStatusBar(activity, 0f)
        SystemBarHelper.setHeightAndPadding(activity, mViewBinding?.topView)
        mAdapter = MallProductAdapter(mData)
        mViewBinding?.productList?.layoutManager = GridLayoutManager(activity, 3)
        mViewBinding?.productList?.adapter = mAdapter
        mAdapter.setOnItemClickListener {
            openUrl(it.mallLink)
        }

        NetworkChangeUtil.getInstance(activity)
            .setNetchangeListener {
                if (!it) {
                    mViewBinding?.tvNetError?.visibility = View.VISIBLE
                } else {
                    mViewBinding?.tvNetError?.visibility = View.GONE
                }
            }
        NetworkChangeUtil.getInstance(activity).registerNetChangeReceiver()
    }

    override fun initData() {
        super.initData()
        mViewModel.getMallInfo()
//        mData.add(ProductBean(R.mipmap.icon_product_1, "计数单杠"))
//        mData.add(ProductBean(R.mipmap.icon_product_2, "计数俯卧撑板"))
//        mData.add(ProductBean(R.mipmap.icon_product_3, "计数健腹轮"))
////        mData.add(ProductBean(R.mipmap.icon_product_4, "计数跳绳"))
//        mData.add(ProductBean(R.mipmap.icon_product_5, "计数羽毛球拍"))
//        mData.add(ProductBean(R.mipmap.icon_product_6, "家用跑步机"))
////        mData.add(ProductBean(R.mipmap.icon_product_7, "腕力球"))
////        mData.add(ProductBean(R.mipmap.icon_product_8, "智能计数跳绳"))
//        mData.add(ProductBean(R.mipmap.icon_product_9, "智能体脂秤"))
//        mAdapter.setItems(mData)
    }

    override fun registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack()
        mViewModel.mallData.observe(viewLifecycleOwner, {
            mData.addAll(it)
            mAdapter.setItems(mData)
        })
    }

    override fun initViewModel(): MallViewModel =
        ViewModelProvider(this).get(MallViewModel::class.java)

    override fun initViewBinding(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?
    ): FragmentMallBinding = FragmentMallBinding.inflate(inflater, viewGroup, false)

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_store_details_1 -> {
                openUrl("https://www.tmall.com/")
            }
            R.id.tv_store_details_2 -> {
                openUrl("https://mall.jd.com/index-1000096602.html")
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
}
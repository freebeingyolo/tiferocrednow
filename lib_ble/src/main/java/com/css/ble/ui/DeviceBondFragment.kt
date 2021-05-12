package com.css.ble.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.css.base.uibase.BaseFragment
import com.css.ble.BR
import com.css.ble.bean.DeviceInfo
import com.css.ble.databinding.FragmentDeviceBondBinding
import com.css.ble.databinding.LayoutDeviceItemBinding
import com.css.ble.ui.view.SpaceItemDecoration
import com.css.ble.viewmodel.BoundViewModel

class DeviceBondFragment : BaseFragment<BoundViewModel, FragmentDeviceBondBinding>() {
    companion object {
        fun newInstance() = DeviceBondFragment()
        private val TAG: String? = "DeviceBondFragment"
    }

    lateinit var mAdapter: RecycleViewAdapter

    override fun initViewBinding(inflater: LayoutInflater, viewGroup: ViewGroup?): FragmentDeviceBondBinding {
        return FragmentDeviceBondBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun initViewModel(): BoundViewModel {
        return ViewModelProvider(requireActivity()).get(BoundViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewBinding!!.model = mViewModel
        mViewBinding!!.lifecycleOwner = viewLifecycleOwner
        mViewBinding?.lv!!.apply {
            mAdapter = RecycleViewAdapter()
            addItemDecoration(SpaceItemDecoration(30))
            layoutManager = LinearLayoutManager(requireContext())
            //(layoutManager as LinearLayoutManager).orientation = LinearLayoutManager.VERTICAL
            adapter = mAdapter
            //addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
        }
        mViewModel._deviceInfos.observe(viewLifecycleOwner, {
            Log.d(TAG, "mViewModel._deviceInfos：$it")
            mAdapter.mList = it
            mAdapter.notifyDataSetChanged()
        })
    }

    class RecycleViewAdapter : RecyclerView.Adapter<RecycleViewAdapter.MyViewHolder>() {
        var mList: List<DeviceInfo>? = null

        class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            var view = LayoutDeviceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false).root
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            var binding: LayoutDeviceItemBinding = DataBindingUtil.getBinding(holder.itemView)!!
            mList?.let {
                binding.name.text = it[position].name
                binding.icon.setImageResource(it[position].icon)
            }

        }

        override fun getItemCount(): Int {
            return mList?.size ?: 0
        }

    }
}
package com.css.ble.ui.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView


/**
 * 与dataBinding结合RecycleView的adapter
 * M: 数据类       B:layout文件对应的binding类
 * @author yuedong
 * @date 2021-05-13
 */
abstract class BaseBindingAdapter<M, B : ViewDataBinding> : RecyclerView.Adapter<BaseBindingAdapter.BaseViewHolder> {
    private var items: List<M>? = null

    constructor(items: List<M>?) : super() {
        this.items = items
    }

    fun setItems(items: List<M>?) {
        this.items = items
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseBindingAdapter.BaseViewHolder {
        val binding: B = DataBindingUtil.inflate(LayoutInflater.from(parent.context), getLayoutResId(viewType), parent, false)
        return BaseViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: BaseBindingAdapter.BaseViewHolder, position: Int) {
        val binding: B = DataBindingUtil.getBinding(holder.itemView)!!
        onBindItem(binding, items!![position], position)
    }

    @LayoutRes
    protected abstract fun getLayoutResId(viewType: Int): Int
    protected abstract fun onBindItem(binding: B, item: M, position: Int)

    class BaseViewHolder(v: View) : RecyclerView.ViewHolder(v) {

    }
}
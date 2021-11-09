package com.css.ble.ui.view

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.css.ble.ui.view.BaseRecyclerViewAdapter.ViewHolder
/**
 *@author baoyuedong
 *@time 2021-11-09 15:03
 *@description
 */
abstract class BaseRecyclerViewAdapter<M>(private var items: List<M>? = null) : RecyclerView.Adapter<ViewHolder>() {

    fun setItems(items: List<M>?) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val root: View = onCreateView(parent, viewType)
        return ViewHolder(root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        onBindItem(holder.itemView, items!![position], position)
    }

    //创建view
    protected abstract fun onCreateView(parent: ViewGroup, viewType: Int): View

    //设置view
    protected abstract fun onBindItem(itemView: View, item: M, position: Int)
    class ViewHolder(itemView : View): RecyclerView.ViewHolder(itemView)
}
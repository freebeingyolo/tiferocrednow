package com.css.wondercorefit.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.BaseExpandableListAdapter
import com.css.ble.ui.view.BaseBindingAdapter
import com.css.service.data.FeedbackData
import com.css.wondercorefit.bean.ProductBean
import com.css.wondercorefit.databinding.ItemProductLayoutBinding

/**
 * Created by YH
 * Describe ${描述}
 * on 2021/7/16.
 */
class FeedbackAdapter(mContext: Context) : BaseExpandableListAdapter() {

    private var mGroupData   = ArrayList<FeedbackData>()
    private var mChildData   = ArrayList<FeedbackData>()

    fun setGroupData(groupData : ArrayList<FeedbackData>) {
        mChildData = groupData
    }

    fun setChildData(childData : ArrayList<FeedbackData>) {
        mChildData = childData
    }

    override fun getGroupCount(): Int {
       return mGroupData.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
       return mChildData.size
    }

    override fun getGroup(groupPosition: Int): Any {

        return mGroupData[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        // TODO: 2021/7/23
        return mGroupData[groupPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
      return true
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        TODO("Not yet implemented")
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        TODO("Not yet implemented")
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
       return true
    }

}
package com.shopwonder.jingzaoyd.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import androidx.core.content.ContextCompat
import com.css.service.data.FeedbackData
import com.shopwonder.jingzaoyd.R
import com.shopwonder.jingzaoyd.databinding.ItemFeedbackChildBinding
import com.shopwonder.jingzaoyd.databinding.ItemFeedbackGroupBinding

/**
 * Created by YH
 * Describe 意见和反馈适配器
 * on 2021/7/16.
 */
class FeedbackAdapter(mContext: Context) : BaseExpandableListAdapter() {

    private var mGroupData = ArrayList<FeedbackData>()
    private var mChildData = HashMap<Int, ArrayList<FeedbackData>>()

    fun setGroupData(groupData: ArrayList<FeedbackData>) {
        mGroupData = groupData
        for (i in 0 until groupData.size) {
            mChildData[i] = ArrayList<FeedbackData>()
        }
    }

    fun setChildData(position: Int, childData: ArrayList<FeedbackData>) {
        mChildData[position] = childData
    }

    override fun getGroupCount(): Int {
        return mGroupData.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return mChildData[groupPosition]!!.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return mGroupData[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        // TODO: 2021/7/23 数据问题
        return mGroupData[childPosition]
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
    ): View? {
        val binding =
            ItemFeedbackGroupBinding.inflate(LayoutInflater.from(parent!!.context), parent, false)
        //数据
        val bean = mGroupData[groupPosition]
        binding.tvHistoryDate.text = bean.feedbackDate
        binding.tvHistoryStatus.text = bean.feedbackStatus
        //如果是展开状态，
        if (isExpanded) {
            binding.tvHistoryIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    parent!!.context, R.mipmap.icon_more
                )
            )
        } else {
            binding.tvHistoryIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    parent!!.context, R.mipmap.icon_next
                )
            )
        }
        return binding.root
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View? {
        val binding =
            ItemFeedbackChildBinding.inflate(LayoutInflater.from(parent!!.context), parent, false)
//        //数据
        if (mChildData[groupPosition]?.size!! > 0) {
            val bean = mChildData[groupPosition]?.get(childPosition) as FeedbackData
            binding.tvHistoryDate.text = bean.feedbackDate
            binding.tvHistoryStatus.text = bean.feedbackStatus
            binding.tvHistoryContent.text = bean.feedbackContent
        }
        return binding.root
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

}
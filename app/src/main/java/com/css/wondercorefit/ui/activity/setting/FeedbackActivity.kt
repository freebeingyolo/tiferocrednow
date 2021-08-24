package com.css.wondercorefit.ui.activity.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseActivity
import com.css.base.utils.DateTimeHelper
import com.css.service.data.FeedbackData
import com.css.wondercorefit.R
import com.css.wondercorefit.adapter.FeedbackAdapter
import com.css.wondercorefit.databinding.ActivityFeedbackBinding
import com.css.wondercorefit.viewmodel.FeedbackViewModel
import java.util.*

/**
 * Created by YH
 * Describe 意见反馈
 * on 2021/7/8.
 */
class FeedbackActivity : BaseActivity<FeedbackViewModel, ActivityFeedbackBinding>(),
    View.OnClickListener {

    //提交按钮是否可用，false不可用，true可用
    private var isSubmitStatus = false
    private var feedbackId = 0
    private var selectPosition = 0
    private var isRefresh = false

    private var feedbackData = ArrayList<FeedbackData>()

    lateinit var mAdapter: FeedbackAdapter

    companion object {
        fun starActivity(context: Context) {
            val intent = Intent(context, FeedbackActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun enabledVisibleToolBar(): Boolean {
        return true
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setToolBarLeftTitle("意见和反馈")
//        mViewBinding.tvErrorData.setOnClickListener(this)
//        mViewBinding.tvErrorTime.setOnClickListener(this)
        mViewBinding.etContent.setOnClickListener(this)
        mViewBinding.rtSubmit.setOnClickListener(this)

        initListener()
    }

    override fun initData() {
        super.initData()
        //初始化日期数据
        setFeedbackDate(Calendar.getInstance().time)
//        try {
//            startDate.time = DateTimeHelper.parseStringToDate("1970-01-01")
//        } catch (e: ParseException) {
//            e.printStackTrace()
//        }
        mAdapter = FeedbackAdapter(this)
        mViewBinding.eListviewFeedback.setAdapter(mAdapter)
        mViewBinding.eListviewFeedback.setOnGroupClickListener { parent, v, groupPosition, id ->
//            LogUtils.dTag("---", "eListviewFeedback:" + groupPosition)
            isRefresh = true
            if (parent.isGroupExpanded(groupPosition)) {
                //收起删除缓存
                feedbackId = 0
                selectPosition = 0
                setFeedbackDate(Calendar.getInstance().time)
            } else {
                //展开获取反馈详情数据
//                var bean =  parent.adapter.getItem(groupPosition) as FeedbackData
                val bean = feedbackData[groupPosition]
//                LogUtils.dTag("---", ":" + bean.id)
                feedbackId = bean.id
                selectPosition = groupPosition
                mViewModel.queryFeedBackHistoryDetail(bean.id)
                setFeedbackDate(DateTimeHelper.parseStringToDate(bean.feedbackDate))
            }
//            LogUtils.dTag("---", "feedbackId:" + feedbackId)
            false
        }

        mViewBinding.eListviewFeedback.setOnGroupExpandListener {
//            LogUtils.dTag("---", ":" + it)
            if (isRefresh) {
                for (i in feedbackData.indices) {
                    if (i != it) {
                        mViewBinding.eListviewFeedback.collapseGroup(i)
                    }
                }
            }
        }

        //加载历史反馈数据
        mViewModel.queryFeedBackHistory()
    }

    override fun initViewModel(): FeedbackViewModel =
        ViewModelProvider(this).get(FeedbackViewModel::class.java)

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityFeedbackBinding = ActivityFeedbackBinding.inflate(layoutInflater, parent, false)

    override fun onClick(v: View?) {
        when (v) {
//            mViewBinding.tvErrorData -> {
//                //选择日期
//                showSelectedData()
//            }
//            mViewBinding.tvErrorTime -> {
//                //选择时间
//                showSelectedTime()
//            }
            mViewBinding.rtSubmit -> {
                //提交
                mViewModel.doSubmit(
                    isSubmitStatus,
                    feedbackId,
                    mViewBinding.etPhone.text.toString(),
                    mViewBinding.etContent.text.toString(),
                );
            }

        }
    }

    override fun registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack()
        mViewModel.submitData.observe(this, {
            showToast("提交成功")
            mViewBinding.etPhone.setText("")
            mViewBinding.etContent.setText("")
            setFeedbackDate(Calendar.getInstance().time);
            mViewModel.queryFeedBackHistory()
        })

        mViewModel.historyData.observe(this, {
            //意见反馈历史数据
            feedbackData = it
            isRefresh = false;
            mAdapter.setGroupData(feedbackData)
            mAdapter.notifyDataSetChanged()
            for (i in feedbackData.indices) {
                mViewBinding.eListviewFeedback.collapseGroup(i)
//                LogUtils.dTag("---", "expandGroup:" + i)
            }
        })

        mViewModel.historyDetails.observe(this, {
            //意见反馈历史数据详情
            mAdapter.setChildData(selectPosition, it)
            mAdapter.notifyDataSetChanged()
        })

    }


    /**
     * 设置意见反馈出现问题时间，默认为当前时间，选中反馈记录时候，填充反馈记录的出问题时间，该字段不需要提交
     */
    private fun setFeedbackDate(date: Date) {
        mViewBinding.tvErrorData.text = DateTimeHelper.formatToString(date, "yyyy-MM-dd")
        mViewBinding.tvErrorTime.text = DateTimeHelper.formatToString(date, "HH:mm")
    }

    private fun initListener() {
        //输入手机号
        mViewBinding.etPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                checkSubmitStatus()
            }
        })
        //输入反馈内容
        mViewBinding.etContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val maxLength = 200
                if (s.length == maxLength) {
                    mViewBinding.tvCurrentLength.setTextColor(resources.getColor(R.color.colorAccent))
                } else {
                    mViewBinding.tvCurrentLength.setTextColor(resources.getColor(R.color.text_AAAAAA))
                }
                mViewBinding.tvCurrentLength.text = s.length.toString()
                val maxLengthStr = "/$maxLength"
                mViewBinding.tvMaxLength.text = maxLengthStr
            }

            override fun afterTextChanged(s: Editable?) {
                checkSubmitStatus()
            }
        })
    }

    /**
     * 必要字段填充完才可点击提交
     */
    private fun checkSubmitStatus() {
        //手机、内容必填之后，按钮才高亮可点击
        if (!TextUtils.isEmpty(mViewBinding.etPhone.text) && !TextUtils.isEmpty(mViewBinding.etContent.text)
//        日期、时间、 && !TextUtils.isEmpty(mViewBinding.tvErrorData.text) && !TextUtils.isEmpty(mViewBinding.tvErrorTime.text)
        ) {
            isSubmitStatus = true
            mViewBinding.rtSubmit.setTextColor(resources.getColor(R.color.white))
            mViewBinding.rtSubmit.setBackgroundColor(resources.getColor(R.color.colorAccent))
        } else {
            isSubmitStatus = false
            mViewBinding.rtSubmit.setTextColor(resources.getColor(R.color.black))
            mViewBinding.rtSubmit.setBackgroundColor(resources.getColor(R.color.color_e5e5e5))
        }
    }

/* 日期与时间不可选
    private var mDataPickerDialog: TimePickerView? = null
    private var mTimePickerDialog: TimePickerView? = null
    //默认选中日期
    private val selectedDate = Calendar.getInstance()

    //设置最小日期和最大日期
    private val startDate = Calendar.getInstance()

    //最大日期是今天
    private val endDate = Calendar.getInstance()

    private fun showSelectedData() {
        mDataPickerDialog = TimePickerBuilder(
            this
        ) { date, v ->
            // 这里回调过来的v,就是show()方法里面所添加的 View 参数，如果show的时候没有添加参数，v则为null
            mViewBinding.tvErrorData.text = DateTimeHelper.formatToString(date, "yyyy-MM-dd")
            checkSubmitStatus()
        }
//         .setDecorView((RelativeLayout) findViewById(R.id.ll_pickerView))//必须是RelativeLayout，不设置setDecorView的话，底部虚拟导航栏会显示在弹出的选择器区域
            //年月日时分秒 的显示与否，不设置则默认全部显示
            .setType(booleanArrayOf(true, true, true, false, false, false))
            .setLabel("年", "月", "日", "", "", "")
            .isCenterLabel(false)//是否只显示中间选中项的label文字，false则每项item全部都带有label。
            .setTitleText("选择日期")//标题文字
            .setTitleSize(18)//标题文字大小
            .setTitleColor(resources.getColor(R.color.black))//标题文字颜色
            .setCancelText("取消")//取消按钮文字
            .setCancelColor(resources.getColor(R.color.black))//取消按钮文字颜色
            .setSubmitText("确定")//确认按钮文字
            .setSubmitColor(resources.getColor(R.color.black))//确定按钮文字颜色
            .setContentTextSize(16)//滚轮文字大小
            .setTextColorCenter(resources.getColor(R.color.colorAccent))//设置选中文本的颜色值
            .setLineSpacingMultiplier(1.6f)//行间距
            .setDividerColor(resources.getColor(R.color.text_AAAAAA))//设置分割线的颜色
            .setRangDate(startDate, endDate)//设置最小和最大日期
            .setDate(selectedDate)//设置选中的日期
            .setOutSideCancelable(true)//点击外部dismiss default true
            .isDialog(true)//是否显示为对话框样式
            .build()
        val lp: FrameLayout.LayoutParams =
            mDataPickerDialog!!.dialogContainerLayout.layoutParams as FrameLayout.LayoutParams
        lp.leftMargin = 0
        lp.rightMargin = 0
        mDataPickerDialog!!.dialog.window?.setGravity(Gravity.BOTTOM)
        mDataPickerDialog!!.dialog.window?.setWindowAnimations(R.style.picker_view_slide_anim)
        mDataPickerDialog?.show()

    }

    private fun showSelectedTime() {
        if (TextUtils.isEmpty(mViewBinding.tvErrorData.text)) {
            showCenterToast("请先选择日期")
            return
        }
        mTimePickerDialog = TimePickerBuilder(
            this
        ) { date, v ->
            // 这里回调过来的v,就是show()方法里面所添加的 View 参数，如果show的时候没有添加参数，v则为null
            mViewBinding.tvErrorTime.text = DateTimeHelper.formatToString(date, "HH:mm")
            checkSubmitStatus()
        }
//         .setDecorView((RelativeLayout) findViewById(R.id.ll_pickerView))//必须是RelativeLayout，不设置setDecorView的话，底部虚拟导航栏会显示在弹出的选择器区域
            //年月日时分秒 的显示与否，不设置则默认全部显示
            .setType(booleanArrayOf(false, false, false, true, true, false))
            .setLabel("", "", "", "", "", "")
            .isCenterLabel(false)//是否只显示中间选中项的label文字，false则每项item全部都带有label。
            .setTitleText("选择日期")//标题文字
            .setTitleSize(18)//标题文字大小
            .setTitleColor(resources.getColor(R.color.black))//标题文字颜色
            .setCancelText("取消")//取消按钮文字
            .setCancelColor(resources.getColor(R.color.black))//取消按钮文字颜色
            .setSubmitText("确定")//确认按钮文字
            .setSubmitColor(resources.getColor(R.color.black))//确定按钮文字颜色
            .setContentTextSize(16)//滚轮文字大小
            .setTextColorCenter(resources.getColor(R.color.colorAccent))//设置选中文本的颜色值
            .setLineSpacingMultiplier(1.6f)//行间距
            .setDividerColor(resources.getColor(R.color.text_AAAAAA))//设置分割线的颜色
            .setRangDate(startDate, endDate)//设置最小和最大日期
            .setDate(selectedDate)//设置选中的日期
            .setOutSideCancelable(true)//点击外部dismiss default true
            .isDialog(true)//是否显示为对话框样式
            .build()
        val lp: FrameLayout.LayoutParams =
            mDataPickerDialog!!.dialogContainerLayout.layoutParams as FrameLayout.LayoutParams
        lp.leftMargin = 0
        lp.rightMargin = 0
        mTimePickerDialog!!.dialog.window?.setGravity(Gravity.BOTTOM)
        mTimePickerDialog!!.dialog.window?.setWindowAnimations(R.style.picker_view_slide_anim)
        mTimePickerDialog?.show()

    }*/

}



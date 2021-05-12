package com.css.wondercorefit.ui.activity.setting

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider

import com.css.base.uibase.BaseActivity
import com.css.base.uibase.inner.OnToolBarClickListener
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.css.base.view.ToolBarView
import com.css.pickerview.builder.OptionsPickerBuilder
import com.css.pickerview.listener.CustomListener
import com.css.pickerview.view.OptionsPickerView
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.ActivityPersonInformationBinding

class PersonInformationActivity :
    BaseActivity<DefaultViewModel, ActivityPersonInformationBinding>(), OnToolBarClickListener,
    View.OnClickListener {
    var mSexPickerDialog: OptionsPickerView<String>? = null
    var mAgePickerDialog: OptionsPickerView<String>? = null
    var mStaturePickerDialog: OptionsPickerView<String>? = null
    var mSexList = ArrayList<String>()
    var mAgeList = ArrayList<String>()
    var mStatureList = ArrayList<String>()

    companion object {
        fun starActivity(context: Context) {
            val intent = Intent(context, PersonInformationActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setWhiteFakeStatus(R.id.ll_parent, false)
        mViewBinding.toolBarView.setCenterText("个人信息")
        mViewBinding.toolBarView.setToolBarClickListener(this)
        mViewBinding.rlSex.setOnClickListener(this)
        mViewBinding.rlAge.setOnClickListener(this)
        mViewBinding.rlStature.setOnClickListener(this)
    }

    override fun initViewModel(): DefaultViewModel =
        ViewModelProvider(this).get(DefaultViewModel::class.java)

    override fun initViewBinding(): ActivityPersonInformationBinding =
        ActivityPersonInformationBinding.inflate(layoutInflater)

    override fun onClickToolBarView(view: View, event: ToolBarView.ViewType) {
        when (event) {
            //支持默认返回按钮和事件
            ToolBarView.ViewType.LEFT_IMAGE -> {
                finishAc()
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.rl_sex -> showSexDialog()
            R.id.rl_age -> showAgeDialog()
            R.id.rl_stature -> showStatureDialog()
        }
    }

    private fun showSexDialog() {
        if (mSexPickerDialog == null) {
            mSexList.add("男")
            mSexList.add("女")
            mSexPickerDialog = OptionsPickerBuilder(
                this
            ) { options1, options2, options3, v ->
                var str = mSexList[options1]
                mViewBinding.tvSex.text = str

            }.setLayoutRes(R.layout.dialog_person_info_setting, object : CustomListener {
                override fun customLayout(v: View?) {
                    var title = v?.findViewById<TextView>(R.id.tv_title)
                    var cancel = v?.findViewById<TextView>(R.id.btn_cancel)
                    var submit = v?.findViewById<TextView>(R.id.btn_submit)
                    title?.text = "性别"
                    cancel?.setOnClickListener {
                        mSexPickerDialog?.dismiss()
                    }
                    submit?.setOnClickListener {
                        mSexPickerDialog?.returnData()
                        mSexPickerDialog?.dismiss()
                    }
                }

            })
                .setSelectOptions(1)  //设置默认选中项
                .setOutSideCancelable(true)//点击外部dismiss default true
                .isDialog(false)//是否显示为对话框样式
                .build()
            mSexPickerDialog?.setPicker(mSexList)
            mSexPickerDialog?.show()
        } else {
            mSexPickerDialog?.show()
        }
    }

    private fun showAgeDialog() {
        if (mAgePickerDialog == null) {
            for (index in 100 downTo 1) {
                mAgeList.add(index.toString())
            }
            mAgePickerDialog = OptionsPickerBuilder(
                this
            ) { options1, options2, options3, v ->
                var str = mAgeList[options1]
                mViewBinding.tvAge.text = str

            }.setLayoutRes(R.layout.dialog_person_info_setting, object : CustomListener {
                override fun customLayout(v: View?) {
                    var title = v?.findViewById<TextView>(R.id.tv_title)
                    var cancel = v?.findViewById<TextView>(R.id.btn_cancel)
                    var submit = v?.findViewById<TextView>(R.id.btn_submit)
                    title?.text = "年龄"
                    cancel?.setOnClickListener {
                        mAgePickerDialog?.dismiss()
                    }
                    submit?.setOnClickListener {
                        mAgePickerDialog?.returnData()
                        mAgePickerDialog?.dismiss()
                    }
                }

            }).setLabels("岁", "", "")
                .isCenterLabel(true)
                .setSelectOptions(18)
                .setLineSpacingMultiplier(3.0F)
                .setTextColorCenter(Color.parseColor("#F2682A"))
                .setOutSideCancelable(true)//点击外部dismiss default true
                .isDialog(false)//是否显示为对话框样式
                .build()
            mAgePickerDialog?.setPicker(mAgeList)
            mAgePickerDialog?.show()
        } else {
            mAgePickerDialog?.show()
        }
    }

    private fun showStatureDialog() {
        if (mStaturePickerDialog == null) {
            for (index in 250 downTo 100) {
                mStatureList.add(index.toString())
            }
            mStaturePickerDialog = OptionsPickerBuilder(
                this
            ) { options1, options2, options3, v ->
                var str = mStatureList[options1]
                mViewBinding.tvStature.text = str

            }.setLayoutRes(R.layout.dialog_person_info_setting, object : CustomListener {
                override fun customLayout(v: View?) {
                    var title = v?.findViewById<TextView>(R.id.tv_title)
                    var cancel = v?.findViewById<TextView>(R.id.btn_cancel)
                    var submit = v?.findViewById<TextView>(R.id.btn_submit)
                    title?.text = "身高"
                    cancel?.setOnClickListener {
                        mStaturePickerDialog?.dismiss()
                    }
                    submit?.setOnClickListener {
                        mStaturePickerDialog?.returnData()
                        mStaturePickerDialog?.dismiss()
                    }
                }

            }).setLabels("cm", "", "")
                .isCenterLabel(true)
                .setSelectOptions(90)
                .setLineSpacingMultiplier(3.0F)
                .setTextColorCenter(Color.parseColor("#F2682A"))
                .setOutSideCancelable(true)//点击外部dismiss default true
                .isDialog(false)//是否显示为对话框样式
                .build()
            mStaturePickerDialog?.setPicker(mStatureList)
            mStaturePickerDialog?.show()
        } else {
            mStaturePickerDialog?.show()
        }
    }
}
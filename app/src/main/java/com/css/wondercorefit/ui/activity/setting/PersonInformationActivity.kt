package com.css.wondercorefit.ui.activity.setting

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.bigkoo.pickerview.view.OptionsPickerView
import com.css.base.uibase.BaseActivity
import com.css.base.uibase.inner.OnToolBarClickListener
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.css.base.view.ToolBarView
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.ActivityPersonInformationBinding

class PersonInformationActivity :
    BaseActivity<DefaultViewModel, ActivityPersonInformationBinding>(), OnToolBarClickListener,
    View.OnClickListener {
    lateinit var mOptionPickerDialog: OptionsPickerView<String>
    var mSexList = ArrayList<String>()

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
        optionData()
    }

    private fun optionData() {
        mSexList.add("男")
        mSexList.add("女")
        showDialog()
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
            R.id.rl_sex -> {
                mOptionPickerDialog.show()
            }
        }
    }

    private fun showDialog() {
        mOptionPickerDialog = OptionsPickerBuilder(
            this
        ) { options1, options2, options3, v ->
            var str = mSexList[options1]
            mViewBinding.tvSex.text = str

        }.setSubmitText("确定")//确定按钮文字
            .setCancelText("取消")//取消按钮文字
            .setTitleText("性别")//标题
            .setSubCalSize(18)//确定和取消文字大小
            .setTitleSize(20)//标题文字大小
            .setTitleColor(Color.BLACK)//标题文字颜色
            .setSubmitColor(Color.BLUE)//确定按钮文字颜色
            .setCancelColor(Color.BLUE)//取消按钮文字颜色
            .setTitleBgColor(Color.WHITE)//标题背景颜色 Night mode
            .setBgColor(Color.WHITE)//滚轮背景颜色 Night mode
            .setContentTextSize(18)//滚轮文字大小
            .setSelectOptions(1)  //设置默认选中项
            .setOutSideCancelable(false)//点击外部dismiss default true
            .isDialog(false)//是否显示为对话框样式
            .build()
        mOptionPickerDialog.setPicker(mSexList)
    }
}
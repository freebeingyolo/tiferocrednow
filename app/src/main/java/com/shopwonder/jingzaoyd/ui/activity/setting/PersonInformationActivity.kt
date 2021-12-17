package com.shopwonder.jingzaoyd.ui.activity.setting

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseActivity
import com.css.pickerview.builder.OptionsPickerBuilder
import com.css.pickerview.listener.CustomListener
import com.css.pickerview.view.OptionsPickerView
import com.css.service.data.UserData
import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache
import com.shopwonder.jingzaoyd.R
import com.shopwonder.jingzaoyd.databinding.ActivityPersonInformationBinding
import com.shopwonder.jingzaoyd.viewmodel.PersonInformationViewModel

class PersonInformationActivity : BaseActivity<PersonInformationViewModel, ActivityPersonInformationBinding>(),
    View.OnClickListener {
    var mSexPickerDialog: OptionsPickerView<String>? = null
    var mAgePickerDialog: OptionsPickerView<String>? = null
    var mStaturePickerDialog: OptionsPickerView<String>? = null
    var mTargetWeightPickerDialog: OptionsPickerView<String>? = null
    var mTargetStepPickerDialog: OptionsPickerView<String>? = null
    val mSexList by lazy { arrayOf("男", "女").toList() }
    val mAgeList by lazy { (100 downTo 1).map { it.toString() } }
    val mStatureList by lazy { (250 downTo 100).map { it.toString() } }
    val mTargetWeightList by lazy { (150 downTo 30).map { it.toString() } }
    val mTargetStepList by lazy { (100000 downTo 1000 step 1000).map { it.toString() } }


    companion object {
        fun starActivity(context: Context) {
            val intent = Intent(context, PersonInformationActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun enabledVisibleToolBar(): Boolean {
        return true
    }

    override fun registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack()
        WonderCoreCache.getLiveData<UserData>(CacheKey.USER_INFO).observe(this) { userData ->
            hideLoading()
            if (userData == null) return@observe
            mViewBinding.tvTargetWeight.text = "${userData.goalBodyWeight}kg"
            mViewBinding.tvTargetStep.text = "${userData.goalStepCount}步"
            mViewBinding.tvStature.text = "${userData.height}cm"
            mViewBinding.tvAge.text = "${userData.age}岁"
            mViewBinding.tvSex.text = userData.sex
        }
        mViewModel.upPersonInfoData.observe(this, {
            hideLoading()
            showCenterToast(it)
        })
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setToolBarLeftTitle("个人信息")
        mViewBinding.rlSex.setOnClickListener(this)
        mViewBinding.rlAge.setOnClickListener(this)
        mViewBinding.rlStature.setOnClickListener(this)
        mViewBinding.rlTargetWeight.setOnClickListener(this)
        mViewBinding.rlTargetStep.setOnClickListener(this)
    }

    override fun initData() {
        super.initData()
        mViewModel.getPersonInfo()
    }

    override fun initViewModel(): PersonInformationViewModel =
        ViewModelProvider(this).get(PersonInformationViewModel::class.java)

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityPersonInformationBinding =
        ActivityPersonInformationBinding.inflate(layoutInflater, parent, false)

    override fun onClick(v: View) {
        when (v.id) {
            R.id.rl_sex -> showSexDialog()
            R.id.rl_age -> showAgeDialog()
            R.id.rl_stature -> showStatureDialog()
            R.id.rl_target_weight -> showTargetWeightDialog()
            R.id.rl_target_step -> showTargetStepDialog()
        }
    }

    private fun showSexDialog() {
        mSexPickerDialog = OptionsPickerBuilder(
            this
        ) { options1, options2, options3, v ->
            var str = mSexList[options1]
            mViewBinding.tvSex.text = str
            val mUserData = WonderCoreCache.getUserInfo()
            mUserData.sex = str
            WonderCoreCache.saveUserInfo(mUserData)
            mViewModel.upDataPersonInfo(sex = str)

        }.setLayoutRes(R.layout.dialog_person_info_setting) { v ->
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
            .setSelectOptions(mSexList.indexOf(WonderCoreCache.getUserInfo().sex))  //设置默认选中项
            .setOutSideCancelable(true)//点击外部dismiss default true
            .setTextColorCenter(R.color.color_e1251b)
            .isDialog(true)//是否显示为对话框样式
            .build()
        mSexPickerDialog?.setPicker(mSexList)
        val lp: FrameLayout.LayoutParams =
            mSexPickerDialog!!.dialogContainerLayout.layoutParams as FrameLayout.LayoutParams
        lp.leftMargin = 0
        lp.rightMargin = 0
        mSexPickerDialog!!.dialog.window?.setGravity(Gravity.BOTTOM)
        mSexPickerDialog!!.dialog.window?.setWindowAnimations(R.style.picker_view_slide_anim)
        mSexPickerDialog?.show()

    }

    private fun showAgeDialog() {
        mAgePickerDialog = OptionsPickerBuilder(
            this
        ) { options1, options2, options3, v ->
            val str = mAgeList[options1]
            mViewBinding.tvAge.text = str + "岁"
            val mUserData = WonderCoreCache.getUserInfo()
            mUserData.age = str
            WonderCoreCache.saveUserInfo(mUserData)
            mViewModel.upDataPersonInfo(age = str)

        }.setLayoutRes(
            R.layout.dialog_person_info_setting
        ) { v ->
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
        }.setLabels("岁", "", "")
            .isCenterLabel(true)
            .setSelectOptions(mAgeList.indexOf(WonderCoreCache.getUserInfo().age))
            .setLineSpacingMultiplier(3.0F)
            .setTextColorCenter(R.color.color_e1251b)
            .setOutSideCancelable(true)//点击外部dismiss default true
            .isDialog(true)//是否显示为对话框样式
            .build()
        mAgePickerDialog?.setPicker(mAgeList)
        val lp: FrameLayout.LayoutParams =
            mAgePickerDialog!!.dialogContainerLayout.layoutParams as FrameLayout.LayoutParams
        lp.leftMargin = 0
        lp.rightMargin = 0
        mAgePickerDialog!!.dialog.window?.setGravity(Gravity.BOTTOM)
        mAgePickerDialog!!.dialog.window?.setWindowAnimations(R.style.picker_view_slide_anim)
        mAgePickerDialog?.show()

    }

    private fun showStatureDialog() {
        mStaturePickerDialog = OptionsPickerBuilder(
            this
        ) { options1, options2, options3, v ->
            var str = mStatureList[options1]
            mViewBinding.tvStature.text = str + "cm"
            val mUserData = WonderCoreCache.getUserInfo()
            mUserData.height = str
            WonderCoreCache.saveUserInfo(mUserData)
            mViewModel.upDataPersonInfo(height = str)

        }.setLayoutRes(
            R.layout.dialog_person_info_setting
        ) { v ->
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
        }.setLabels("cm", "", "")
            .isCenterLabel(true)
            .setSelectOptions(mStatureList.indexOf(WonderCoreCache.getUserInfo().height))
            .setLineSpacingMultiplier(3.0F)
            .setTextColorCenter(R.color.color_e1251b)
            .setOutSideCancelable(true)//点击外部dismiss default true
            .isDialog(true)//是否显示为对话框样式
            .build()
        mStaturePickerDialog?.setPicker(mStatureList)
        val lp: FrameLayout.LayoutParams =
            mStaturePickerDialog!!.dialogContainerLayout.layoutParams as FrameLayout.LayoutParams
        lp.leftMargin = 0
        lp.rightMargin = 0
        mStaturePickerDialog!!.dialog.window?.setGravity(Gravity.BOTTOM)
        mStaturePickerDialog!!.dialog.window?.setWindowAnimations(R.style.picker_view_slide_anim)
        mStaturePickerDialog?.show()

    }

    private fun showTargetWeightDialog() {
        mTargetWeightPickerDialog = OptionsPickerBuilder(
            this
        ) { options1, options2, options3, v ->
            var str = mTargetWeightList[options1]
            mViewBinding.tvTargetWeight.text = str + "kg"
            val mUserData = WonderCoreCache.getUserInfo()
            mUserData.goalBodyWeight = str
            WonderCoreCache.saveUserInfo(mUserData)
            mViewModel.upDataPersonInfo(goalBodyWeight = str)
        }.setLayoutRes(
            R.layout.dialog_person_info_setting
        ) { v ->
            var title = v?.findViewById<TextView>(R.id.tv_title)
            var cancel = v?.findViewById<TextView>(R.id.btn_cancel)
            var submit = v?.findViewById<TextView>(R.id.btn_submit)
            title?.text = "目标体重"
            cancel?.setOnClickListener {
                mTargetWeightPickerDialog?.dismiss()
            }
            submit?.setOnClickListener {
                mTargetWeightPickerDialog?.returnData()
                mTargetWeightPickerDialog?.dismiss()
            }
        }.setLabels("kg", "", "")
            .isCenterLabel(true)
            .setSelectOptions(mTargetWeightList.indexOf(WonderCoreCache.getUserInfo().goalBodyWeight))
            .setLineSpacingMultiplier(3.0F)
            .setTextColorCenter(R.color.color_e1251b)
            .setOutSideCancelable(true)//点击外部dismiss default true
            .isDialog(true)//是否显示为对话框样式
            .build()
        mTargetWeightPickerDialog?.setPicker(mTargetWeightList)
        val lp: FrameLayout.LayoutParams =
            mTargetWeightPickerDialog!!.dialogContainerLayout.layoutParams as FrameLayout.LayoutParams
        lp.leftMargin = 0
        lp.rightMargin = 0
        mTargetWeightPickerDialog!!.dialog.window?.setGravity(Gravity.BOTTOM)
        mTargetWeightPickerDialog!!.dialog.window?.setWindowAnimations(R.style.picker_view_slide_anim)
        mTargetWeightPickerDialog?.show()

    }

    private fun showTargetStepDialog() {
        mTargetStepPickerDialog = OptionsPickerBuilder(
            this
        ) { options1, options2, options3, v ->
            var str = mTargetStepList[options1]
            mViewBinding.tvTargetStep.text = str + "步"
            val mUserData = WonderCoreCache.getUserInfo()
            mUserData.goalStepCount = str
            WonderCoreCache.saveUserInfo(mUserData)
            mViewModel.upDataPersonInfo(goalStepCount = str)
        }.setLayoutRes(R.layout.dialog_person_info_setting, object : CustomListener {
            override fun customLayout(v: View?) {
                var title = v?.findViewById<TextView>(R.id.tv_title)
                var cancel = v?.findViewById<TextView>(R.id.btn_cancel)
                var submit = v?.findViewById<TextView>(R.id.btn_submit)
                title?.text = "目标步数"
                cancel?.setOnClickListener {
                    mTargetStepPickerDialog?.dismiss()
                }
                submit?.setOnClickListener {
                    mTargetStepPickerDialog?.returnData()
                    mTargetStepPickerDialog?.dismiss()
                }
            }

        }).setLabels("步", "", "")
            .isCenterLabel(true)
            .setSelectOptions(mTargetStepList.indexOf(WonderCoreCache.getUserInfo().goalStepCount))
            .setLineSpacingMultiplier(3.0F)
            .setTextColorCenter(R.color.color_e1251b)
            .setOutSideCancelable(true)//点击外部dismiss default true
            .isDialog(true)//是否显示为对话框样式
            .build()
        mTargetStepPickerDialog?.setPicker(mTargetStepList)
        val lp: FrameLayout.LayoutParams =
            mTargetStepPickerDialog!!.dialogContainerLayout.layoutParams as FrameLayout.LayoutParams
        lp.leftMargin = 0
        lp.rightMargin = 0
        mTargetStepPickerDialog!!.dialog.window?.setGravity(Gravity.BOTTOM)
        mTargetStepPickerDialog!!.dialog.window?.setWindowAnimations(R.style.picker_view_slide_anim)
        mTargetStepPickerDialog?.show()

    }


}
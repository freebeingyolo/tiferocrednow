package com.css.wondercorefit.viewmodel

import androidx.lifecycle.MutableLiveData
import com.css.base.net.api.repository.SettingRepository
import com.css.base.uibase.viewmodel.BaseViewModel

/**
 * Created by YH
 * Describe 意见反馈 VM
 * on 2021/7/8.
 */
class FeedbackViewModel : BaseViewModel() {
    val submitData = MutableLiveData<String>()

    fun doSubmit(
        isShowSubmit: Boolean,
        content: String,
        data: String,
        time: String,
        phone: String,
        userId: String
    ) {
        if (isShowSubmit) {
            //可以提交
            //检查电话是否合规
            if (phone.length != 11) {
                showCenterToast("请输入正确的手机号码")
            } else {
//                netLaunch(
//                    {
//                        showLoading()
//                        SettingRepository.submit(content, data, time, phone, userId)
//                    }, { msg, _ ->
//                        hideLoading()
//                        submitData.value = msg
//                    }, { _, msg, _ ->
//                        hideLoading()
//                        showToast(msg)
//                    }
//                )
            }
        }
    }


}
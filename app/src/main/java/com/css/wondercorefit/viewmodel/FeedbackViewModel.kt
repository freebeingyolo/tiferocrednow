package com.css.wondercorefit.viewmodel

import androidx.lifecycle.MutableLiveData
import com.css.base.net.CommonResponse
import com.css.base.net.api.repository.SettingRepository
import com.css.base.net.api.repository.UserRepository
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.service.data.FeedbackData
import com.css.service.data.LoginUserData
import com.css.service.data.UserData
import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache

/**
 * Created by YH
 * Describe 意见反馈 VM
 * on 2021/7/8.
 */
class FeedbackViewModel : BaseViewModel() {

    val submitDate = MutableLiveData<String>()

    fun doSubmit(
        isShowSubmit: Boolean,
        content: String,
        data: String,
        time: String,
        phone: String
    ) {
        if (isShowSubmit) {
            //可以提交
            //检查电话是否合规
            if (phone.length != 11) {
                showCenterToast("请输入正确的手机号码")
            } else {
                netLaunch(
                    {
                        showLoading()
                        val loginData =
                            WonderCoreCache.getData(CacheKey.LOGIN_DATA, LoginUserData::class.java)
                        SettingRepository.submit(
                            content,
                            data,
                            time,
                            phone,
                            loginData.userId.toString()
                        )
                    }, { msg, _ ->
                        hideLoading()
                        submitDate.value = msg
                    }, { _, msg, _ ->
                        hideLoading()
                        showToast(msg)
                    }
                )
            }
        }
    }

    fun getFeedBackHistory() {
//        netLaunch(
//            {
//                showLoading()
//                SettingRepository.queryFeedBackHistory
//                (“”
////                    WonderCoreCache.getData(
////                        CacheKey.LOGIN_DATA,
////                        LoginUserData::class.java
////                    ).userId.toString()
//                )
//            }, { _, d ->
//                hideLoading()
////                personInfoData.value = d
//            }, { _, msg, _ ->
//                hideLoading()
////                nonePersonInfoData.value = ""
//                showToast(msg)
//            }
//        )
    }

}
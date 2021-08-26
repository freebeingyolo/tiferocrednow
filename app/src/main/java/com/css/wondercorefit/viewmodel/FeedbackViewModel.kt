package com.css.wondercorefit.viewmodel

import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.RegexUtils
import com.css.base.net.api.repository.SettingRepository
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.service.data.FeedbackData
import com.css.service.utils.WonderCoreCache

/**
 * Created by YH
 * Describe 意见反馈 VM
 * on 2021/7/8.
 */
class FeedbackViewModel : BaseViewModel() {

    val submitData = MutableLiveData<String>()
    val historyData = MutableLiveData<ArrayList<FeedbackData>>()
    val historyDetails = MutableLiveData<ArrayList<FeedbackData>>()

    fun doSubmit(
        isShowSubmit: Boolean,
        id: Int,
        phone: String,
        content: String
    ) {
        if (isShowSubmit) {
            //可以提交
            //检查电话是否合规
            if (!RegexUtils.isMobileExact(phone)) {
                showCenterToast("手机格式有误")
            } else if (content.length < 10) {
                showCenterToast("请输入至少10个字以上")
            } else {
                netLaunch(
                    {
                        showLoading()
                        SettingRepository.submit(
                            WonderCoreCache.getLoginInfo()?.userId,
                            id,
                            phone,
                            content
                        )
                    }, { msg, _ ->
                        hideLoading()
                        submitData.value = msg
                    }, { _, msg, _ ->
                        hideLoading()
                        showCenterToast(msg)
                    }
                )
            }
        }
    }

    fun queryFeedBackHistory() {
        netLaunch(
            {
                showLoading()
                SettingRepository.queryFeedBackHistory()
            }, { _, d ->
                hideLoading()
                historyData.value = d
            }, { _, msg, _ ->
                hideLoading()
                showCenterToast(msg)
            }
        )
    }

    fun queryFeedBackHistoryDetail(id: Int) {
        netLaunch(
            {
                showLoading()
                SettingRepository.queryFeedBackHistoryDetail(id)
            }, { _, d ->
                hideLoading()
                historyDetails.value = d
            }, { _, msg, _ ->
                hideLoading()
                showCenterToast(msg)
            }
        )
    }

}
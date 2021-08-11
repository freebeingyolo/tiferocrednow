package com.css.ble.viewmodel

import androidx.lifecycle.MutableLiveData
import com.css.base.net.api.repository.DeviceRepository
import com.css.base.net.api.repository.SettingRepository
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.base.utils.DateTimeHelper
import com.css.service.data.FeedbackData
import com.css.service.data.PullUpData

/**
 * Created by YH
 * Describe 数据统计VM
 * on 2021/8/2.
 */
class DataStatisticsVM : BaseViewModel() {
    val pullUpDataList = MutableLiveData<List<PullUpData>>()

    fun queryPushUps(deviceType: String, startDate: String, endDate: String) {
        netLaunch(
            {
                showLoading()
                DeviceRepository.queryPushUps(deviceType, startDate, endDate)
            }, { _, d ->
                hideLoading()
                pullUpDataList.value = d
            }, { _, msg, _ ->
                hideLoading()
                showToast(msg)
            }
        )
    }


}
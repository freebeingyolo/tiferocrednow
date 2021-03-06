package com.shopwonder.jingzaoyd.viewmodel

import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.AppUtils
import com.css.base.net.api.repository.SettingRepository
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.service.data.UpGradeData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AboutUsViewModel : BaseViewModel() {
    val upGradeData = MutableLiveData<UpGradeData>()

    fun getUpGrade() {
        netLaunch(
            {
                showLoading()
                withContext(Dispatchers.IO) {
                    SettingRepository.upGrade(AppUtils.getAppVersionCode().toString())
                }
            }, { msg, data ->
                hideLoading()
                if (data != null) {
                    upGradeData.value = data
                } else {
                    showCenterToast(msg)
                }

            }, { _, msg, _ ->
                hideLoading()
                showCenterToast(msg)
            }
        )
    }
}
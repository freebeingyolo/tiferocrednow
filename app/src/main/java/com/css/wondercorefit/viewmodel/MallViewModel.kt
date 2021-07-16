package com.css.wondercorefit.viewmodel

import androidx.lifecycle.MutableLiveData
import com.css.base.net.api.repository.MallRepository
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.service.data.MallData

class MallViewModel : BaseViewModel() {
    val mallData = MutableLiveData<ArrayList<MallData>>()
    fun getMallInfo() {
        netLaunch(
            {
                showLoading()
                MallRepository.queryMall()
            }, { _, data ->
                hideLoading()
                mallData.value = data
            }, { _, msg, _ ->
                hideLoading()
                showToast(msg)
            }
        )
    }
}
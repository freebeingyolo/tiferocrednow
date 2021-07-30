package com.css.wondercorefit.viewmodel

import androidx.lifecycle.MutableLiveData
import com.css.base.net.api.repository.MallRepository
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.service.data.MallData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MallViewModel : BaseViewModel() {
    val mallData = MutableLiveData<ArrayList<MallData>>()
    fun getMallInfo() {
        netLaunch(
            {
                showLoading()
                withContext(Dispatchers.IO) {
                    MallRepository.queryMall()
                }
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
package com.shopwonder.jingzaoyd.viewmodel

import androidx.lifecycle.MutableLiveData
import com.css.base.net.HttpNetCode
import com.css.base.net.api.repository.CourseRepository
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.service.data.CourseData
import com.shopwonder.jingzaoyd.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CourseViewModel : BaseViewModel() {
    val courseData = MutableLiveData<List<CourseData>>()
    fun getCourseInfo() {
        netLaunch(
            {
                showLoading()
                withContext(Dispatchers.IO) {
                    CourseRepository.queryVideo()
                }
            }, { _, data ->
                hideLoading()
                courseData.value = data
            }, {code, msg, _ ->
                hideLoading()
                if (code != HttpNetCode.NET_CONNECT_ERROR) {
                    showCenterToast(msg)
                }
            }
        )
    }
}
package com.shopwonder.jingzaoyd.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.css.base.uibase.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class SplashViewModel : BaseViewModel() {
    val mDownSecondNormalEvent = MutableLiveData<Long>()
    var mNormalTask: Job? = null

    fun downTimeNormalTask(sec: Long) {
        mNormalTask = viewModelScope.launch {
            downTimeNormal(sec)
        }
    }

    private suspend fun downTimeNormal(sec: Long) {
        flow {
            for (i in sec downTo 0) {
                delay(1000)
                emit(i)
            }
        }
            .collect {
                if (it <= 0) {
                    mDownSecondNormalEvent.value = it
                }
            }

    }
}
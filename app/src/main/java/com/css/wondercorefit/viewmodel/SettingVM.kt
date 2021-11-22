package com.css.wondercorefit.viewmodel

import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.service.data.UserData
import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache

/**
 *@author baoyuedong
 *@time 2021-11-22 14:52
 *@description
 */
class SettingVM : BaseViewModel() {
    val userData = WonderCoreCache.getLiveData<UserData>(CacheKey.USER_INFO)
}
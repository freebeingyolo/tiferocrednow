package com.css.base.uibase

import androidx.viewbinding.ViewBinding
import com.css.base.uibase.base.BaseWonderFragment
import com.css.base.uibase.viewmodel.BaseViewModel

abstract class BaseFragment<VM : BaseViewModel,VB:ViewBinding> : BaseWonderFragment<VM,VB>() {

}
package com.css.base.uibase

import androidx.fragment.app.Fragment
import com.css.base.uibase.base.BaseWonderFragment
import com.css.base.uibase.viewmodel.BaseViewModel

abstract class BaseFragment<VM : BaseViewModel> : BaseWonderFragment<VM>() {

}
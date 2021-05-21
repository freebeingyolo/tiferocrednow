package com.css.ble.utils

import android.util.Log
import androidx.annotation.IntDef
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.ActivityUtils
import com.css.ble.R
import java.lang.IllegalStateException


/**
 * @author yuedong
 * @date 2021-05-20
 */
object FragmentUtils {
    enum class Option {
        OPT_ADD,
        OPT_REPLACE
    }

    fun <T : Fragment> changeFragment(cls: Class<T>, opt: Option = Option.OPT_ADD, id: Int = R.id.container): T {
        var fmgr = (ActivityUtils.getTopActivity() as FragmentActivity).supportFragmentManager
        return changeFragment(cls, opt, id, fmgr)
    }

    //切换Fragment,Fragmente不存在就创建，存在则弹出它位置上面的fragment
    fun <T : Fragment> changeFragment(cls: Class<T>, opt: Option, id: Int = R.id.container, supportFragmentManager: FragmentManager): T {
        var tag = cls.simpleName
        var fragment: T? = supportFragmentManager.findFragmentByTag(tag) as T?
        if (opt == Option.OPT_REPLACE && supportFragmentManager.fragments.size > 0) supportFragmentManager.popBackStack()
        var ft = supportFragmentManager.beginTransaction()
        //隐藏上一个fragment
        supportFragmentManager.apply {
//            if (backStackEntryCount > 0) {
//                var lastFragment = fragments.findLast { getBackStackEntryAt(backStackEntryCount - 1).name == it.javaClass.simpleName }
//                lastFragment?.let { ft.hide(it) }
//            }
            for (fm in fragments) {
                if (fm != fragment && !fm.isHidden) {
                    ft.hide(fm)
                }
            }
            if (fragments.size > 0 && fragments[0] != fragment) {
                ft.addToBackStack(tag)
            }
        }
        if (fragment == null) {
            fragment = cls.newInstance()
            if (!fragment!!.isAdded) {
                when (opt) {
                    Option.OPT_REPLACE,
                    Option.OPT_ADD -> {
                        ft.add(id, fragment, tag)
                    }
                }
            }
        } else {
            //在回退栈里找
            var findInBackEntry = supportFragmentManager.run {
                for (i in backStackEntryCount - 1 downTo 0) {
                    if (getBackStackEntryAt(i).name == tag) {
                        return@run true
                    }
                }
                false
            }
            if (findInBackEntry) {//弹出tag以上的fragment
                supportFragmentManager.popBackStack(tag, 0)
            } else {
                //找不到回退栈,全弹出
                supportFragmentManager.apply {
                    if (backStackEntryCount > 0) {
                        supportFragmentManager.popBackStack(supportFragmentManager.getBackStackEntryAt(0).name, 1)
                    }
                }
            }
        }
        ft.show(fragment)
        ft.commit()
        return fragment
    }

}





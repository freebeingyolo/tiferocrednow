package com.css.ble.utils

import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.ActivityUtils
import com.css.ble.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


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

    fun <T : Fragment> changeFragment(cls: Class<T>, opt: Option, id: Int = R.id.container, supportFragmentManager: FragmentManager): T {
        var tag = cls.simpleName
        var fragment: T? = supportFragmentManager.findFragmentByTag(tag) as T?
        val addOprt: (Fragment, Int) -> Unit = { f, o ->
            val ft2 = supportFragmentManager.beginTransaction()
            //A -> B，将A的历史压入栈，第一个元素不压入历史栈
            if (supportFragmentManager.fragments.size > o) {
                ft2.addToBackStack(supportFragmentManager.fragments[supportFragmentManager.fragments.size - (o + 1)].javaClass.simpleName)
            }
            ft2.add(R.id.container, f, tag)
            ft2.commitAllowingStateLoss() //如果commit,被系统回收会异常
        }
        if (fragment == null) { //新增的
            fragment = cls.newInstance()
            if (!fragment!!.isAdded) {

                when (opt) {
                    Option.OPT_ADD -> {
                        addOprt(fragment, 0)
                    }
                    Option.OPT_REPLACE -> {
                        if (supportFragmentManager.backStackEntryCount > 0) {
                            supportFragmentManager.popBackStack()
                        }
                        if (supportFragmentManager.fragments.size > 0) {
                            var ft = supportFragmentManager.beginTransaction()
                            ft.remove(supportFragmentManager.fragments[supportFragmentManager.fragments.size - 1])
                            ft.commitAllowingStateLoss()
                            addOprt(fragment, 1)
                        } else {
                            addOprt(fragment, 0)
                        }
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
            if (findInBackEntry) supportFragmentManager.popBackStack(tag, 1)
        }

        /*GlobalScope.launch {
            delay(100)
            var str: String = ""
            for (i in supportFragmentManager.backStackEntryCount - 1 downTo 0) {
                str += supportFragmentManager.getBackStackEntryAt(i).name + "|"
            }
            var str2: String = ""
            for (i in supportFragmentManager.fragments.size - 1 downTo 0) {
                str2 += supportFragmentManager.fragments[i].tag + "|"
            }
            Log.d("changeFragment", "run on Main Thread:" + (Looper.myLooper() == Looper.getMainLooper()) + " $tag")
            Log.d("changeFragment", "supportFragmentManager.backEntry:$str")
            Log.d("changeFragment", "supportFragmentManager.fragments:$str2")
        }*/
        return fragment
    }


}






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
            ft2.commit()
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
                            ft.commit()
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
        return fragment!!
    }

    //切换Fragment,Fragmente不存在就创建，存在则弹出它位置上面的fragment
    fun <T : Fragment> changeFragment2(cls: Class<T>, opt: Option, id: Int = R.id.container, supportFragmentManager: FragmentManager): T {
        var tag = cls.simpleName
        var fragment: T? = supportFragmentManager.findFragmentByTag(tag) as T?
        if (opt == Option.OPT_REPLACE && supportFragmentManager.fragments.size > 0) supportFragmentManager.popBackStack()
        var ft = supportFragmentManager.beginTransaction()
        //隐藏上一个fragment
        supportFragmentManager.apply {
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






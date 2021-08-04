package com.css.ble.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.ActivityUtils
import com.css.ble.R


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
        val fmgr = (ActivityUtils.getTopActivity() as FragmentActivity).supportFragmentManager
        return changeFragment(cls, cls.simpleName, opt, id, fmgr)
    }

    fun <T : Fragment> changeFragment(
        cls: Class<T>,
        tag: String,
        opt: Option,
        initializer: ((Class<T>) -> T) = { cls.newInstance() },
        id: Int = R.id.container,
    ): T {
        val fmgr = (ActivityUtils.getTopActivity() as FragmentActivity).supportFragmentManager
        return changeFragment(cls, tag, opt, id, fmgr, initializer)
    }

    fun <T : Fragment> changeFragment(
        cls: Class<T>,
        tag: String,
        opt: Option,
        id: Int = R.id.container,
        supportFragmentManager: FragmentManager,
        initializer: ((Class<T>) -> T) = { cls.newInstance() },
    ): T {
        var fragment: T? = supportFragmentManager.findFragmentByTag(tag) as T?
        val addOprt: (Fragment, Int) -> Unit = { f, o ->
            val ft2 = supportFragmentManager.beginTransaction()
            //A -> B，将A的历史压入栈，第一个元素不压入历史栈
            if (supportFragmentManager.fragments.size > o) {
                ft2.addToBackStack(supportFragmentManager.fragments[supportFragmentManager.fragments.size - (o + 1)].javaClass.simpleName)
            }
            ft2.add(id, f, tag)
            for (f2 in supportFragmentManager.fragments) {
                if (f2 == f) {
                    ft2.show(f2)
                } else {
                    ft2.hide(f2)
                }
            }
            ft2.commitAllowingStateLoss() //如果commit,被系统回收会异常
        }
        if (fragment == null) { //新增的
            fragment = initializer(cls)
            if (!fragment.isAdded) {
                when (opt) {
                    Option.OPT_ADD -> {
                        addOprt(fragment, 0)
                    }
                    Option.OPT_REPLACE -> {
                        if (supportFragmentManager.backStackEntryCount > 0) {
                            supportFragmentManager.popBackStack()
                        }
                        if (supportFragmentManager.fragments.size > 0) {
                            val ft = supportFragmentManager.beginTransaction()
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
            val findInBackEntry = supportFragmentManager.run {
                for (i in backStackEntryCount - 1 downTo 0) {
                    if (getBackStackEntryAt(i).name == tag) {
                        return@run true
                    }
                }
                false
            }
            if (findInBackEntry) {
                supportFragmentManager.popBackStackImmediate(tag, 1)
                supportFragmentManager.popBackStackImmediate()
            }
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






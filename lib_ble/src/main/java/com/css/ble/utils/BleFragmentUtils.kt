package com.css.ble.utils

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
object BleFragmentUtils {
    const val OP_ADD = 0
    const val OP_REPLACE = 1

    //当前Activity是FragmentManager,可以不传supportFragmentManager
    fun <T : Fragment> changeFragment(
        cls: Class<T>,
        addToBackStack: Boolean = true,
        @OptionRange option: Int = OP_ADD,
    ): T {
        var act = ActivityUtils.getTopActivity()
        if (act is FragmentActivity) {
            return changeFragment(act.supportFragmentManager, cls, addToBackStack, option)
        } else {
            throw IllegalStateException("you can't call changeFragment at the non-framgent activity")
        }
    }

    fun <T : Fragment> changeFragment(
        fragmentManager: FragmentManager,
        cls: Class<T>,
        addToBackStack: Boolean = true,
        @OptionRange option: Int = OP_ADD,
    ): T {
        val newFragmentTag = cls.simpleName
        val ft = fragmentManager.beginTransaction()
        var fragment = fragmentManager.findFragmentByTag(newFragmentTag)
        if (fragment == null) {
            fragment = cls.newInstance()
            var curFragment = fragmentManager.fragments.takeIf { it.size > 0 }?.let { it[it.size - 1] }
            curFragment?.let { ft.hide(it) }
            if (!fragment.isAdded) {
                when (option) {
                    OP_ADD -> ft.add(R.id.content, fragment, newFragmentTag)
                    OP_REPLACE -> ft.replace(R.id.content, fragment, newFragmentTag)
                }
                if (addToBackStack) ft.addToBackStack(newFragmentTag)
            }
        } else {
            //将supportFragmentManager栈中fragment之前的都弹栈
            var size = fragmentManager.fragments.size
            for (i in size - 1 downTo 0) {
                if (fragmentManager.fragments[i] == fragment) break
                fragmentManager.popBackStackImmediate()
            }
            ft.show(fragment)
        }
        ft.commit()
        return fragment as T
    }

    //@Retention表示这个注解保留的范围，SOURCE=注解将被编译器编译的时候丢弃，不在代码运行时存在，这个注解只是希望IDE警告限定值的范围并不需要保留到VM或者运行时
    @Retention(AnnotationRetention.SOURCE) //@Target 这个注解需要使用的地方 PARAMETER=注解将被使用到方法的参数中
    @Target(AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.VALUE_PARAMETER) //显式声明被定义的整数值，除了@IntDef还有@LongDef @StringDef等等
    @IntDef(value = [OP_ADD, OP_REPLACE])
    annotation class OptionRange()
}


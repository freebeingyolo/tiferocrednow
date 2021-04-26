package com.css.base

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import java.util.*

object ActivityHolder {

    private val activityStack: Stack<Activity> by lazy { Stack<Activity>() }

    fun addActivity(activity: Activity) {
        activityStack.push(activity)
    }

    fun currentActivity(): Activity {
        return activityStack.lastElement()
    }

    fun removeActivity(activity: Activity?): Activity? {
        activityStack.remove(activity)
        return activity
    }

    fun finishActivity() {
        val activity = activityStack.lastElement()
        finishActivity(activity)
    }

    fun finishActivityTop(cls: Class<*>) {
        val size = activityStack.size
        for (i in size - 1 downTo 0) {
            val activity = activityStack[i]
            if (activityStack[i]!!.javaClass == cls) {
                finishActivity(activity)
                return
            }
        }
    }

    //结束栈中指定Activity上面的所有Activity
    fun finishTopAllActivityButFirst(cls: Class<*>) {
        val size = activityStack.size
        val tempStack = Stack<Activity?>()
        var actExist = false
        for (i in size - 2 downTo 0) {
            val activity = activityStack[i]
            tempStack.add(activity)
            if (activityStack[i]!!.javaClass == cls) {
                actExist = true
                break
            }
        }
        if (actExist) {
            for (i in tempStack.indices) {
                val activity = tempStack[i]
                activity!!.finish()
            }
        }
    }

    /**
     * 结束栈中指定Activity上面的所有Activity，不包含指定的Activity
     *
     * @param cls
     * @param offset 跳过上面几个
     */
    fun finishTopAllActivityUntil(cls: Class<*>, offset: Int) {
        val size = activityStack.size
        val tempStack = Stack<Activity?>()
        var actExist = false
        for (i in size - 1 - offset downTo 0) {
            val activity = activityStack[i]
            if (activityStack[i]!!.javaClass == cls) {
                actExist = true
                break
            } else {
                tempStack.add(activity)
            }
        }
        if (actExist) {
            for (i in tempStack.indices) {
                val activity = tempStack[i]
                activity!!.finish()
            }
        }
    }

    fun finishAllActivity() {
        for (i in activityStack.indices) {
            if (activityStack[i] != null) {
                activityStack[i]!!.finish()
            }
        }
        activityStack.clear()
        System.exit(0)
    }

    fun finishAllActivityButFirst() {
        for (i in 0 until activityStack.size - 1) {
            if (activityStack[i] != null) {
                activityStack[i]!!.finish()
            }
        }
        val activity = activityStack[activityStack.size - 1]
        activityStack.clear()
        activityStack.add(activity)
    }

    fun appExit(context: Context) {
        try {
            finishAllActivity()
            if(false){
                var activityMgr = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                activityMgr.restartPackage(context.getPackageName())
                System.exit(0)
            }
        } catch (localException: Exception) {
        }
    }

    /**
     * 调用此方法用于退出整个Project
     */
    fun exit() {
        try {
            for (activity in activityStack) {
                activity?.finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            System.exit(0)
        }
    }


    fun finishActivity(activity: Activity?) {
        if (activity != null) {
            activityStack.remove(activity)
            activity.finish()
        }
    }
}
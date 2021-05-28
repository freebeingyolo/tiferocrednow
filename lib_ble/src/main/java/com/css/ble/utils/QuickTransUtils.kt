package com.css.ble.utils

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.blankj.utilcode.util.ActivityUtils

/**
 * startActivityForResult 和 requestPermissions 一步调用
 * @author yuedong
 * @date 2021-05-28
 */
object QuickTransUtils {

    open fun isPermissionAllowed(ac: Activity, permissions: Array<String?>): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(ac, permission!!) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.shouldShowRequestPermissionRationale(ac, permission)
            ) {
                return false
            }
        }
        return true
    }

    fun startActivityForResult(intent: Intent, onActivityResultCB: ((Activity, Int, Int, Intent?) -> Unit)? = null) {
        val f = TransFragment(object : TransFragment.TransDelegate {
            override fun onCreated(fragment: Fragment, savedInstanceState: Bundle?) {
                fragment.startActivityForResult(intent, 0x100)
            }

            override fun onActivityResult(fragment: Fragment, requestCode: Int, resultCode: Int, data: Intent?) {
                if (onActivityResultCB != null) onActivityResultCB(fragment.requireActivity(), requestCode, resultCode, data)
            }
        })
        val fm = (ActivityUtils.getTopActivity() as FragmentActivity).supportFragmentManager
        val trans = fm.beginTransaction()
        trans.add(f, TransFragment.TAG)
        trans.commitAllowingStateLoss()
    }

    fun requestPermissions(
        permissions: Array<String>,
        onRequestPermissionsResult: ((Activity, Int, Array<out String>?, IntArray?) -> Unit)? = null
    ) {
        val f = TransFragment(object : TransFragment.TransDelegate {
            override fun onCreated(fragment: Fragment, savedInstanceState: Bundle?) {
                ActivityCompat.requestPermissions(fragment.requireActivity(), permissions, 0x100)
            }

            override fun onRequestPermissionsResult(
                fragment: Fragment,
                requestCode: Int,
                permissions: Array<out String>,
                grantResults: IntArray
            ) {
                if (onRequestPermissionsResult != null) onRequestPermissionsResult(
                    fragment.requireActivity(),
                    requestCode,
                    permissions,
                    grantResults
                )
            }

        })
        val fm = (ActivityUtils.getTopActivity() as FragmentActivity).supportFragmentManager
        val trans = fm.beginTransaction()
        trans.add(f, TransFragment.TAG)
        trans.commitAllowingStateLoss()
    }

    class TransFragment(private val delegate: TransDelegate) : Fragment() {
        val finishAfterUsed = true
        companion object {val TAG = "TransFragment"}

        override fun onCreate(savedInstanceState: Bundle?) {
            delegate.onCreateBefore(this, savedInstanceState)
            super.onCreate(savedInstanceState)
            delegate.onCreated(this, savedInstanceState)
            Log.d(TAG,"TransFragment#onCreate")
        }

        override fun onStart() {
            super.onStart()
            delegate.onStarted(this)
        }

        override fun onResume() {
            super.onResume()
            delegate.onResumed(this)
        }

        override fun onPause() {
            super.onPause()
            delegate.onPaused(this)
        }

        override fun onStop() {
            super.onStop()
            delegate.onStop(this)
        }

        override fun onDestroy() {
            super.onDestroy()
            delegate.onDestroy(this)
            Log.d(TAG,"TransFragment#onDestroy")
        }


        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            delegate.onActivityResult(this, requestCode, resultCode, data)
            if (finishAfterUsed) {
                var trans = requireFragmentManager().beginTransaction()
                trans.remove(this)
                trans.commitAllowingStateLoss()
            }
        }

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            delegate.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
            if (finishAfterUsed) {
                var trans = requireFragmentManager().beginTransaction()
                trans.remove(this)
                trans.commitAllowingStateLoss()
            }
        }

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            delegate.onSaveInstanceState(this, outState)
        }


        interface TransDelegate {
            fun onCreateBefore(fragment: Fragment, savedInstanceState: Bundle?) {}
            fun onCreated(fragment: Fragment, savedInstanceState: Bundle?) {}
            fun onStarted(fragment: Fragment) {}
            fun onDestroy(fragment: Fragment) {}
            fun onResumed(fragment: Fragment) {}
            fun onPaused(fragment: Fragment) {}
            fun onStop(fragment: Fragment) {}
            fun onSaveInstanceState(fragment: Fragment, outState: Bundle?) {}
            fun onRequestPermissionsResult(fragment: Fragment, requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {}
            fun onActivityResult(fragment: Fragment, requestCode: Int, resultCode: Int, data: Intent?) {}
        }
    }
}
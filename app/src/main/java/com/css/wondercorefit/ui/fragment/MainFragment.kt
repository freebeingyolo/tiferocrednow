package com.css.wondercorefit.ui.fragment

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseFragment
import com.css.service.utils.SystemBarHelper
import com.css.step.ISportStepInterface
import com.css.step.TodayStepManager
import com.css.step.service.SensorService
import com.css.step.service.TodayStepService
import com.css.wondercorefit.databinding.FragmentMainBinding
import com.css.wondercorefit.viewmodel.MainViewModel


class MainFragment : BaseFragment<MainViewModel,FragmentMainBinding>() {
    private val TAG = "TodayStepService"

    private lateinit var iSportStepInterface: ISportStepInterface
    private var stepArray:Int = 0
    private val mDelayHandler = Handler(TodayStepCounterCall())
    private val REFRESH_STEP_WHAT = 0
    private val TIME_INTERVAL_REFRESH: Long = 1000

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(activity, 0f)
        SystemBarHelper.setHeightAndPadding(activity, mViewBinding?.topView)
        startSensorService()
        startStep()
    }

    private fun startSensorService() {
        val intentSensor = Intent(activity, SensorService::class.java)
        if(Build.VERSION.SDK_INT >= 26){
            activity?.startForegroundService (intentSensor);
        }else{
            activity?.startService (intentSensor);
        }
    }

    private fun startStep() {
        activity?.let { TodayStepManager().init(it.application) }

        //开启计步Service，同时绑定Activity进行aidl通信
        val intentSteps = Intent(activity, TodayStepService::class.java)
        if(Build.VERSION.SDK_INT >= 26){
            activity?.startForegroundService (intentSteps);
        }else{
            activity?.startService (intentSteps);
        }
        activity?.bindService(intentSteps, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                //Activity和Service通过aidl进行通信
                iSportStepInterface = ISportStepInterface.Stub.asInterface(service)
                try {
                    stepArray = iSportStepInterface.todaySportStepArray
                    updataValues(stepArray)
                    Log.d(TAG, " getTodaySportStepArray  :   $stepArray")
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
                mDelayHandler.sendEmptyMessageDelayed(
                    REFRESH_STEP_WHAT,
                    TIME_INTERVAL_REFRESH
                )
            }

            override fun onServiceDisconnected(name: ComponentName) {}
        }, AppCompatActivity.BIND_AUTO_CREATE)
    }

    private fun updataValues(stepArray: Int) {
        val realSteps = stepArray
        if (realSteps == 0) {
            mViewBinding?.tvTodayStep?.text = "今天尚未运动，" + "\n" + "快去运动一下吧"
            mViewBinding?.tvStepNum?.visibility = View.INVISIBLE
        } else {
            mViewBinding?.tvStepNum?.text = realSteps.toString()
        }
        mViewBinding?.tvWalkingDistanceNum?.text = getDistanceByStep(realSteps.toLong())
        mViewBinding?.tvCalorieConsumptionNum?.text = getCalorieByStep(realSteps.toLong())
    }

    // 公里计算公式
    private fun getDistanceByStep(steps: Long): String {
        return String.format("%.2f", steps * 0.6f / 1000)
    }

    // 千卡路里计算公式
    private fun getCalorieByStep(steps: Long): String {
        return String.format("%.1f", steps * 0.6f * 60 * 1.036f / 1000)
    }

    inner class TodayStepCounterCall : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            when (msg.what) {
                REFRESH_STEP_WHAT -> {

                    //每隔500毫秒获取一次计步数据刷新UI
                    if (null != iSportStepInterface) {
                        var step: Int = 0
                        try {
                            step = iSportStepInterface!!.todaySportStepArray
                            Log.d(TAG," refresh UI in 5000 ms  :   " )
                            updataValues(step)
                        } catch (e: RemoteException) {
                            e.printStackTrace()
                        }
                        Log.d(TAG," stepArray  :  $stepArray    step   :   $step " )
                        if (stepArray != step) {
                            stepArray = step
//                            updateStepCount(stepArray)
                        }
                    }
                    mDelayHandler.sendEmptyMessageDelayed(
                        REFRESH_STEP_WHAT,
                        TIME_INTERVAL_REFRESH
                    )
                }
            }
            return false
        }
    }

    override fun initViewModel(): MainViewModel =
        ViewModelProvider(this).get(MainViewModel::class.java)


    override fun initViewBinding(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?
    ): FragmentMainBinding = FragmentMainBinding.inflate(inflater, viewGroup, false)
}
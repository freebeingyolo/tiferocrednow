package com.css.wondercorefit.ui.fragment

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.launcher.ARouter
import com.css.base.uibase.BaseFragment
//import com.css.ble.ui.WeightBondActivity
import com.css.service.data.StepData
import com.css.service.data.UserData
import com.css.service.router.ARouterConst
import com.css.service.utils.SystemBarHelper
import com.css.service.utils.WonderCoreCache
import com.css.step.ISportStepInterface
import com.css.step.TodayStepManager
import com.css.step.service.SensorService
import com.css.step.service.TodayStepService
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.FragmentMainBinding
import com.css.wondercorefit.viewmodel.MainViewModel


class MainFragment : BaseFragment<MainViewModel, FragmentMainBinding>(),View.OnClickListener,View.OnLongClickListener {
    private val TAG = "MainFragment"

    private lateinit var iSportStepInterface: ISportStepInterface
    private var stepArray: Int = 0
    private val mDelayHandler = Handler(TodayStepCounterCall())
    private val REFRESH_STEP_WHAT = 0
    private val TIME_INTERVAL_REFRESH: Long = 1000
    private lateinit var targetStep:String
    private var currentStep:Int = 0
    private var result:Float = 0.0f
    private lateinit var userData: UserData
    private lateinit var stepData: StepData

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(activity, 0f)
        SystemBarHelper.setHeightAndPadding(activity, mViewBinding?.topView)
        initdeviceWidget()
        startSensorService()
        startStep()
        initClickListenr()
        initProgressRate()
    }

    private fun initProgressRate() {
        userData = WonderCoreCache.getUserInfo()
        stepData = WonderCoreCache.getData(WonderCoreCache.STEP_DATA, StepData::class.java)
        targetStep = userData.targetStep
        mViewBinding!!.tvTodayStepTarget.text = "目标 " + targetStep
        currentStep = stepData.todaySteps
        result = ((currentStep*100)/targetStep.toInt()).toFloat()
        Log.d(TAG, "ProgressInformation   :  $currentStep    $targetStep    $result")
        mViewBinding?.pbStep?.setProgress(result)
    }

    private fun initdeviceWidget() {
        mViewBinding!!.bleScale.setOnLongClickListener(this)
        mViewBinding!!.bleWheel.setOnLongClickListener(this)
        mViewBinding!!.bleScale.setOnClickListener {
            ARouter.getInstance()
                .build(ARouterConst.PATH_APP_BLE_WEIGHTBOND)
                .navigation()
        }
        mViewBinding!!.bleWheel.setOnClickListener {
//            var intentScale = Intent (activity , WeightBondActivity::class.java)
//            startActivity(intentScale)
        }
    }

    private fun initClickListenr() {
        mViewBinding!!.addBleDevice.setOnClickListener {
            ARouter.getInstance()
                .build(ARouterConst.PATH_APP_BLE)
                .navigation()
        }
    }

    private fun startSensorService() {
        val intentSensor = Intent(activity, SensorService::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            activity?.startForegroundService(intentSensor)
        } else {
            activity?.startService(intentSensor)
        }
    }

    private fun startStep() {
        activity?.let { TodayStepManager().init(it.application) }

        //开启计步Service，同时绑定Activity进行aidl通信
        val intentSteps = Intent(activity, TodayStepService::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            activity?.startForegroundService(intentSteps)
        } else {
            activity?.startService(intentSteps)
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
//            mViewBinding?.tvTodayStep?.text = getString(R.string.zero_stepsOne) + "\n" + getString(R.string.zero_stepsTwo)
//            mViewBinding?.tvStepNum?.visibility = View.INVISIBLE
            mViewBinding?.tvStepNum?.text = "--"
        } else {
//            mViewBinding?.tvStepNum?.visibility = View.VISIBLE
//            mViewBinding?.tvTodayStep?.text = getString(R.string.today_steps)
            mViewBinding?.tvStepNum?.text = realSteps.toString()
        }
        mViewBinding?.tvWalkingDistanceNum?.text = getDistanceByStep(realSteps.toLong())
        mViewBinding?.tvCalorieConsumptionNum?.text = getCalorieByStep(realSteps.toLong())
        result = ((realSteps*100)/targetStep.toInt()).toFloat()
        mViewBinding?.pbStep?.setProgress(result)
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
                        try {
                            stepArray = iSportStepInterface.todaySportStepArray
                            Log.d(TAG, " refresh UI in 500 ms  :   ")
                            updataValues(stepArray)
                        } catch (e: RemoteException) {
                            e.printStackTrace()
                        }
                        Log.d(TAG, " stepArray  :  $stepArray    step   :   $stepArray ")
                        if (stepArray != stepArray) {
                            stepArray = stepArray
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

    override fun onClick(v: View) {
       when(v.id){
       }
    }

    override fun onLongClick(view: View?): Boolean {
        Log.d(TAG, "onLongClick   :  ${view.toString()}")
        when (view?.id) {
            R.id.ble_scale -> deleteWeight()
            R.id.ble_wheel -> deleteWheel()
            else -> {
            }
        }
        return true
    }

    private fun deleteWeight() {
        mViewBinding!!.deleteWeight.visibility = View.VISIBLE
        val vibrator = activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(100)
        val handler = Handler()
        handler.postDelayed({
            mViewBinding!!.deleteWeight.visibility = View.GONE
        }, 3000) //3000毫秒后执行

        mViewBinding!!.deleteWeight.setOnClickListener {
            mViewBinding!!.deviceWeight.visibility = View.GONE
            mViewBinding!!.addDeviceWeight.visibility = View.VISIBLE
            deleteDevice()
        }
        Toast.makeText(activity, "长按体脂秤收到", Toast.LENGTH_SHORT).show()
    }

    private fun deleteWheel() {
        mViewBinding!!.deleteWheel?.visibility = View.VISIBLE
        val vibrator = activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(100)
        val handler = Handler()
        handler.postDelayed({
            mViewBinding!!.deleteWheel.visibility = View.GONE
        }, 3000) //3000毫秒后执行
        mViewBinding!!.deleteWheel.setOnClickListener {
            mViewBinding!!.deviceWheel.visibility = View.GONE
            mViewBinding!!.addDeviceWheel.visibility = View.VISIBLE
            deleteDevice()
        }
        Toast.makeText(activity, "长按健腹轮收到", Toast.LENGTH_SHORT).show()
    }

    private fun deleteDevice() {
        Toast.makeText(activity, "删除设备成功", Toast.LENGTH_SHORT).show()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        initProgressRate()
    }
}
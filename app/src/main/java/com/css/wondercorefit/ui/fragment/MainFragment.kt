package com.css.wondercorefit.ui.fragment

//import com.css.ble.ui.WeightBondActivity
import android.content.*
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.LogUtils
import com.css.base.uibase.BaseFragment
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.WeightBondData
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


class MainFragment : BaseFragment<MainViewModel, FragmentMainBinding>(), View.OnClickListener {
    private val TAG = "MainFragment"

    private lateinit var iSportStepInterface: ISportStepInterface
    private var stepArray: Int = 0
    private val mDelayHandler = Handler(TodayStepCounterCall())
    private val REFRESH_STEP_WHAT = 0
    private val TIME_INTERVAL_REFRESH: Long = 1000
    private lateinit var targetStep: String
    private var currentStep: Int = 0
    private var result: Float = 0.0f
    private lateinit var userData: UserData
    private lateinit var stepData: StepData

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(activity, 0f)
        SystemBarHelper.setHeightAndPadding(activity, mViewBinding?.topView)
        showDevice()
        startSensorService()
        startStep()
        initClickListenr()
        initProgressRate()

    }

    override fun initData() {
        super.initData()
        mViewBinding?.tvTargetWeightNum?.text = userData.targetWeight
        WeightBondData.firstWeightInfo?.let {
            mViewBinding?.tvInitialWeightNum?.text =
                it.getWeightKg().toString()
        }

    }

    private fun showDevice() {
        if (WonderCoreCache.getData(
                WonderCoreCache.BOND_WEIGHT_INFO,
                BondDeviceData::class.java
            ).mac.isNotEmpty()
        ) {
            mViewBinding?.llDevice?.visibility = View.VISIBLE
            mViewBinding?.deviceWeight?.visibility = View.VISIBLE
        }
        if (WonderCoreCache.getData(
                WonderCoreCache.BOND_WHEEL_INFO,
                BondDeviceData::class.java
            ).mac.isNotEmpty()
        ) {
            mViewBinding?.llDevice?.visibility = View.VISIBLE
            mViewBinding?.deviceWheel?.visibility = View.VISIBLE
        }
        sp?.registerOnSharedPreferenceChangeListener(spLis)
    }

    private val sp by lazy { activity?.getSharedPreferences("spUtils", Context.MODE_PRIVATE) }
    private val spLis by lazy {
        SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
            if (key.equals(WonderCoreCache.BOND_WEIGHT_INFO)) {
                if (WonderCoreCache.getData(
                        WonderCoreCache.BOND_WEIGHT_INFO,
                        BondDeviceData::class.java
                    ).mac.isNotEmpty()
                ) {
                    mViewBinding?.llDevice?.visibility = View.VISIBLE
                    mViewBinding?.deviceWeight?.visibility = View.VISIBLE
                } else {
                    mViewBinding?.llDevice?.visibility = View.GONE
                }

                if (WonderCoreCache.getData(
                        WonderCoreCache.BOND_WHEEL_INFO,
                        BondDeviceData::class.java
                    ).mac.isNotEmpty()
                ) {
                    mViewBinding?.llDevice?.visibility = View.VISIBLE
                    mViewBinding?.deviceWheel?.visibility = View.VISIBLE
                } else {
                    mViewBinding?.deviceWheel?.visibility = View.GONE
                }
            }
            LogUtils.vTag("suisui", key)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sp?.unregisterOnSharedPreferenceChangeListener(spLis)
    }

    private fun initProgressRate() {
        userData = WonderCoreCache.getUserInfo()
        stepData = WonderCoreCache.getData(WonderCoreCache.STEP_DATA, StepData::class.java)
        targetStep = userData.targetStep
        mViewBinding!!.tvTodayStepTarget.text = "目标 " + targetStep
        currentStep = stepData.todaySteps
        result = ((currentStep * 100) / targetStep.toInt()).toFloat()
        Log.d(TAG, "ProgressInformation   :  $currentStep    $targetStep    $result")
        mViewBinding?.pbStep?.setProgress(result)
    }

    private fun initClickListenr() {
        mViewBinding!!.gotoMeasure.setOnClickListener(this)
        mViewBinding!!.deviceWeight.setOnClickListener(this)
        mViewBinding!!.addBleDevice.setOnClickListener(this)
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
            mViewBinding?.tvStepNum?.text = "--"
        } else {
            mViewBinding?.tvStepNum?.text = realSteps.toString()
        }
        mViewBinding?.tvWalkingDistanceNum?.text = getDistanceByStep(realSteps.toLong())
        mViewBinding?.tvCalorieConsumptionNum?.text = getCalorieByStep(realSteps.toLong())
        result = ((realSteps * 100) / targetStep.toInt()).toFloat()
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
        when (v.id) {
            R.id.device_weight -> {
                ARouter.getInstance()
                    .build(ARouterConst.PATH_APP_BLE_WEIGHTMEASURE)
                    .navigation()
            }
            R.id.goto_measure -> {
                ARouter.getInstance()
                    .build(ARouterConst.PATH_APP_BLE_WEIGHTMEASURE)
                    .navigation()
            }

            R.id.add_ble_device -> {
                ARouter.getInstance()
                    .build(ARouterConst.PATH_APP_BLE)
                    .navigation()
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        initProgressRate()
    }
}
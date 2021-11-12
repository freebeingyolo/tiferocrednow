package com.css.wondercorefit.ui.fragment

import LogUtils
import android.app.Activity
import android.content.*
import android.graphics.Typeface
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.NetworkUtils
import com.css.base.uibase.BaseFragment
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.bean.WeightBondData
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM
import com.css.service.data.GlobalData
import com.css.service.data.StepData
import com.css.service.data.UserData
import com.css.service.router.ARouterConst
import com.css.service.utils.CacheKey
import com.css.service.utils.SystemBarHelper
import com.css.service.utils.WonderCoreCache
import com.css.step.ISportStepInterface
import com.css.step.TodayStepManager
import com.css.step.service.SensorService
import com.css.step.service.TodayStepService
import com.css.step.utils.BootstrapService
import com.css.wondercorefit.R
import com.css.wondercorefit.adapter.MainDeviceAdapter
import com.css.wondercorefit.databinding.FragmentMainBinding
import com.css.wondercorefit.ui.activity.setting.PersonInformationActivity
import com.css.wondercorefit.viewmodel.MainViewModel
import com.google.android.material.tabs.TabLayout


class MainFragment : BaseFragment<MainViewModel, FragmentMainBinding>(), View.OnClickListener,
    NetworkUtils.OnNetworkStatusChangedListener {
    private val TAG = "MainFragment"

    lateinit var userInfo: UserData
    private var iSportStepInterface: ISportStepInterface? = null
    private var stepArray: Int = 0
    private val mDelayHandler = Handler(TodayStepCounterCall())
    private val REFRESH_STEP_WHAT = 0
    private val TIME_INTERVAL_REFRESH: Long = 500
    private lateinit var targetStep: String
    private var currentStep: Int = 0
    private var result: Float = 0.0f
    private lateinit var stepData: StepData
    private lateinit var mUserData: UserData
    private var needNotify: Boolean = false
    private var pauseResume: Boolean = false
    private lateinit var mMainDeviceAdapter: MainDeviceAdapter
    var mData = ArrayList<BondDeviceData>()
    private val tabTitle = arrayListOf("最近使用", "全部")
    private var initialWeight: Float = -1f
    private var currentWeight: Float = -1f
    private var goalWeight: Float = -1f
    private lateinit var device: BondDeviceData

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(activity, 0f)
        SystemBarHelper.setHeightAndPadding(activity, mViewBinding?.topView)
        mViewBinding?.deviceList?.layoutManager = GridLayoutManager(activity, 2)
        mMainDeviceAdapter = MainDeviceAdapter(mData)
        mViewBinding?.deviceList?.adapter = mMainDeviceAdapter
        mMainDeviceAdapter.setOnItemClickListener {
            when (it.deviceType) {
                DeviceType.WEIGHT -> {
                    ARouter.getInstance()
                        .build(ARouterConst.PATH_APP_BLE_WEIGHTMEASURE)
                        .navigation()
                }
                DeviceType.WHEEL -> {
                    ARouter.getInstance()
                        .build(ARouterConst.PATH_APP_BLE_WHEELMEASURE)
                        .navigation()
                }
                else ->
                    ARouter.getInstance().build(ARouterConst.PATH_APP_BLE_COMMON)
                        .withInt("mode", BaseDeviceScan2ConnVM.WorkMode.MEASURE.ordinal)
                        .withInt("deviceType", it.deviceType.ordinal)
                        .navigation()
            }

        }
        showDevice()
        startSensorService()
        startStep()
        initClickListenr()
        initProgressRate()
        initTabLayout()
        initTabListener()
        if (WonderCoreCache.getGlobalData().isFirst) {
            activity?.let { PersonInformationActivity.starActivity(it) }
            WonderCoreCache.saveGlobalData(GlobalData(false))
        }
        startBootStrapService()
        if (NetworkUtils.isConnected()) {
            mViewBinding?.networkError?.visibility = View.GONE
            mViewBinding?.mainLayout?.visibility = View.VISIBLE
        } else {
            mViewBinding?.networkError?.visibility = View.VISIBLE
            mViewBinding?.mainLayout?.visibility = View.GONE
        }
        NetworkUtils.registerNetworkStatusChangedListener(this)
    }

    private fun startBootStrapService() {
        userInfo = WonderCoreCache.getUserInfo()
        if ("关" == userInfo.notification) {
            val intentSteps = Intent(activity, BootstrapService::class.java)
            val intent = Intent()
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.action = "android.intent.action.CLOSE_NOTIFICATION"
            Handler().postDelayed({
                activity?.sendBroadcast(intent)
                if (Build.VERSION.SDK_INT >= 26) {
                    activity?.startForegroundService(intentSteps)
                } else {
                    activity?.startService(intentSteps)
                }
            }, 1000)

        }

    }


    override fun initData() {
        super.initData()
        mViewBinding?.tvTargetWeightNum?.text = WonderCoreCache.getUserInfo().goalBodyWeight
        mViewBinding?.tvTargetWeightNum2?.text = WonderCoreCache.getUserInfo().goalBodyWeight
        goalWeight = WonderCoreCache.getUserInfo().goalBodyWeight.toFloat()
        mViewModel.loadDevice()

        WonderCoreCache.getLiveData<WeightBondData>(CacheKey.FIRST_WEIGHT_INFO).let {
            it.observe(this) { it2 ->
                if (it2 != null) {
                    mViewBinding?.tvInitialWeightNum?.text = it2.weightKgFmt
                    initialWeight = it2.weightKg
                } else {
                    mViewBinding?.tvInitialWeightNum?.text = "--"
                    initialWeight = -1f
                }
                mViewBinding?.tvSubtractedWeight?.text = getSubtractedWeight()
                mViewBinding?.progressRing?.progress = getSubtractedWeightPer()
            }
        }

        WonderCoreCache.getLiveData<WeightBondData>(CacheKey.LAST_WEIGHT_INFO)
            .observe(this) { it2 ->
                if (it2 != null) {
                    mViewBinding?.tvCurrentWeight?.text = it2.weightKgFmt("%.1f")
                    mViewBinding?.llBmi?.visibility = View.VISIBLE
                    mViewBinding?.tvBmi?.text = "BMI${it2?.bodyFatData.bmi}"
                    mViewBinding?.tvBodyType?.text = it2.bodyFatData.bmiJudge
                    currentWeight = it2.weightKg
                } else {
                    mViewBinding?.llBmi?.visibility = View.GONE
                    mViewBinding?.tvCurrentWeight?.text = "--"
                    currentWeight = -1f
                }
                mViewBinding?.tvSubtractedWeight?.text = getSubtractedWeight()
                mViewBinding?.progressRing?.progress = getSubtractedWeightPer()
            }
    }

    // 已减去体重
    private fun getSubtractedWeight(): String {
        if (-1f != initialWeight && -1f != currentWeight) {
            return String.format("%.1f", initialWeight - currentWeight)
        }
        return "--"
    }

    // 已减去体重百分比
    private fun getSubtractedWeightPer(): Int {
        if (-1f != initialWeight && -1f != currentWeight && -1f != goalWeight) {
            val sub = initialWeight - currentWeight
            val goal = initialWeight - goalWeight
            if (0f >= sub || 0f >= goal) {
                return 0
            } else if (sub >= goal) {
                return 100
            }
            return (sub / goal * 100).toInt()
        }
        return 0
    }

    private fun showDevice() {
        BondDeviceData.getDeviceLiveDataMerge().observe(viewLifecycleOwner) {
            //it为map，值为null表示删除，key为设备类型
            LogUtils.d("it-->$it")
            val devices = BondDeviceData.getDevices()
            mViewBinding?.recentDevice?.visibility = View.GONE
            mViewBinding?.blank?.visibility = View.VISIBLE
            if (devices.isNotEmpty()) {
                device = devices.first()
                mViewBinding?.recentDevice?.visibility = View.VISIBLE
                mViewBinding?.blank?.visibility = View.GONE
                mViewBinding?.recentDeviceName?.text = device.alias
                mViewBinding?.recentDeviceState?.text = device.deviceConnect
                mViewBinding?.recentDeviceImg?.setImageResource(device.deviceImg)
            }
            mData.clear()
            mData.addAll(devices)
            mMainDeviceAdapter.setItems(mData)
        }
        BondDeviceData.getDeviceConnectStateLiveData().observe(viewLifecycleOwner) {
            LogUtils.d("it--->$it")
            for (item in mData) {
                if (item.deviceCategory == it.first) {
                    item.deviceConnect = it.second
                }
            }
            mMainDeviceAdapter.setItems(mData)
            if (it.first == mViewBinding?.recentDeviceName?.text) {
                mViewBinding?.recentDeviceState?.text = it.second
            }
        }
        //绑定监听
        sp?.registerOnSharedPreferenceChangeListener(spLis)
    }

    private val sp by lazy { activity?.getSharedPreferences("spUtils", Context.MODE_PRIVATE) }
    private val spLis by lazy {
        SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
            when (key) {
                CacheKey.USER_INFO.k -> {
                    mUserData = WonderCoreCache.getUserInfo()
                    mViewBinding?.tvTargetWeightNum?.text = mUserData.goalBodyWeight
                    mViewBinding?.tvTargetWeightNum2?.text = mUserData.goalBodyWeight
                    mViewBinding?.tvTodayStepTarget?.text = "目标 ${mUserData.goalStepCount}"
                }

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sp?.unregisterOnSharedPreferenceChangeListener(spLis)
    }

    private fun initProgressRate() {
        mUserData = WonderCoreCache.getUserInfo()
        targetStep = mUserData.goalStepCount
        stepData = WonderCoreCache.getData(CacheKey.STEP_DATA, StepData::class.java) ?: StepData()
        currentStep = stepData.todaySteps
        result = ((currentStep * 100) / targetStep.toInt()).toFloat()
        Log.d(TAG, "ProgressInformation   :  $currentStep    $targetStep    $result")
        mViewBinding?.pbStep?.setProgress(result)
    }

    private fun initClickListenr() {
        mViewBinding!!.addBleDevice.setOnClickListener(this)
        mViewBinding!!.layoutUser.setOnClickListener(this)
        mViewBinding!!.recentDevice.setOnClickListener(this)
    }

    private fun initTabLayout() {
        tabTitle.forEachIndexed { index, s ->
            mViewBinding?.tabLayout?.newTab()?.let { mViewBinding?.tabLayout?.addTab(it) }
            mViewBinding?.tabLayout?.getTabAt(index)?.apply {
                val tabView = TextView(requireContext())
                tabView.layoutParams =
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                tabView.text = tabTitle[index]
                tabView.textSize = 12F
                //tabView.setTextColor(resources.getColor(R.color.color_7b7b7b))
                tabView.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.mtrl_tabs_legacy_text_color_selector))
                tabView.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                customView = tabView
                /*text = tabTitle[index]*/
                tag = index
            }
            mViewBinding?.tabLayout?.getTabAt(0)?.select()
        }
    }

    private fun initTabListener() {
        mViewBinding?.tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (0 == tab?.tag) {
                    mViewBinding?.deviceRecent?.visibility = View.VISIBLE
                    mViewBinding?.deviceList?.visibility = View.GONE
                } else {
                    mViewBinding?.deviceRecent?.visibility = View.GONE
                    mViewBinding?.deviceList?.visibility = View.VISIBLE
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
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
                    stepArray = iSportStepInterface!!.todaySportStepArray
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
        }, Activity.BIND_AUTO_CREATE)
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
        return String.format("%.2f", steps * 0.7f / 1000)
    }

    // 千卡路里计算公式
    private fun getCalorieByStep(steps: Long): String {
        return String.format("%.1f", steps * 0.7f * 60 * 1.036f / 1000)
    }

    inner class TodayStepCounterCall : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            when (msg.what) {
                REFRESH_STEP_WHAT -> {
                    //每隔500毫秒获取一次计步数据刷新UI
                    if (null != iSportStepInterface) {
                        try {
                            stepArray = iSportStepInterface!!.currentTimeSportStep
                            updataValues(stepArray)
                        } catch (e: RemoteException) {
                            e.printStackTrace()
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
            R.id.add_ble_device -> {
                ARouter.getInstance()
                    .build(ARouterConst.PATH_APP_BLE_DEVICELIST)
                    .navigation()
            }
            R.id.layout_user -> {
                activity?.let { PersonInformationActivity.starActivity(it) }
            }
            R.id.recent_device -> {
                when (device.deviceType) {
                    DeviceType.WEIGHT -> {
                        ARouter.getInstance()
                            .build(ARouterConst.PATH_APP_BLE_WEIGHTMEASURE)
                            .navigation()
                    }
                    DeviceType.WHEEL -> {
                        ARouter.getInstance()
                            .build(ARouterConst.PATH_APP_BLE_WHEELMEASURE)
                            .navigation()
                    }
                    else ->
                        ARouter.getInstance().build(ARouterConst.PATH_APP_BLE_COMMON)
                            .withInt("mode", BaseDeviceScan2ConnVM.WorkMode.MEASURE.ordinal)
                            .withInt("deviceType", device.deviceType.ordinal)
                            .navigation()
                }
            }
        }
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        initProgressRate()
        needNotify = if (!needNotify) {
            mDelayHandler.removeCallbacksAndMessages(null)
            true
        } else {
            mDelayHandler.removeCallbacksAndMessages(null)
            mDelayHandler.sendEmptyMessageDelayed(
                REFRESH_STEP_WHAT,
                TIME_INTERVAL_REFRESH
            )
            false
        }
    }

    override fun onPause() {
        super.onPause()
        if (!pauseResume) {
            mDelayHandler.removeCallbacksAndMessages(null)
            pauseResume = true
        }
    }

    override fun onResume() {
        super.onResume()
        if (pauseResume) {
            mDelayHandler.sendEmptyMessageDelayed(
                REFRESH_STEP_WHAT,
                TIME_INTERVAL_REFRESH
            )
            pauseResume = false
        }
    }

    override fun onDisconnected() {
        mViewBinding?.networkError?.visibility = View.VISIBLE
        mViewBinding?.mainLayout?.visibility = View.GONE
    }

    override fun onConnected(networkType: NetworkUtils.NetworkType?) {
        mViewBinding?.networkError?.visibility = View.GONE
        mViewBinding?.mainLayout?.visibility = View.VISIBLE
        mViewModel.loadDevice()
    }
}
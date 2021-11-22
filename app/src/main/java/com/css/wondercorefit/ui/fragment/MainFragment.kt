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
import com.blankj.utilcode.util.ToastUtils
import com.css.base.uibase.BaseFragment
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
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

    private var iSportStepInterface: ISportStepInterface? = null
    private var stepArray: Int = 0
    private val mDelayHandler = Handler(TodayStepCounterCall())
    private val REFRESH_STEP_WHAT = 0
    private val TIME_INTERVAL_REFRESH: Long = 500
    private lateinit var targetStep: String
    private var currentStep: Int = 0
    private var result: Float = 0.0f
    private lateinit var stepData: StepData
    private var needNotify: Boolean = false
    private var pauseResume: Boolean = false
    private lateinit var mMainDeviceAdapter: MainDeviceAdapter
    var mData = ArrayList<BondDeviceData>()
    private val tabTitle = arrayListOf("最近使用", "全部")

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(activity, 0f)
        SystemBarHelper.setHeightAndPadding(activity, mViewBinding?.topView)
        mViewBinding?.deviceList?.layoutManager = GridLayoutManager(activity, 2)
        mMainDeviceAdapter = MainDeviceAdapter(mData)
        mViewBinding?.deviceList?.adapter = mMainDeviceAdapter
        mMainDeviceAdapter.setOnItemClickListener {
            WonderCoreCache.saveData(CacheKey.RECENT_DEVICE, it)
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
        val userInfo = WonderCoreCache.getUserInfo()
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
        mViewModel.loadData(true)
        mViewBinding?.model = mViewModel
        mViewBinding?.lifecycleOwner = viewLifecycleOwner
    }


    private fun showDevice() {
        BondDeviceData.getDeviceLiveDataMerge().observe(viewLifecycleOwner) {
            //it为map，值为null表示删除，key为设备类型
            LogUtils.d("it-->$it")
            val devices = BondDeviceData.getDevices()
            mData.clear()
            mData.addAll(devices)
            mMainDeviceAdapter.setItems(mData)
            WonderCoreCache.saveData(CacheKey.RECENT_DEVICE,it.second)
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
                    val mUserData = WonderCoreCache.getUserInfo()
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
        val mUserData = WonderCoreCache.getUserInfo()
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
        mViewBinding!!.ivRec1.setOnClickListener(this)
        mViewBinding!!.ivRec2.setOnClickListener(this)
        mViewBinding!!.ivRec3.setOnClickListener(this)
        mViewBinding!!.ivRec4.setOnClickListener(this)
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
                tabView.setTextColor(
                    ContextCompat.getColorStateList(
                        requireContext(),
                        R.color.mtrl_tabs_legacy_text_color_selector
                    )
                )
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

    override fun initViewModel(): MainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)


    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentMainBinding = FragmentMainBinding.inflate(inflater, parent, false)

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
                val device = mViewModel.recentDevice.value!!
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
            R.id.iv_rec1 -> {
                playCourseVideo("https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-adc4b5ee-44b8-4314-843b-530100b99ca2.mp4")
            }
            R.id.iv_rec2 -> {
                playCourseVideo("https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-e708aa50-8bba-443d-88d9-711c1531224a.mp4")
            }
            R.id.iv_rec3 -> {
                playCourseVideo("https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-d50d9e2b-114d-443b-b6a1-4a82a1cf4fdd.mp4")
            }
            R.id.iv_rec4 -> {
                playCourseVideo("https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-52faf301-1575-4f8a-a3d9-c31cb5658515.mp4")
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

    private fun playCourseVideo(videoLink: String) {
        try {
            if (NetworkUtils.isConnected()) {
                ARouter.getInstance()
                    .build(ARouterConst.PATH_APP_MAIN_COURSE)
                    .with(Bundle().apply { putString("videoLink", videoLink) })
                    .navigation()
            } else {
                showNetworkErrorDialog();
            }
        } catch (e: ActivityNotFoundException) {
            ToastUtils.showShort("链接无效:${videoLink}")
        }
    }

    override fun onDisconnected() {
        mViewBinding?.networkError?.visibility = View.VISIBLE
        mViewBinding?.mainLayout?.visibility = View.GONE
    }

    override fun onConnected(networkType: NetworkUtils.NetworkType?) {
        mViewBinding?.networkError?.visibility = View.GONE
        mViewBinding?.mainLayout?.visibility = View.VISIBLE
        mViewModel.loadData()
    }
}
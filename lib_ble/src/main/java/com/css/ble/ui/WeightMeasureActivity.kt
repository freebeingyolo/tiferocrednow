package com.css.ble.ui

import android.os.Bundle
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.facade.callback.NavCallback
import com.alibaba.android.arouter.launcher.ARouter
import com.css.base.dialog.CommonAlertDialog
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.bean.WeightBondData
import com.css.ble.databinding.ActivityBleEntryBinding
import com.css.ble.ui.fragment.WeightMeasureBeginFragment
import com.css.ble.ui.fragment.WeightMeasureEndDeailFragment
import com.css.ble.utils.FragmentUtils
import com.css.ble.viewmodel.DeviceVMFactory
import com.css.ble.viewmodel.WeightMeasureVM
import com.css.service.router.ARouterConst
import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache
import razerdp.basepopup.BasePopupWindow


@Route(path = ARouterConst.PATH_APP_BLE_WEIGHTMEASURE)
class WeightMeasureActivity : BaseWeightActivity<WeightMeasureVM, ActivityBleEntryBinding>() {
    override val vmCls: Class<WeightMeasureVM> = WeightMeasureVM::class.java
    override val vbCls: Class<ActivityBleEntryBinding> = ActivityBleEntryBinding::class.java

    override fun enabledVisibleToolBar() = false

    override fun initViewModel(): WeightMeasureVM {
        return DeviceVMFactory.getViewModel(deviceType)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        if (WonderCoreCache.getData(CacheKey.LAST_WEIGHT_INFO, WeightBondData::class.java) == null) {
            FragmentUtils.changeFragment(WeightMeasureBeginFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
        } else {
            mViewModel.bondData.value = WonderCoreCache.getData(CacheKey.LAST_WEIGHT_INFO, WeightBondData::class.java)
            FragmentUtils.changeFragment(WeightMeasureEndDeailFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
        }
    }

    override fun onResume() {
        super.onResume()
        if (BondDeviceData.getDevice(deviceType) == null) {
            mViewModel.disconnect()
            CommonAlertDialog(this).apply {
                type = CommonAlertDialog.DialogType.Image
                imageResources = R.mipmap.icon_tick
                content = getString(R.string.please_bond_first)
                onDismissListener = object : BasePopupWindow.OnDismissListener() {
                    override fun onDismiss() {
                        mViewModel.disconnect()
                        ARouter.getInstance().build(ARouterConst.PATH_APP_BLE_DEVICELIST).navigation(context,
                            object : NavCallback() {
                                override fun onArrival(postcard: Postcard?) {
                                    finish()
                                }
                            })
                    }
                }
            }.show()
        }
    }
}
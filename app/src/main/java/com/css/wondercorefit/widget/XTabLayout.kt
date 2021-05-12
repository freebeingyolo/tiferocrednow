package com.css.wondercorefit.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.css.service.inner.BaseInner
import com.css.wondercorefit.R

class XTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(context, attrs, defStyleAttr), View.OnClickListener {
    private lateinit var mMainIcon: AppCompatImageView
    private lateinit var mMainName: AppCompatTextView

    private lateinit var mCourseIcon: AppCompatImageView
    private lateinit var mCourseName: AppCompatTextView

    private lateinit var mMallIcon: AppCompatImageView
    private lateinit var mMallName: AppCompatTextView

    private lateinit var mSettingIcon: AppCompatImageView
    private lateinit var mSettingName: AppCompatTextView

    private lateinit var mCallback: (Int) -> Unit
    private val badges = SparseArray<AppCompatImageView>()

    init {
        initUI()
    }

    private fun initUI() {
        setBackgroundColor(Color.WHITE)
        var view = LayoutInflater.from(context).inflate(R.layout.main_tab_view, this, true)
        val tabMain = view.findViewById<ConstraintLayout>(R.id.tab_main)
        mMainIcon = tabMain.findViewById(R.id.icon)
        mMainName = tabMain.findViewById(R.id.name)
        tabMain.setOnClickListener(this)
        val tabCourse = view.findViewById<ConstraintLayout>(R.id.tab_course)
        mCourseIcon = tabCourse.findViewById(R.id.icon)
        mCourseName = tabCourse.findViewById(R.id.name)
        tabCourse.setOnClickListener(this)
        val tabMall = view.findViewById<ConstraintLayout>(R.id.tab_mall)
        mMallIcon = tabMall.findViewById(R.id.icon)
        mMallName = tabMall.findViewById(R.id.name)
        tabMall.setOnClickListener(this)
        val tabSetting = view.findViewById<ConstraintLayout>(R.id.tab_setting)
        mSettingIcon = tabSetting.findViewById(R.id.icon)
        mSettingName = tabSetting.findViewById(R.id.name)
        tabSetting.setOnClickListener(this)
    }

    fun initTab(callback: (Int) -> Unit) {
        this.mCallback = callback
        initConfig(
            BaseInner.TabIndex.HOME,
            BaseInner.TabIndex.COURSE,
            BaseInner.TabIndex.MALL,
            BaseInner.TabIndex.SETTING,
        )
        tabSelected(BaseInner.TabIndex.HOME)
    }

    private fun initConfig(@BaseInner.TabIndex vararg tabIndex: Int) {
        for (tab in tabIndex) {
            changeTabUnclick(tab, true)
        }
    }

    /**
     * @param tabIndex 切换tab位置
     */
    fun tabSelected(@BaseInner.TabIndex tabIndex: Int) {
        if (badges == null) return
        for (pos in 0 until badges.size()) {
            val key = badges.keyAt(pos)
            if (key == tabIndex) {
                changeTabClick(key)
            } else {
                changeTabUnclick(key, false)
            }
        }
    }

    private fun changeTabUnclick(@BaseInner.TabIndex tab: Int, isInitConfig: Boolean) {
        changeTabStatus(tab, false, isInitConfig)
    }

    private fun changeTabClick(@BaseInner.TabIndex tab: Int) {
        changeTabStatus(tab, true, false)
    }

    private fun changeTabStatus(
        @BaseInner.TabIndex tab: Int,
        isClick: Boolean,
        isInitConfig: Boolean
    ) {
        if (tab == BaseInner.TabIndex.HOME) {
            if (isClick) {
                localClickConfig(
                    mMainIcon,
                    mMainName,
                    R.mipmap.icon_focused_home,
                    R.string.title_index_home
                )
            } else {
                localUnclickConfig(
                    mMainIcon,
                    mMainName,
                    R.mipmap.icon_unfocused_home,
                    R.string.title_index_home
                )
            }
            if (isInitConfig) {
                badges.put(tab, mMainIcon)
            }
        } else if (tab == BaseInner.TabIndex.COURSE) {
            if (isClick) {
                localClickConfig(
                    mCourseIcon,
                    mCourseName,
                    R.mipmap.icon_focused_course,
                    R.string.title_index_course
                )
            } else {
                localUnclickConfig(
                    mCourseIcon,
                    mCourseName,
                    R.mipmap.icon_unfocused_course,
                    R.string.title_index_course
                )
            }
            if (isInitConfig) {
                badges.put(tab, mCourseIcon)
            }
        } else if (tab == BaseInner.TabIndex.MALL) {
            if (isClick) {
                localClickConfig(
                    mMallIcon,
                    mMallName,
                    R.mipmap.icon_focused_mall,
                    R.string.title_index_mall
                )
            } else {
                localUnclickConfig(
                    mMallIcon,
                    mMallName,
                    R.mipmap.icon_unfocused_mall,
                    R.string.title_index_mall
                )
            }
            if (isInitConfig) {
                badges.put(tab, mMallIcon)
            }
        } else if (tab == BaseInner.TabIndex.SETTING) {
            if (isClick) {
                localClickConfig(
                    mSettingIcon,
                    mSettingName,
                    R.mipmap.icon_focused_settings,
                    R.string.title_index_setting
                )
            } else {
                localUnclickConfig(
                    mSettingIcon,
                    mSettingName,
                    R.mipmap.icon_unfocused_settings,
                    R.string.title_index_setting
                )
            }
            if (isInitConfig) {
                badges.put(tab, mSettingIcon)
            }
        }
        if (isClick) {
            mCallback.invoke(tab)
        }
    }

    private fun localUnclickConfig(
        icon: AppCompatImageView,
        name: AppCompatTextView,
        iconRes: Int,
        text: Int
    ) {
        name.visibility = View.VISIBLE
        icon.setImageResource(iconRes)
        name.setText(text)
        name.setTextColor(ContextCompat.getColor(context, R.color.tab_textcolor_unfocus))
    }

    private fun localClickConfig(
        icon: AppCompatImageView,
        name: AppCompatTextView,
        iconRes: Int,
        text: Int
    ) {
        doApngAnim(icon, iconRes)
        name.setText(text)
        name.setTextColor(ContextCompat.getColor(context, R.color.tab_textcolor_focus))
    }

    private fun doApngAnim(icon: AppCompatImageView, iconRes: Int) {
        icon.setImageResource(iconRes)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tab_main -> {
                tabSelected(BaseInner.TabIndex.HOME)
            }
            R.id.tab_course -> {
                tabSelected(BaseInner.TabIndex.COURSE)
            }
            R.id.tab_mall -> {
                tabSelected(BaseInner.TabIndex.MALL)
            }
            R.id.tab_setting -> {
                tabSelected(BaseInner.TabIndex.SETTING)
            }

        }
    }
}
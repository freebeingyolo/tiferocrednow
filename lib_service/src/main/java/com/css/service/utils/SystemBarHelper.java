/*
 * Copyright (c) 2016 BiliBili Inc.
 */

package com.css.service.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;


import com.css.service.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * 状态栏工具类
 * 状态栏两种模式(Android 4.4以上)
 * 1.沉浸式全屏模式
 * 2.状态栏着色模式
 */
public class SystemBarHelper {
    public static float DEFAULT_ALPHA = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 0.2f : 0.3f;

    /**
     * Android4.4以上的状态栏着色
     *
     * @param activity       Activity对象
     * @param statusBarColor 状态栏颜色
     */
    public static void tintStatusBar(Activity activity, @ColorInt int statusBarColor) {
        tintStatusBar(activity, statusBarColor, DEFAULT_ALPHA);
    }

    /**
     * Android4.4以上的状态栏着色
     *
     * @param activity       Activity对象
     * @param statusBarColor 状态栏颜色
     * @param alpha          透明栏透明度[0.0-1.0]
     */
    public static void tintStatusBar(Activity activity, @ColorInt int statusBarColor, @FloatRange(from = 0.0, to = 1.0) float alpha) {
        tintStatusBar(activity.getWindow(), statusBarColor, alpha);
    }

    /**
     * Android4.4以上的状态栏着色
     *
     * @param window         一般都是用于Activity的window,也可以是其他的例如Dialog,DialogFragment
     * @param statusBarColor 状态栏颜色
     */
    public static void tintStatusBar(Window window, @ColorInt int statusBarColor) {
        tintStatusBar(window, statusBarColor, DEFAULT_ALPHA);
    }

    /**
     * Android4.4以上的状态栏着色
     *
     * @param window         一般都是用于Activity的window,也可以是其他的例如Dialog,DialogFragment
     * @param statusBarColor 状态栏颜色
     * @param alpha          透明栏透明度[0.0-1.0]
     */
    public static void tintStatusBar(Window window, @ColorInt int statusBarColor, @FloatRange(from = 0.0, to = 1.0) float alpha) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        ViewGroup decorView = (ViewGroup) window.getDecorView();
        ViewGroup contentView = (ViewGroup) window.getDecorView().findViewById(Window.ID_ANDROID_CONTENT);
        View rootView = contentView.getChildAt(0);
        if (rootView != null) {
            ViewCompat.setFitsSystemWindows(rootView, true);
        }

        setStatusBar(decorView, statusBarColor, true);
        setTranslucentView(decorView, alpha);
    }

    /**
     * Android4.4以上的状态栏着色(针对于DrawerLayout)
     * 注:
     * 1.如果出现界面展示不正确,删除布局中所有fitsSystemWindows属性,尤其是DrawerLayout的fitsSystemWindows属性
     * 2.可以版本判断在5.0以上不调用该方法,使用系统自带
     *
     * @param activity       Activity对象
     * @param drawerLayout   DrawerLayout对象
     * @param statusBarColor 状态栏颜色
     */
    public static void tintStatusBarForDrawer(Activity activity, DrawerLayout drawerLayout, @ColorInt int statusBarColor) {
        tintStatusBarForDrawer(activity, drawerLayout, statusBarColor, DEFAULT_ALPHA);
    }

    /**
     * Android4.4以上的状态栏着色(针对于DrawerLayout)
     * 注:
     * 1.如果出现界面展示不正确,删除布局中所有fitsSystemWindows属性,尤其是DrawerLayout的fitsSystemWindows属性
     * 2.可以版本判断在5.0以上不调用该方法,使用系统自带
     *
     * @param activity       Activity对象
     * @param drawerLayout   DrawerLayout对象
     * @param statusBarColor 状态栏颜色
     * @param alpha          透明栏透明度[0.0-1.0]
     */
    public static void tintStatusBarForDrawer(Activity activity, DrawerLayout drawerLayout, @ColorInt int statusBarColor,
                                              @FloatRange(from = 0.0, to = 1.0) float alpha) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }

        Window window = activity.getWindow();
        ViewGroup decorView = (ViewGroup) window.getDecorView();
        ViewGroup drawContent = (ViewGroup) drawerLayout.getChildAt(0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            drawerLayout.setStatusBarBackgroundColor(statusBarColor);

            int systemUiVisibility = window.getDecorView().getSystemUiVisibility();
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            window.getDecorView().setSystemUiVisibility(systemUiVisibility);
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setStatusBar(decorView, statusBarColor, true, true);
        setTranslucentView(decorView, alpha);

        drawerLayout.setFitsSystemWindows(false);
        drawContent.setFitsSystemWindows(true);
        ViewGroup drawer = (ViewGroup) drawerLayout.getChildAt(1);
        drawer.setFitsSystemWindows(false);
    }

    /**
     * Android4.4以上的沉浸式全屏模式
     * 注:
     * 1.删除fitsSystemWindows属性:Android5.0以上使用该方法如果出现界面展示不正确,删除布局中所有fitsSystemWindows属性
     * 或者调用forceFitsSystemWindows方法
     * 2.不删除fitsSystemWindows属性:也可以区别处理,Android5.0以上使用自己的方式实现,不调用该方法
     *
     * @param activity Activity对象
     */
    public static void immersiveStatusBar(Activity activity) {
        immersiveStatusBar(activity, DEFAULT_ALPHA);
    }

    /**
     * Android4.4以上的沉浸式全屏模式
     * 注:
     * 1.删除fitsSystemWindows属性:Android5.0以上使用该方法如果出现界面展示不正确,删除布局中所有fitsSystemWindows属性
     * 或者调用forceFitsSystemWindows方法
     * 2.不删除fitsSystemWindows属性:也可以区别处理,Android5.0以上使用自己的方式实现,不调用该方法
     *
     * @param activity Activity对象
     * @param alpha    透明栏透明度[0.0-1.0]
     */
    public static void immersiveStatusBar(Activity activity, @FloatRange(from = 0.0, to = 1.0) float alpha) {
        immersiveStatusBar(activity.getWindow(), alpha);
    }

    /**
     * Android4.4以上的沉浸式全屏模式
     * 注:
     * 1.删除fitsSystemWindows属性:Android5.0以上使用该方法如果出现界面展示不正确,删除布局中所有fitsSystemWindows属性
     * 或者调用forceFitsSystemWindows方法
     * 2.不删除fitsSystemWindows属性:也可以区别处理,Android5.0以上使用自己的方式实现,不调用该方法
     *
     * @param window 一般都是用于Activity的window,也可以是其他的例如Dialog,DialogFragment
     */
    public static void immersiveStatusBar(Window window) {
        immersiveStatusBar(window, DEFAULT_ALPHA);
    }

    /**
     * Android4.4以上的沉浸式全屏模式
     * 注:
     * 1.删除fitsSystemWindows属性:Android5.0以上使用该方法如果出现界面展示不正确,删除布局中所有fitsSystemWindows属性
     * 或者调用forceFitsSystemWindows方法
     * 2.不删除fitsSystemWindows属性:也可以区别处理,Android5.0以上使用自己的方式实现,不调用该方法
     *
     * @param window 一般都是用于Activity的window,也可以是其他的例如Dialog,DialogFragment
     * @param alpha  透明栏透明度[0.0-1.0]
     */
    public static void immersiveStatusBar(Window window, @FloatRange(from = 0.0, to = 1.0) float alpha) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);

            int systemUiVisibility = window.getDecorView().getSystemUiVisibility();
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            window.getDecorView().setSystemUiVisibility(systemUiVisibility);
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        ViewGroup decorView = (ViewGroup) window.getDecorView();
        ViewGroup contentView = (ViewGroup) window.getDecorView().findViewById(Window.ID_ANDROID_CONTENT);
        View rootView = contentView.getChildAt(0);
        int statusBarHeight = getStatusBarHeight(window.getContext());
        if (rootView != null) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) rootView.getLayoutParams();
            ViewCompat.setFitsSystemWindows(rootView, true);
            lp.topMargin = -statusBarHeight;
            rootView.setLayoutParams(lp);
        }

        setTranslucentView(decorView, alpha);
    }

    /**
     * 设置状态栏darkMode,字体颜色及icon变黑(目前支持MIUI6以上,Flyme4以上,Android M以上)
     */
    public static void setStatusBarDarkMode(Activity activity) {
        setStatusBarDarkMode(activity.getWindow());
    }

    /**
     * 设置状态栏lightMode,字体颜色及icon变白(目前支持MIUI6以上,Flyme4以上,Android M以上)
     */
    public static void setStatusBarLightMode(Activity activity) {
        setStatusBarLightMode(activity.getWindow());
    }

    /**
     * 设置状态栏darkMode,字体颜色及icon变黑(目前支持MIUI6以上,Flyme4以上,Android M以上)
     */
    public static void setStatusBarDarkMode(Window window) {
        if (isFlyme4Later()) {
            setStatusBarDarkModeForFlyme4(window, true);
        } else if (isMIUIIn6To9()) {
            setStatusBarDarkModeForMIUI6(window, true);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setStatusBarDarkModeForM(window);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isOPPO()) {
            setStatusBarDarkModeForOPPO(window, true);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            tintStatusBar(window, Color.TRANSPARENT, 0.3f);
        }
    }

    /**
     * 设置状态栏lightMode,字体颜色及icon变白(目前支持MIUI6以上,Flyme4以上,Android M以上)
     */
    public static void setStatusBarLightMode(Window window) {
        if (isFlyme4Later()) {
            setStatusBarDarkModeForFlyme4(window, false);
        } else if (isMIUIIn6To9()) {
            setStatusBarDarkModeForMIUI6(window, false);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setStatusBarDarkModeForM(window, false);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isOPPO()) {
            setStatusBarDarkModeForOPPO(window, false);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            tintStatusBar(window, Color.TRANSPARENT, 0f);
        }
    }

    //------------------------->

    /**
     * android 6.0设置字体颜色
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static void setStatusBarDarkModeForM(Window window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);

        int systemUiVisibility = window.getDecorView().getSystemUiVisibility();
        systemUiVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        window.getDecorView().setSystemUiVisibility(systemUiVisibility);
    }


    /**
     * android 6.0设置字体颜色 可控制深色或浅色
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static void setStatusBarDarkModeForM(Window window, boolean dark) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);

        int systemUiVisibility = window.getDecorView().getSystemUiVisibility();
        if (dark) {
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            systemUiVisibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        window.getDecorView().setSystemUiVisibility(systemUiVisibility);
    }

    /**
     * 设置Flyme4+的darkMode,darkMode时候字体颜色及icon变黑
     * http://open-wiki.flyme.cn/index.php?title=Flyme%E7%B3%BB%E7%BB%9FAPI
     */
    public static boolean setStatusBarDarkModeForFlyme4(Window window, boolean dark) {
        boolean result = false;
        if (window != null) {
            try {
                WindowManager.LayoutParams e = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(e);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }

                meizuFlags.setInt(e, value);
                window.setAttributes(e);
                result = true;
            } catch (Exception var8) {
                Log.e("StatusBar", "setStatusBarDarkIcon: failed");
            }
        }

        return result;
    }

    /**
     * 设置oppo的状态栏是否为darkMode,darkMode时候字体颜色及icon变黑 http://dev.xiaomi.com/doc/p=4769/
     */
    private static void setStatusBarDarkModeForOPPO(Window window, boolean darkMode) {
        if (window == null) {
            return;
        }
        final int SYSTEM_UI_FLAG_OP_STATUS_BAR_TINT = 0x00000010;

        int vis = window.getDecorView().getSystemUiVisibility();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (darkMode) {
                vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (darkMode) {
                vis |= SYSTEM_UI_FLAG_OP_STATUS_BAR_TINT;
            } else {
                vis &= ~SYSTEM_UI_FLAG_OP_STATUS_BAR_TINT;
            }
        }
        window.getDecorView().setSystemUiVisibility(vis);
    }

    /**
     * 设置MIUI6+的状态栏是否为darkMode,darkMode时候字体颜色及icon变黑 https://open.oppomobile.com/wiki/doc#id=10161
     */
    private static void setStatusBarDarkModeForMIUI6(Window window, boolean darkmode) {
        Class<? extends Window> clazz = window.getClass();
        try {
            int darkModeFlag = 0;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(window, darkmode ? darkModeFlag : 0, darkModeFlag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建假的状态栏View
     */
    private static void setStatusBar(ViewGroup container, @ColorInt int statusBarColor, boolean visible, boolean addToFirst) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View statusBarView = container.findViewById(R.id.statusbar_view);
            if (statusBarView == null) {
                statusBarView = new View(container.getContext());
                statusBarView.setId(R.id.statusbar_view);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(container.getContext()));
                if (addToFirst) {
                    container.addView(statusBarView, 0, lp);
                } else {
                    container.addView(statusBarView, lp);
                }
            }

            statusBarView.setBackgroundColor(statusBarColor);
            statusBarView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 创建假的状态栏View
     */
    private static void setStatusBar(ViewGroup container, @ColorInt int statusBarColor, boolean visible) {
        setStatusBar(container, statusBarColor, visible, false);
    }

    /**
     * 创建假的透明栏
     */
    private static void setTranslucentView(ViewGroup container,
                                           @FloatRange(from = 0.0, to = 1.0) float alpha) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View translucentView = container.findViewById(R.id.translucent_view);
            if (translucentView == null) {
                translucentView = new View(container.getContext());
                translucentView.setId(R.id.translucent_view);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(container.getContext()));
                container.addView(translucentView, lp);
            }

            translucentView.setBackgroundColor(Color.argb((int) (alpha * 255), 0, 0, 0));
        }
    }


    /**
     * 获取状态栏高度
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            result = context.getResources().getDimensionPixelSize(resId);
        }
        return result;
    }

    /**
     * 判断是否Flyme4以上
     */
    public static boolean isFlyme4Later() {
        return Build.FINGERPRINT.contains("Flyme_OS_4")
                || Build.VERSION.INCREMENTAL.contains("Flyme_OS_4")
                || Pattern.compile("Flyme OS [4|5]", Pattern.CASE_INSENSITIVE).matcher(Build.DISPLAY).find()
                || Build.FINGERPRINT.contains("Flyme_6");
    }

    /**
     * 判断是否为MIUI6以上
     */
    public static boolean isMIUIIn6To9() {
        try {
            Class<?> clz = Class.forName("android.os.SystemProperties");
            Method mtd = clz.getMethod("get", String.class);
            String val = (String) mtd.invoke(null, "ro.miui.ui.version.name");
            val = val.replaceAll("[vV]", "");
            int version = Integer.parseInt(val);
            return version >= 6 && version < 9;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isOPPO() {
        return Build.BRAND.toLowerCase().contains("oppo");
    }

    /**
     * 增加View的高度以及paddingTop,增加的值为状态栏高度.一般是在沉浸式全屏给ToolBar用的
     */
    public static void setHeightAndPadding(Context context, View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.height += getStatusBarHeight(context);//增高
            view.setPadding(view.getPaddingLeft(), view.getPaddingTop() + getStatusBarHeight(context),
                    view.getPaddingRight(), view.getPaddingBottom());
        }
    }

    /**
     * 增加View的paddingTop,增加的值为状态栏高度
     */
    public static void setPadding(Context context, View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.setPadding(view.getPaddingLeft(), view.getPaddingTop() + getStatusBarHeight(context),
                    view.getPaddingRight(), view.getPaddingBottom());
        }
    }

    /**
     * 强制rootView下面的子View的FitsSystemWindows为false
     */
    public static void forceFitsSystemWindows(Activity activity) {
        forceFitsSystemWindows(activity.getWindow());
    }

    /**
     * 强制rootView下面的子View的FitsSystemWindows为false
     */
    public static void forceFitsSystemWindows(Window window) {
        forceFitsSystemWindows((ViewGroup) window.getDecorView().findViewById(Window.ID_ANDROID_CONTENT));
    }

    /**
     * 强制rootView下面的子View的FitsSystemWindows为false
     */
    public static void forceFitsSystemWindows(ViewGroup viewGroup) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                View view = viewGroup.getChildAt(i);
                if (view instanceof ViewGroup) {
                    forceFitsSystemWindows((ViewGroup) view);
                } else {
                    if (ViewCompat.getFitsSystemWindows(view)) {
                        ViewCompat.setFitsSystemWindows(view, false);
                    }
                }
            }
        }
    }

    public void setHideVirtualKey(Window window) {
        //保持布局状态
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                //布局位于状态栏下方
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                //全屏
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                //隐藏导航栏
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= 19) {
            uiOptions |= 0x00001000;
        } else {
            uiOptions |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        }
        window.getDecorView().setSystemUiVisibility(uiOptions);
    }

    /**
     * 虚拟按键是否显示
     * @param activity Activity
     * @return 是否显示
     */
    public static boolean isNavigationBarShow(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            return realSize.y != size.y;
        } else {
            boolean menu = ViewConfiguration.get(activity).hasPermanentMenuKey();
            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            return !menu && !back;
        }
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    public static void hideSystemNavigationBar(Activity activity) {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View view = activity.getWindow().getDecorView();
            view.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View decorView = activity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    public static void showSystemNavigationBar(Activity activity) {
        //显示虚拟按键
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            //低版本sdk
            View v = activity.getWindow().getDecorView();
            v.setSystemUiVisibility(View.VISIBLE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View decorView = activity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
}
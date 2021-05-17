package com.css.base.utils;

import android.util.Log;

import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.css.base.uibase.base.BaseWonderFragment;

/**
 * author Ruis
 * date 2019/11/6
 */
public class FragmentStarter {

    public static BaseWonderFragment pushFragmentToBackStack(FragmentActivity activity, @IdRes int containerId, Class<? extends BaseWonderFragment> cls, Object data, boolean isAddStatck) {
        ExceptionUtils.checkNotNull(cls == null, " fragment is null");
        try {
            String fragmentTag = cls.toString();
            FragmentManager fm = activity.getSupportFragmentManager();

            log("before operate, stack entry count: " + fm.getBackStackEntryCount());

            BaseWonderFragment fragment = (BaseWonderFragment) fm.findFragmentByTag(fragmentTag);
            if (fragment == null) {
                fragment = cls.newInstance();
            }
            fragment.onEnterWithData(data);

            FragmentTransaction ft = fm.beginTransaction();
            if (fragment.isAdded()) {
                log(fragmentTag + " has been added, will be shown again.");
                ft.show(fragment);
            } else {
                log(fragmentTag + " is added.");

                ft.add((containerId != 0) ? containerId : android.R.id.content, fragment, fragmentTag);
            }
            if(isAddStatck) {
                ft.addToBackStack(fragmentTag);
            }
            ft.commitAllowingStateLoss();
            return fragment;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        return null;
    }

    public static void popTopFragment(FragmentActivity activity, Class<? extends BaseWonderFragment> cls) {
        FragmentManager fm = activity.getSupportFragmentManager();
        fm.popBackStackImmediate();

    }


    public static void goToFragment(FragmentActivity activity, Class<? extends BaseWonderFragment> cls) {
        activity.getSupportFragmentManager().popBackStackImmediate(cls.toString(), 0);
    }

    public static void startFragment(Fragment fromFragment, Fragment toFragment, String tag) {
        FragmentTransaction fragmentTransaction = fromFragment.getParentFragmentManager().beginTransaction();
        fragmentTransaction.hide(fromFragment).add(android.R.id.content, toFragment, tag);
        fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.commitAllowingStateLoss();
    }

    private static void log(String msg) {
        if (UICoreConfig.INSTANCE.getMode()) {
            Log.i("FragmentStarter", "FragmentStarter:" + msg);
        }
    }

}

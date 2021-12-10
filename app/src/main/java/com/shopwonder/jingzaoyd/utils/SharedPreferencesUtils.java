package com.shopwonder.jingzaoyd.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 启动的时候用applaction context进行初始化,然后用单利模式进行调用
 */

public class SharedPreferencesUtils
{
    private static SharedPreferences mSharedPreferences;    // 单例
    private static SharedPreferencesUtils instance;         // 单例

    private SharedPreferencesUtils(Context context)
    {
        mSharedPreferences = context.getSharedPreferences("com.shopwonder.jingzaoyd", Context.MODE_PRIVATE);
    }

    /**
     * 初始化单例
     * @param context
     */
    public static synchronized void init(Context context)
    {
        if (instance == null)
        {
            instance = new SharedPreferencesUtils(context);
        }
    }

    /**
     * 获取单例
     *
     * @return
     */
    public static SharedPreferencesUtils getInstance()
    {
        if (instance == null)
        {
            throw new RuntimeException("class should init!");
        }
        return instance;
    }

    /**
     * 保存数据
     *
     * @param key
     * @param data
     */
    public void saveData(String key, Object data)
    {
        if (data == null)
        {
            return;
        }

        Class<?> tClass = data.getClass();
        if (tClass == null)
        {
            return;
        }

        String type = tClass.getSimpleName();
        Editor editor = mSharedPreferences.edit();

        if (type == null || editor == null)
        {
            return;
        }

        if ("Integer".equals(type))
        {
            editor.putInt(key, (Integer) data);
        }
        else if ("Boolean".equals(type))
        {
            editor.putBoolean(key, (Boolean) data);
        }
        else if ("String".equals(type))
        {
            editor.putString(key, (String) data);
        }
        else if ("Float".equals(type))
        {
            editor.putFloat(key, (Float) data);
        }
        else if ("Long".equals(type))
        {
            editor.putLong(key, (Long) data);
        }

        editor.commit();
    }

    /**
     * 删除指定数据
     * @param key
     */
    public void remove(String key)
    {
        Editor editor = mSharedPreferences.edit();
        if (editor == null)
        {
            return;
        }

        editor.remove(key);
        editor.commit();
    }

    /**
     * 清空所有数据
     */
    public void clear()
    {
        Editor editor = mSharedPreferences.edit();
        if (editor == null)
        {
            return;
        }

        editor.clear();
        editor.commit();
    }

    public String getString(String key, String defalutValue)
    {
        if (getData(key, defalutValue) == null)
        {
            return defalutValue;
        }
        return getData(key, defalutValue).toString();
    }

    public int getInt(String key, int defalutValue)
    {
        if (getData(key, defalutValue) == null)
        {
            return defalutValue;
        }
        return (Integer) getData(key, defalutValue);
    }

    public boolean getBoolean(String key, boolean defalutValue)
    {
        if (getData(key, defalutValue) == null)
        {
            return defalutValue;
        }
        return (Boolean) getData(key, defalutValue);
    }

    public float getFloat(String key, float defalutValue)
    {
        if (getData(key, defalutValue) == null)
        {
            return defalutValue;
        }
        return (Float) getData(key, defalutValue);
    }

    public long getLong(String key, long defalutValue)
    {
        if (getData(key, defalutValue) == null)
        {
            return defalutValue;
        }
        return (Long) getData(key, defalutValue);
    }

    /**
     * 得到数据
     *
     * @param key
     * @param defValue
     * @return
     */
    private Object getData(String key, Object defValue)
    {
        if (defValue == null)
        {
            return null;
        }

        Class<?> tClass = defValue.getClass();
        if (tClass == null)
        {
            return null;
        }

        String type = tClass.getSimpleName();
        if (type == null)
        {
            return null;
        }

        if ("Integer".equals(type))
        {
            return mSharedPreferences.getInt(key, (Integer) defValue);
        }
        else if ("Boolean".equals(type))
        {
            return mSharedPreferences.getBoolean(key, (Boolean) defValue);
        }
        else if ("String".equals(type))
        {
            return mSharedPreferences.getString(key, (String) defValue);
        }
        else if ("Float".equals(type))
        {
            return mSharedPreferences.getFloat(key, (Float) defValue);
        }
        else if ("Long".equals(type))
        {
            return mSharedPreferences.getLong(key, (Long) defValue);
        }

        return null;
    }
}

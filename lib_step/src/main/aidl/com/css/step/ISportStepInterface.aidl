// ISportStepInterface.aidl
package com.css.step;

// Declare any non-default types here with import statements

interface ISportStepInterface {
    /**
     * 获取当前时间运动步数
     */
     int getCurrentTimeSportStep();

     /**
      * 获取当天步数列表，json格式
      */
     int getTodaySportStepArray();
}
package com.css.ble.bean;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 蓝牙发送类型
 * @author yuedong
 * @date 2021-06-10
 */
@IntDef({
        BleSendType.readCharacteristic,
        BleSendType.writeCharacteristic,
        BleSendType.readRssi,
        BleSendType.setNotify,
})
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface BleSendType {
    int readCharacteristic = 1;
    int writeCharacteristic = 2;
    int readRssi = 3;
    int setNotify = 4;
}

package com.css.ble.utils;

public class DataUtils {

    /**
     * 将int转为高字节在前，低字节在后的byte数组（大端）
     *
     * @param n int
     * @return byte[]
     */
    public static byte[] intToByteBig(int n) {
        byte[] b = new byte[4];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) (n >> (8 * (b.length - 1 - i)) & 0xff);//&0xff表示只取一个字节
        }
        return b;
    }

    /**
     * 将int转为低字节在前，高字节在后的byte数组（小端）
     *
     * @param n int
     * @return byte[]
     */
    public static byte[] intToByteLittle(int n) {
        byte[] b = new byte[4];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) (n >> (8 * i) & 0xff);//&0xff表示只取一个字节
        }
        return b;
    }

    /**
     * byte数组到int的转换(小端)
     *
     * @param bytes
     * @return
     */
    public static int bytes2IntLittle(byte[] bytes) {
        int ret = 0;
        int count = 4;
        for (int i = 0; i < count; i++) {
            ret |= ((short) bytes[i] & 0xff) << (8 * i);
        }
        return ret;
    }

    /**
     * byte数组到int的转换(大端)
     * 0x1234 存储方式
     * 0 - 0x12
     * 1 - 0x34
     * @param bytes
     * @return
     */
    public static int bytes2IntBig(byte... bytes) {
        int ret = 0;
        int count = 4;
        for (int i = 0; i < count; i++) {
            ret |= ((short) bytes[i] & 0xff) << (8 * (count - 1 - i));
        }
        return ret;
    }

    /**
     * 将short转为高字节在前，低字节在后的byte数组（大端）
     *
     * @param n short
     * @return byte[]
     */
    public static byte[] shortToByteBig(short n) {
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) (n >> (8 * (b.length - 1 - i)) & 0xff);//&0xff表示只取一个字节
        }
        return b;
    }

    /**
     * 将short转为低字节在前，高字节在后的byte数组(小端)
     *
     * @param n short
     * @return byte[]
     */
    public static byte[] shortToByteLittle(short n) {
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) (n >> (8 * i) & 0xff);//&0xff表示只取一个字节
        }
        return b;
    }

    /**
     * 读取小端byte数组为short
     *
     * @param b
     * @return
     */
    public static short byteToShortLittle(byte[] b) {
        short ret = 0;
        int count = 2;
        for (int i = 0; i < count; i++) {
            ret |= ((short) b[i] & 0xff) << (8 * (i));
        }
        return ret;
    }

    /**
     * 读取大端byte数组为short
     *
     * @param b
     * @return
     */
    public static short byteToShortBig(byte[] b) {
        short ret = 0;
        int count = 2;
        for (int i = 0; i < count; i++) {
            ret |= ((short) b[i] & 0xff) << (8 * (count - 1 - i));
        }
        return ret;
    }

    /**
     * long类型转byte[] (大端)
     *
     * @param n
     * @return
     */
    public static byte[] longToBytesBig(long n) {
        byte[] b = new byte[8];
        for (int i = 0; i < 8; i++) {
            b[i] = (byte) (n >> (8 * (8 - 1 - i)) & 0xff);
        }
        return b;
    }

    /**
     * long类型转byte[] (小端)
     *
     * @param n
     * @return
     */
    public static byte[] longToBytesLittle(long n) {
        byte[] b = new byte[8];
        for (int i = 0; i < 8; i++) {
            b[i] = (byte) (n >> (8 * i) & 0xff);//&0xff表示只取一个字节
        }
        return b;
    }

    /**
     * byte[]转long类型(小端)
     * 0x1234
     * 3412
     *
     * @param array
     * @return
     */
    public static long bytesToLongLittle(byte[] array) {
        long ret = 0;
        for (int i = 0; i < 8; i++) {
            ret |= ((long) array[i] & 0xff) << (8 * i);
        }
        return ret;
    }

    /**
     * byte[]转long类型(大端)
     *
     * @param array
     * @return
     */
    public static long bytesToLongBig(byte[] array) {
        long ret = 0;
        for (int i = 0; i < 8; i++) {
            ret |= ((long) array[i] & 0xff) << (8 * (8 - 1 - i));
        }
        return ret;
    }
}

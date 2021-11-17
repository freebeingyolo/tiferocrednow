package com.css.ble.utils;

public class DataUtils {

    /**
     * 将int转为高字节在前，低字节在后的byte数组（大端）
     *
     * @param n int
     * @return byte[]
     */

    public static byte[] intToByteBig(int n, int len) {
        byte[] bytes = new byte[Math.min(4, len)];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (n >> (8 * (bytes.length - 1 - i)) & 0xff);//&0xff表示只取一个字节
        }
        return bytes;
    }

    public static byte[] intToByteBig(int n) {
        return intToByteBig(n, 4);
    }

    /**
     * 将int转为低字节在前，高字节在后的byte数组（小端）
     *
     * @param n int
     * @return byte[]
     */
    public static byte[] intToByteLittle(int n, int len) {
        byte[] bytes = new byte[Math.min(len, 4)];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (n >> (8 * i) & 0xff);//&0xff表示只取一个字节
        }
        return bytes;
    }

    public static byte[] intToByteLittle(int n) {
        return intToByteLittle(n, 4);
    }

    /**
     * byte数组到int的转换(小端)
     *
     * @param bytes
     * @return
     */
    public static int bytes2IntLittle(byte[] bytes) {
        int ret = 0;
        for (int i = 0; i < bytes.length; i++) {
            ret |= ((short) bytes[i] & 0xff) << (8 * i);
        }
        return ret;
    }

    /**
     * byte数组到int的转换(大端)
     * 0x1234 存储方式
     * 0 - 0x12
     * 1 - 0x34
     *
     * @param bytes
     * @return
     */
    public static int bytes2IntBig(byte... bytes) {
        int ret = 0;
        for (int i = 0; i < bytes.length; i++) {
            ret |= ((short) bytes[i] & 0xff) << (8 * (bytes.length - 1 - i));
        }
        return ret;
    }

    /**
     * 将short转为高字节在前，低字节在后的byte数组（大端）
     *
     * @param n short
     * @return byte[]
     */
    public static byte[] shortToByteBig(short n, int len) {
        byte[] bytes = new byte[Math.min(len, 2)];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (n >> (8 * (bytes.length - 1 - i)) & 0xff);//&0xff表示只取一个字节
        }
        return bytes;
    }

    public static byte[] shortToByteBig(short n) {
        return shortToByteBig(n, 2);
    }

    /**
     * 将short转为低字节在前，高字节在后的byte数组(小端)
     *
     * @param n short
     * @return byte[]
     */
    public static byte[] shortToByteLittle(short n, int len) {
        byte[] bytes = new byte[Math.min(len, 2)];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (n >> (8 * i) & 0xff);//&0xff表示只取一个字节
        }
        return bytes;
    }

    public static byte[] shortToByteLittle(short n) {
        return shortToByteLittle(n, 2);
    }

    /**
     * 读取小端byte数组为short
     *
     * @param bytes
     * @return
     */
    public static short byteToShortLittle(byte[] bytes) {
        short ret = 0;
        for (int i = 0; i < bytes.length; i++) {
            ret |= ((short) bytes[i] & 0xff) << (8 * (i));
        }
        return ret;
    }

    /**
     * 读取大端byte数组为short
     *
     * @param bytes
     * @return
     */
    public static short byteToShortBig(byte[] bytes) {
        short ret = 0;
        for (int i = 0; i < bytes.length; i++) {
            ret |= ((short) bytes[i] & 0xff) << (8 * (bytes.length - 1 - i));
        }
        return ret;
    }

    /**
     * long类型转byte[] (大端)
     *
     * @param n
     * @return
     */
    public static byte[] longToBytesBig(long n, int len) {
        byte[] bytes = new byte[Math.min(8, len)];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (n >> (8 * (bytes.length - 1 - i)) & 0xff);
        }
        return bytes;
    }

    public static byte[] longToBytesBig(long n) {
        return longToBytesBig(n, 8);
    }

    /**
     * long类型转byte[] (小端)
     *
     * @param n
     * @return
     */
    public static byte[] longToBytesLittle(long n, int len) {
        byte[] bytes = new byte[Math.min(len, 8)];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (n >> (8 * i) & 0xff);//&0xff表示只取一个字节
        }
        return bytes;
    }

    public static byte[] longToBytesLittle(long n) {
        return longToBytesLittle(n, 8);
    }

    /**
     * byte[]转long类型(小端)
     * 0x1234
     * 3412
     *
     * @param bytes
     * @return
     */
    public static long bytesToLongLittle(byte[] bytes) {
        long ret = 0;
        for (int i = 0; i < bytes.length; i++) {
            ret |= ((long) bytes[i] & 0xff) << (8 * i);
        }
        return ret;
    }

    /**
     * byte[]转long类型(大端)
     *
     * @param bytes
     * @return
     */
    public static long bytesToLongBig(byte[] bytes) {
        long ret = 0;
        for (int i = 0; i < bytes.length; i++) {
            ret |= ((long) bytes[i] & 0xff) << (8 * (bytes.length - 1 - i));
        }
        return ret;
    }

    public static String byte2HexStr(byte[] b) {
        if (b == null) {
            return "";
        } else {
            StringBuilder hs = new StringBuilder();
            byte[] var3 = b;
            int var4 = b.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                byte aB = var3[var5];
                int a = aB & 255;
                String stmp = Integer.toHexString(a);
                if (stmp.length() == 1) {
                    hs.append("0").append(stmp);
                } else {
                    hs.append(stmp);
                }

                hs.append(" ");
            }

            return hs.toString();
        }
    }
}

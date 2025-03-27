package org.app.common.utils;

import org.openjdk.jmh.annotations.Param;

public class ByteUtils {
    public static final int SMALL_SIZE = 1024;      // 🥳
    public static final int MEDIUM_SIZE = 2048;     // 😯
    public static final int MEDIUM_RARE = 4096;     // 🤡
    public static final int LARGE_SIZE = 8192;      // 😨
    public static final int LARGE_RARE = 16384;     // 🫣
    public static final int OMG_SIZE = 536870912;   // 😱

    @Param(value = {"1024", "2048", "4096", "8192", "16384", "536870912"})
    public static int size;
}

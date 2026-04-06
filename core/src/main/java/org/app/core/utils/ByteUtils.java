package org.app.core.utils;

import lombok.NoArgsConstructor;

// can extend to develop more methods
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public abstract class ByteUtils {
    public static final int SMALL_SIZE = 1024;      // 🥳
    public static final int MEDIUM_SIZE = 2048;     // 😯
    public static final int MEDIUM_RARE = 4096;     // 🤡
    public static final int LARGE_SIZE = 8192;      // 😨
    public static final int LARGE_RARE = 16384;     // 🫣
    public static final int OMG_SIZE = 536870912;   // 😱
}

package com.nick.libffmpeg;

import android.os.Build;

class CpuArchHelper {

    static CpuArch getCpuArch() {
        String abi = null;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            abi = Build.CPU_ABI;
        } else {
            abi = Build.SUPPORTED_ABIS[0];
        }
        if (abi == null || abi.length() == 0) {
            return CpuArch.NONE;
        }
        if (abi.equals("armeabi")) {
            return CpuArch.ARMv7;
        }
        if (abi.equals("armeabi-v7a") || abi.equals("arm64-v8a")) {
            return CpuArch.ARMv7_NEON;
        }
        if (abi.contains("x86")) {
            return CpuArch.x86;
        }

        return CpuArch.NONE;
    }

    static String getx86CpuAbi() {
        return "x86";
    }

    static String getArmeabiv7CpuAbi() {
        return "armeabi-v7a";
    }
}

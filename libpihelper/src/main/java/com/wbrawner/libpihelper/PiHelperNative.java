package com.wbrawner.libpihelper;

public class PiHelperNative {

    public static native void initConfig();

    public static native void readConfig(String configPath);

    public static native String getHost();

    public static native void setHost(String host);

    public static native String getApiKey();

    public static native void setApiKey(String apiKey);

    public static native void setPassword(String password);

    public static native void saveConfig(String configPath);

    public static native void cleanup();

    public static native int getStatus();

    public static native int enable();

    private static native int disable(String seconds);

    public static int disable(Long seconds) {
        if (seconds == null) {
            return disable("");
        } else {
            return disable(String.valueOf(seconds.longValue()));
        }
    }

    static {
        System.loadLibrary("pihelper-android");
    }
}

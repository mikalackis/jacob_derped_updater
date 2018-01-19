package com.invictrixrom.updater;

public class Delta {
    public static native boolean patch(String source, String delta, String out);

    static {
        System.loadLibrary("invupdater");
    }
}

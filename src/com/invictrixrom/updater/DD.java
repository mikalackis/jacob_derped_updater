package com.invictrixrom.updater;

public class DD {
    public static native boolean dd(String input, String output);

    static {
        System.loadLibrary("invdd");
    }
}

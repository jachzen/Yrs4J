package at.yrs4j.libnative.linux;

import at.yrs4j.api.LibLoader;
import at.yrs4j.api.YrsLibNativeInterface;
import com.sun.jna.Native;

import java.io.File;
import java.io.IOException;

public class AndroidLibLoader implements LibLoader {
    private static volatile YrsLibNativeInterface instance;

    public static AndroidLibLoader create() {
        return new AndroidLibLoader();
    }

    @Override
    public YrsLibNativeInterface get() {
        return load();
    }

    private static synchronized YrsLibNativeInterface load() {
        if (instance == null) {
            System.loadLibrary("yrs"); // loads libyrs.so
            instance = Native.load("yrs", YrsLibNativeInterface.class);
        }
        return instance;
    }
}
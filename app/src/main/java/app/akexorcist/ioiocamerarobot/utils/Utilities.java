package app.akexorcist.ioiocamerarobot.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Akexorcist on 9/5/15 AD.
 */
public class Utilities {
    public static String getCurrentIP(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        Method[] wmMethods = wifi.getClass().getDeclaredMethods();
        for (Method method : wmMethods) {
            if (method.getName().equals("isWifiApEnabled")) {

                try {
                    if (method.invoke(wifi).toString().equals("false")) {
                        WifiInfo wifiInfo = wifi.getConnectionInfo();
                        int ipAddress = wifiInfo.getIpAddress();
                        return (ipAddress & 0xFF) + "." +
                                ((ipAddress >> 8) & 0xFF) + "." +
                                ((ipAddress >> 16) & 0xFF) + "." +
                                ((ipAddress >> 24) & 0xFF);
                    } else if (method.invoke(wifi).toString().equals("true")) {
                        return "192.168.43.1";
                    }
                } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return "Unknown";
    }
}

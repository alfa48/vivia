package ao.easy.vvia.controllers;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import ao.easy.vvia.interfaces.ResourceHandler;

public class WifiHandler implements ResourceHandler {
    @Override
    public void execute(Context context, double value) {
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        boolean enable = value > 0;
        wifi.setWifiEnabled(enable);
        Log.d("WifiHandler", "Wifi enabled: " + enable);
    }
}

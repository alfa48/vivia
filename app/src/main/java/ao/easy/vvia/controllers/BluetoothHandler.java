package ao.easy.vvia.controllers;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresPermission;

import java.util.logging.Handler;

import ao.easy.vvia.interfaces.ResourceHandler;

public class BluetoothHandler implements ResourceHandler {
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public void execute(Context context, double value) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        if (adapter == null) {
            Log.d("BluetoothHandler", "Bluetooth não disponível");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("BluetoothHandler", "Permissão BLUETOOTH_CONNECT não concedida");
           /* new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, "Permissão necessária para controlar o Bluetooth", Toast.LENGTH_SHORT).show()
            );*/
            return;
        }

        boolean enable = value > 0;
        if (enable && !adapter.isEnabled()) adapter.enable();
        else if (!enable && adapter.isEnabled()) adapter.disable();

        Log.d("BluetoothHandler", "Bluetooth " + (enable ? "ativado" : "desativado"));
    }




}

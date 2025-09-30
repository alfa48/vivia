package ao.easy.vvia.controllers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import ao.easy.vvia.interfaces.ResourceHandler;

public class BrightnessHandler  implements ResourceHandler {
    @Override
    public void execute(Context context, int value) {
        if (Settings.System.canWrite(context)) {
            int brightness = Math.max(0, Math.min(value, 255));
            Settings.System.putInt(
                    context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    brightness
            );
        } else {
            Toast.makeText(context, "Permissão necessária para alterar brilho", Toast.LENGTH_SHORT).show();
            if (!Settings.System.canWrite(context)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);
            }
        }
    }
}

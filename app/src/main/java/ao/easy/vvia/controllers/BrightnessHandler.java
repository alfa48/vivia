package ao.easy.vvia.controllers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import ao.easy.vvia.interfaces.ResourceHandler;

public class BrightnessHandler  implements ResourceHandler {
    @Override
    public void execute(Context context, double value) {
        if (Settings.System.canWrite(context)) {
                int brightness = (int) Math.max(0, Math.min(value, 255));
                Log.d("Action", "Setting brightness. Value: " + brightness);

                // Define modo manual
                Settings.System.putInt(
                    context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                );

                // Altera valor do sistema
                Settings.System.putInt(
                    context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    brightness
                );

                // Atualiza a tela atual
                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
                    layoutParams.screenBrightness = brightness / 255f;
                    activity.getWindow().setAttributes(layoutParams);
                }
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

package ao.easy.vvia.dispatcher;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import ao.easy.vvia.controllers.BrightnessHandler;
import ao.easy.vvia.controllers.VolumeHandler;
import ao.easy.vvia.interfaces.ResourceHandler;
import ao.easy.vvia.models.IAResponse;

public class ActionDispatcher {
    private final Map<String, ResourceHandler> handlers = new HashMap<>();

    public ActionDispatcher() {
        handlers.put("brightness", new BrightnessHandler());
        handlers.put("volume", new VolumeHandler());
    }

    public void dispatch(Context context, IAResponse response) {
        ResourceHandler handler = handlers.get(response.getTarget());
        if (handler != null) {
            handler.execute(context, response.getValue());
            new Handler(Looper.getMainLooper()).post(() -> {
                Toast.makeText(context, "Ação executada: " + response.getTarget(), Toast.LENGTH_SHORT).show();
            });

        } else {
            new Handler(Looper.getMainLooper()).post(() -> {
                Toast.makeText(context, "Ação não suportada: " + response.getTarget(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}

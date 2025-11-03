package ao.easy.vvia.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ao.easy.vvia.models.IAResponse;
import ao.easy.vvia.models.IAResponseList;

public class VivIAUtils {

    public static IAResponseList parseIaResponseList(String responseBody) {
        try {
            JSONObject root = new JSONObject(responseBody);
            JSONArray choices = root.getJSONArray("choices");
            if (choices.length() == 0) return null;

            JSONObject message = choices.getJSONObject(0).getJSONObject("message");
            String content = message.getString("content").trim();

            // 🔹 Limpar possíveis blocos ```json ... ```
            if (content.startsWith("```")) {
                int start = content.indexOf("{");
                int end = content.lastIndexOf("}");
                if (start != -1 && end != -1) {
                    content = content.substring(start, end + 1);
                }
            }

            // Agora o content é um JSON puro
            JSONObject data = new JSONObject(content);

            String userMessage = data.optString("message", "Ação executada.");
            JSONArray actionsJson = data.optJSONArray("actions");

            List<IAResponse> actions = new ArrayList<>();
            if (actionsJson != null) {
                for (int i = 0; i < actionsJson.length(); i++) {
                    JSONObject act = actionsJson.getJSONObject(i);
                    IAResponse action = new IAResponse(
                        act.optString("action"),
                        act.optString("target"),
                        act.has("value") ? act.getDouble("value") : 0.0
                    );
                    actions.add(action);
                }
            }

            return new IAResponseList(userMessage, actions);

        } catch (Exception e) {
            Log.e("HttpClient", "Erro ao parsear resposta: ", e);
            return null;
        }
    }

    public static String extractText(String json) {
       /* try {
            JSONObject obj = new JSONObject(json);
            return obj.getString("partial");
        } catch (JSONException e) {
            return "";
        }*/return json;
    }

}

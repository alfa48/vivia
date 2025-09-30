package ao.easy.vvia.core.http;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ao.easy.vvia.models.IAResponse;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpClient {
    private static final String BASE_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_KEY = "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"; // coloca tua chave aqui
    //private static final String API_KEY = "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";
    private final OkHttpClient client;
    private final MediaType JSON;

    public HttpClient() {
        Log.d("HttpClient", "construtor: ");
        client = new OkHttpClient();
        JSON = MediaType.get("application/json; charset=utf-8");
    }

    // Função principal
    public String sendMessage(String userMessage) {
        Log.e("HttpClient", "sendMessage");

        try {
            Request request = buildRequest(userMessage);
            String rt = executeRequest(request);
            Log.e("HttpClient", "sendMessage:: "+ rt);
            return rt;
        } catch (Exception e) {
            Log.e("HttpClient", "Erro no sendMessage", e);
            return null;
        }
    }

    // 1. Monta o corpo JSON da request
    private JSONObject buildRequestBody(String userMessage) throws Exception {
        Log.e("HttpClient", "buildRequestBody");

        JSONObject body = new JSONObject();
        body.put("model", "deepseek/deepseek-chat-v3.1:free");
        body.put("messages", new org.json.JSONArray()
                .put(new JSONObject()
                        .put("role", "system")
                        .put("content", "Tu és um assistente que responde somente em JSON no formato: {\"action\":\"set\", \"target\":\"brightness|volume\", \"value\":N}"))
                .put(new JSONObject()
                        .put("role", "user")
                        .put("content", userMessage))
        );
        return body;
    }

    // 2. Cria o Request pronto para enviar
    private Request buildRequest(String userMessage) throws Exception {
        Log.e("HttpClient", "buildRequest");

        JSONObject body = buildRequestBody(userMessage);
        RequestBody requestBody = RequestBody.create(body.toString(), JSON);

        return new Request.Builder()
                .url(BASE_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();
    }
    // 3. Executa a chamada e devolve a resposta
    private String executeRequest(Request request) throws IOException {
        Log.e("HttpClient", "executeRequest");

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.e("HttpClient", "Erro: " + response.code());
                return null;
            }
            return response.body().string();
        }
    }



}

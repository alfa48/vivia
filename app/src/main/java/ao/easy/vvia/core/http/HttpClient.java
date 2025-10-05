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
    private static final String API_KEY = "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";
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
                Log.e("HttpClient", "Erro: " + response.body().string());

                return null;
            }
            return response.body().string();
        }
    }
/*
*
*
*
*
*
*
*
*
*
* */





    private JSONObject buildRequestBody(String userMessage) throws Exception {
        JSONObject body = new JSONObject();
        body.put("model", "deepseek/deepseek-chat-v3.1:free");

        // prompt mais robusto (string normal)
        String systemPrompt =
            "Você é um assistente de automação de dispositivos.\n" +
                "Sempre responda exclusivamente em JSON no formato:\n" +
                "{\n" +
                "  \"message\": \"texto explicativo para o usuário\",\n" +
                "  \"actions\": [\n" +
                "    { \"action\": \"set|off|on\", \"target\": \"brightness|volume|wifi|bluetooth|...\", \"value\": número (opcional) }\n" +
                "  ]\n" +
                "}\n" +
                "Responda com uma lista de ações que precisam ser executadas.\n" +

                "Se form uma ação de liga desligao value tem de ser 1 ou 0.\n" +
                "Exemplo:\n" +
                "{\n" +
                "  \"message\": \"Modo cinema ativado\",\n" +
                "  \"actions\": [\n" +
                "    { \"action\": \"set\", \"target\": \"brightness\", \"value\": 30 },\n" +
                "    { \"action\": \"set\", \"target\": \"volume\", \"value\": 70 },\n" +
                "    { \"action\": \"off\", \"target\": \"wifi\" }\n" +
                "  ]\n" +
                "}";

        body.put("messages", new org.json.JSONArray()
            .put(new JSONObject()
                .put("role", "system")
                .put("content", systemPrompt))
            .put(new JSONObject()
                .put("role", "user")
                .put("content", userMessage))
        );

        return body;
    }


}

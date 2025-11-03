package ao.easy.vvia.core.http;

import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import ao.easy.vvia.models.IAResponse;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpClient {
    private static final String BASE_URL = "https://openrouter.ai/api/v1/chat/completions";
    //private static final String API_KEY = "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";
    private static final String API_KEY = "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";
    private final OkHttpClient client;
    private final MediaType JSON;

    // Valores atuais dos sensores
    private float[] accelerometerValues = new float[3]; // x, y, z
    private float lightValue = 0f;

    private static final int MAX_SAMPLES = 10; // número de leituras para média

    // Buffers para as leituras
    private final Queue<float[]> accelBuffer = new LinkedList<>();
    private final Queue<Float> lightBuffer = new LinkedList<>();



    public HttpClient() {
        Log.d("HttpClient", "construtor: ");
        client = new OkHttpClient();
        JSON = MediaType.get("application/json; charset=utf-8");
    }

    // Função principal
    public String sendMessage(String userMessage, Context context) {
        Log.e("HttpClient", "sendMessage");

        try {
            Request request = buildRequest(userMessage, context);
            String rt = executeRequest(request);
            Log.e("HttpClient", "sendMessage:: "+ rt);
            return rt;
        } catch (Exception e) {
            Log.e("HttpClient", "Erro no sendMessage", e);
            return null;
        }
    }


    // 2. Cria o Request pronto para enviar
    private Request buildRequest(String userMessage, Context context) throws Exception {
        Log.e("HttpClient", "buildRequest");

        JSONObject body = buildRequestBody(userMessage, context);
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
* */


    private JSONObject buildRequestBody(String userMessage, Context context) throws Exception {
        JSONObject body = new JSONObject();
        body.put("model", "deepseek/deepseek-chat-v3.1:free");

        // Estado atual do dispositivo
        String deviceState = getDeviceState(context);

        String systemPrompt =
            "Você é um assistente de automação do dispositivo.\n" +
                "O estado atual do dispositivo é:\n" + deviceState + "\n" +
                "Sempre responda exclusivamente em JSON no formato:\n" +
                "{\n" +
                "  \"message\": \"texto explicativo para o usuário\",\n" +
                "  \"actions\": [\n" +
                "    { \"action\": \"set|off|on\", \"target\": \"brightness|volume|wifi|bluetooth|...\", \"value\": número (opcional) }\n" +
                "  ]\n" +
                "}\n" +
                "Responda com uma lista de ações que precisam ser executadas.\n" +
                "Se for uma ação de liga/desliga, value tem de ser 1 ou 0.\n" +
                "Tuas respostas devem levar em conta o estado atual do dispositivo e ajustar valores relativamente a ele. " +
                "Por exemplo, se o volume é 54% e o comando é 'aumentar o volume', o novo volume deve fazer cálculos relativos ao valor atual. .\n"+
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


/*
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
*/

    private String getDeviceState(Context context) {
        StringBuilder state = new StringBuilder();

        appendConnectivityInfo(context, state);
        appendMediaPlaybackInfo(context, state);
        appendVolumeAndSoundInfo(context, state);
        appendDoNotDisturbInfo(context, state);
        appendBrightnessInfo(context, state);
        appendLocationInfo(context, state);
        appendRotationAndAirplaneInfo(context, state);
        appendDeviceInfo(context, state);
        registerSensors(context);
        appendSensorsInfo(state);
        appendObservations(state);
        appendStorageInfo(context, state);

        Log.e("HttpClient", "getDeviceState: " + state.toString());
        return state.toString();
    }



    private void appendConnectivityInfo(Context context, StringBuilder state) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        state.append("Bluetooth: ").append(bluetoothAdapter != null && bluetoothAdapter.isEnabled() ? "ligado" : "desligado").append("\n");

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        state.append("Wi-Fi: ").append(wifiManager != null && wifiManager.isWifiEnabled() ? "ligado" : "desligado").append("\n");

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm != null ? cm.getActiveNetworkInfo() : null;
        if (activeNetwork != null && activeNetwork.isConnected()) {
            state.append("Internet: ").append(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI ? "Wi-Fi" : "dados móveis").append("\n");
        } else {
            state.append("Internet: desconectado\n");
        }
    }

    private void appendMediaPlaybackInfo(Context context, StringBuilder state) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        boolean isMusicPlaying = audioManager != null && audioManager.isMusicActive();
        state.append("Mídia em reprodução: ").append(isMusicPlaying ? "Sim" : "Não").append("\n");
    }


    private void appendVolumeAndSoundInfo(Context context, StringBuilder state) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int mediaVolume = audioManager != null ? audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) : 0;
        state.append("Volume de mídia: ").append(mediaVolume).append("\n");

        int ringerMode = audioManager != null ? audioManager.getRingerMode() : AudioManager.RINGER_MODE_NORMAL;
        String soundMode;
        switch (ringerMode) {
            case AudioManager.RINGER_MODE_SILENT: soundMode = "Silencioso"; break;
            case AudioManager.RINGER_MODE_VIBRATE: soundMode = "Vibrar"; break;
            default: soundMode = "Normal"; break;
        }
        state.append("Modo de som: ").append(soundMode).append("\n");
    }
    private void appendDoNotDisturbInfo(Context context, StringBuilder state) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            int filter = notificationManager != null ? notificationManager.getCurrentInterruptionFilter() : NotificationManager.INTERRUPTION_FILTER_ALL;
            String mode;
            switch (filter) {
                case NotificationManager.INTERRUPTION_FILTER_NONE: mode = "Não Perturbe total"; break;
                case NotificationManager.INTERRUPTION_FILTER_PRIORITY: mode = "Somente prioridades"; break;
                case NotificationManager.INTERRUPTION_FILTER_ALARMS: mode = "Somente alarmes"; break;
                default: mode = "Não Perturbe desativado"; break;
            }
            state.append("Modo Não Perturbe: ").append(mode).append("\n");
        }
    }
    private void appendLocationInfo(Context context, StringBuilder state) {
        int locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, 0);
        state.append("Localização: ").append(locationMode != 0 ? "ativa" : "desativada").append("\n");
    }

    private void appendBrightnessInfo(Context context, StringBuilder state) {
        try {
            int brightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            int autoBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
            state.append("Brilho: ").append(brightness).append("\n");
            state.append("Auto-brilho: ").append(autoBrightness == 1 ? "ativo" : "desativado").append("\n");
        } catch (Settings.SettingNotFoundException e) {
            state.append("Brilho: desconhecido\n");
        }
    }

    private void appendRotationAndAirplaneInfo(Context context, StringBuilder state) {
        try {
            int rotation = Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
            state.append("Rotação automática: ").append(rotation == 1 ? "ativa" : "desativada").append("\n");
        } catch (Settings.SettingNotFoundException e) {
            state.append("Rotação automática: desconhecida\n");
        }

        int airplaneMode = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0);
        state.append("Modo avião: ").append(airplaneMode == 1 ? "ativo" : "desativado").append("\n");
    }
    private void appendDeviceInfo(Context context, StringBuilder state) {
        state.append("Fabricante: ").append(Build.MANUFACTURER).append("\n");
        state.append("Modelo: ").append(Build.MODEL).append("\n");
        state.append("Versão Android: ").append(Build.VERSION.RELEASE).append("\n");
        state.append("RAM disponível: ").append(Runtime.getRuntime().freeMemory() / 1024 / 1024).append("MB\n");
        state.append("Memória total: ").append(Runtime.getRuntime().totalMemory() / 1024 / 1024).append("MB\n");
    }
    private void appendSensorsInfo(StringBuilder state) {
        state.append("Sensor Acelerômetro (último valor): x=").append(accelerometerValues[0])
            .append(", y=").append(accelerometerValues[1])
            .append(", z=").append(accelerometerValues[2]).append("\n");

        state.append("Sensor de Luz (último valor): ").append(lightValue).append(" lx\n");
    }
    private void appendObservations(StringBuilder state) {
        state.append("OBS: Aplicativos em segundo plano, consumo de bateria e dados exigem permissões especiais.\n");
        state.append("- PACKAGE_USAGE_STATS para uso de apps\n");
        state.append("- BATTERY_STATS para consumo de bateria\n");
        state.append("- ACCESS_NETWORK_STATE para consumo de dados\n");
    }


    private void registerSensors(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) return;

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    addAccelerometerSample(event.values.clone());
                } else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                    addLightSample(event.values[0]);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };

        if (accelerometer != null) {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (light != null) {
            sensorManager.registerListener(listener, light, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    // Adiciona nova leitura e calcula média
    private void addAccelerometerSample(float[] sample) {
        if (accelBuffer.size() >= MAX_SAMPLES) accelBuffer.poll(); // remove a mais antiga
        accelBuffer.add(sample);

        float xSum = 0f, ySum = 0f, zSum = 0f;
        for (float[] s : accelBuffer) {
            xSum += s[0];
            ySum += s[1];
            zSum += s[2];
        }
        int n = accelBuffer.size();
        accelerometerValues[0] = xSum / n;
        accelerometerValues[1] = ySum / n;
        accelerometerValues[2] = zSum / n;
    }

    // Adiciona nova leitura de luz e calcula média
    private void addLightSample(float value) {
        if (lightBuffer.size() >= MAX_SAMPLES) lightBuffer.poll(); // remove a mais antiga
        lightBuffer.add(value);

        float sum = 0f;
        for (float v : lightBuffer) sum += v;
        lightValue = sum / lightBuffer.size();
    }

    private void appendStorageInfo(Context context, StringBuilder state) {
        // Memória interna
        File internalStorage = Environment.getDataDirectory();
        StatFs statFsInternal = new StatFs(internalStorage.getPath());

        long blockSize = statFsInternal.getBlockSizeLong();
        long totalBlocks = statFsInternal.getBlockCountLong();
        long availableBlocks = statFsInternal.getAvailableBlocksLong();

        long totalInternal = totalBlocks * blockSize;
        long availableInternal = availableBlocks * blockSize;
        long usedInternal = totalInternal - availableInternal;

        state.append("Memória interna total: ").append(totalInternal / 1024 / 1024).append(" MB\n");
        state.append("Memória interna disponível: ").append(availableInternal / 1024 / 1024).append(" MB\n");
        state.append("Memória interna usada: ").append(usedInternal / 1024 / 1024).append(" MB\n");

        // Memória externa (cartão SD), se existir
        File externalStorage = Environment.getExternalStorageDirectory();
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            StatFs statFsExternal = new StatFs(externalStorage.getPath());
            long totalExternal = statFsExternal.getBlockCountLong() * statFsExternal.getBlockSizeLong();
            long availableExternal = statFsExternal.getAvailableBlocksLong() * statFsExternal.getBlockSizeLong();
            long usedExternal = totalExternal - availableExternal;

            state.append("Memória externa total: ").append(totalExternal / 1024 / 1024).append(" MB\n");
            state.append("Memória externa disponível: ").append(availableExternal / 1024 / 1024).append(" MB\n");
            state.append("Memória externa usada: ").append(usedExternal / 1024 / 1024).append(" MB\n");
        } else {
            state.append("Memória externa: não montada ou indisponível\n");
        }
    }


}

package ao.easy.vvia.features.audio;

import android.content.Context;
import android.media.MediaRecorder;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AudioTranscriber {

    public interface TranscriptionListener {
        void onTranscriptionSuccess(String text);
        void onTranscriptionError(String error);
    }

    private Context context;
    private MediaRecorder recorder;
    private String audioFilePath;
    private TranscriptionListener listener;

    public AudioTranscriber(Context context, TranscriptionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    // --- Iniciar gravação ---
    public void startRecording() {
        audioFilePath = context.getExternalFilesDir(null).getAbsolutePath() + "/audio.mp3";
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(audioFilePath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            e.printStackTrace();
            if(listener != null) listener.onTranscriptionError("Erro ao iniciar gravação: " + e.getMessage());
        }
    }

    // --- Parar gravação e iniciar transcrição ---
    public void stopRecordingAndTranscribe() {
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (RuntimeException e) {
                // pode ocorrer se stop for chamado muito cedo
                e.printStackTrace();
            }
            recorder.release();
            recorder = null;
            transcribeAudio(new File(audioFilePath));
        }
    }

    // --- Transcrição via Whisper API ---
    private void transcribeAudio(File audioFile) {
        OkHttpClient client = new OkHttpClient();

        MultipartBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("model", "whisper-1")
            .addFormDataPart("file", "audio.mp3",
                RequestBody.create(audioFile, MediaType.parse("audio/mpeg")))
            .build();

        Request request = new Request.Builder()
            .url("https://api.openai.com/v1/audio/transcriptions")
            .header("Authorization", "Bearer YOUR_OPENAI_API_KEY")
            .post(requestBody)
            .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if(listener != null) listener.onTranscriptionError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errMsg = "Erro na transcrição: " + response.message();
                    Log.e("Whisper", errMsg);
                    if(listener != null) listener.onTranscriptionError(errMsg);
                    return;
                }

                String json = response.body().string();
                try {
                    JSONObject obj = new JSONObject(json);
                    String text = obj.getString("text");
                    if(listener != null) listener.onTranscriptionSuccess(text);
                } catch (JSONException e) {
                    e.printStackTrace();
                    if(listener != null) listener.onTranscriptionError(e.getMessage());
                }
            }
        });
    }
}


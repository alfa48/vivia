package ao.easy.vvia.features.audio;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;
import org.vosk.android.SpeechStreamService;
import org.vosk.android.StorageService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ao.easy.vvia.MainActivity;

public class AudioTranscriberVosk {
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;


    public interface TranscriptionListener {
        void onPartialResult(String partial);
        void onFinalResult(String finalText);
        void onError(String error);
        void onModelLoaded();
    }
    public interface Listener {
        void onResult(String text);
        void onFinalResult(String text);
        void onPartialResult(String text);
        void onError(String error);
        void onReady();
        void onListeningStarted();
        void onListeningStopped();
    }

    private Context context;
    private Model model;
    private SpeechService speechService;
    private SpeechStreamService speechStreamService;
    private Listener listener;

    public AudioTranscriberVosk(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void initModel() {
        StorageService.unpack(context, "model-pt", "model",
            (model) -> {
                this.model = model;
                listener.onReady();
            },
            (exception) -> listener.onError("Failed to unpack the model: " + exception.getMessage()));
    }

    public void recognizeMicrophone(MainActivity context) {
        int permissionCheck = ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }

        if (speechService != null) {
            speechService.stop();
            speechService = null;
            listener.onListeningStopped(); // 🔹 Agora avisamos que parou
        } else {
            try {
                Recognizer rec = new Recognizer(model, 16000.0f);
                speechService = new SpeechService(rec, 16000.0f);
                speechService.startListening(new RecognitionListenerAdapter());
                rec.setMaxAlternatives(0);
                rec.setWords(true);
                // rec.setPartialWords(true);

                listener.onListeningStarted(); // 🔹 Agora avisamos que começou
            } catch (IOException e) {
                listener.onError(e.getMessage());
            }
        }
    }


    public void recognizeFile(InputStream ais) {
        if (speechStreamService != null) {
            speechStreamService.stop();
            speechStreamService = null;
        } else {
            try {
                Recognizer rec = new Recognizer(model, 16000.f);
                speechStreamService = new SpeechStreamService(rec, ais, 16000);
                speechStreamService.start(new RecognitionListenerAdapter());
            } catch (IOException e) {
                listener.onError(e.getMessage());
            }
        }
    }

    public void pause(boolean checked) {
        if (speechService != null) {
            speechService.setPause(checked);
        }
    }

    public void stop() {
        if (speechService != null) speechService.stop();
        if (speechStreamService != null) speechStreamService.stop();
    }

    public void stopListening() {
        if (speechService != null) {
            speechService.cancel(); // ou .stop() se preferir o resultado final
        }
    }

    private class RecognitionListenerAdapter implements RecognitionListener {
        @Override
        public void onResult(String hypothesis) {
            listener.onResult(hypothesis);
        }

        @Override
        public void onFinalResult(String hypothesis) {
            listener.onFinalResult(hypothesis);
        }

        @Override
        public void onPartialResult(String hypothesis) {
            listener.onPartialResult(hypothesis);
        }

        @Override
        public void onError(Exception e) {
            listener.onError(e.getMessage());
        }

        @Override
        public void onTimeout() {}
    }
}

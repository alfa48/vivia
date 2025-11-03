package ao.easy.vvia;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ao.easy.vvia.dispatcher.ActionDispatcher;
import ao.easy.vvia.features.audio.AudioRecorder;
import ao.easy.vvia.features.audio.AudioTranscriberVosk;
import ao.easy.vvia.wigdets.ClickableDrawableEditText;

public class MainActivity2 extends AppCompatActivity {

    private static final String TAG = "🌍 IdiomasSuportados";

    private static final int REQUEST_RECORD_AUDIO = 1;
    private SpeechRecognizer speechRecognizer;
    Button buttonSend;
    //EditText editText;
    ClickableDrawableEditText editText;
    TextView textView, textViewVivia;
    ImageView btnMic;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_);

        initWidgets();

        // Pedir permissão de microfone
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }

        // Iniciaactivity_main_lizar reconhecimento de voz
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US"); // ou "pt-BR"
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true); // ⚡ Forçar modo offline

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {
                Log.i("🌍 IdiomasSuportados", "Reconhecimento iniciado para: " +
                    intent.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE));
                textViewVivia.setText("Fale agora...");
            }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}

            @Override public void onError(int error) {
                Log.e("🌍 IdiomasSuportados", "Erro no reconhecimento: " + error);
                textViewVivia.setText("Erro: " + error);
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty())
                    textView.setText("Final: " + matches.get(0));
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> partial = partialResults.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
                if (partial != null && !partial.isEmpty())
                    textView.setText("Parcial: " + partial.get(0));
            }

            @Override public void onEvent(int eventType, Bundle params) {}
        });

        btnMic.setOnClickListener(v -> {
            textViewVivia.setText("Escutando...");
            speechRecognizer.startListening(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO && grantResults.length > 0
            && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            textViewVivia.setText("Permissão de microfone negada.");
        }
    }

    private void initWidgets() {
        btnMic = findViewById(R.id.btnMic);
        editText = (ClickableDrawableEditText) findViewById(R.id.editTextCommand);
        textView = findViewById(R.id.textResponse);
        textViewVivia = findViewById(R.id.textViewVivia);
        progressBar = findViewById(R.id.progressBar);
    }

}


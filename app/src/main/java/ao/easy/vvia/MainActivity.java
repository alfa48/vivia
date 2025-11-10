package ao.easy.vvia;
//Key_api::::::

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ao.easy.vvia.core.http.HttpClient;
import ao.easy.vvia.dispatcher.ActionDispatcher;
import ao.easy.vvia.features.audio.AudioTranscriberVosk;
import ao.easy.vvia.models.IAResponse;
import ao.easy.vvia.models.IAResponseList;
import ao.easy.vvia.utils.PulseAnimator;
import ao.easy.vvia.utils.VivIAUtils;
import ao.easy.vvia.wigdets.ClickableDrawableEditText;

public class MainActivity extends AppCompatActivity {
    EditText editText;
    TextView textView, textViewVivia;
    ImageView btnMic, btnSend;
    private ActionDispatcher dispatcher;
    ProgressBar progressBar;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 101;
    private static final int REQUEST_RECORD_AUDIO = 1;
    private SpeechRecognizer speechRecognizer;
    Intent intent;

    private PulseAnimator pulseAnimator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.min_x);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        requestBluetoothPermission();
        initViews();
        //btnSend.setEnabled(false);
        //btnMic.setEnabled(true);

        // Iniciaactivity_main_lizar reconhecimento de voz
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US"); // ou "pt-BR"
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true); // ⚡ Forçar modo offline



        pulseAnimator = new PulseAnimator();

        btnSend.setOnClickListener(v->{
            String message = editText.getText().toString();
            progressBar.setVisibility(View.VISIBLE);

            if (message.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Mensagem invaliada: " + message, Toast.LENGTH_LONG).show();
                return;
            }else{
                new Thread(() -> {
                    HttpClient aiClient = new HttpClient();
                    String response = aiClient.sendMessage(message, MainActivity.this);
                    if (response == null) {
                        runOnUiThread(() -> {
                            btnSend.setEnabled(false);
                            progressBar.setVisibility(View.GONE);
                            textView.setText("Ops! Você atingiu o limite gratuito de hoje. Para continuar, adicione créditos à sua conta");
                        });
                        return;
                    }

                    try {
                        IAResponseList results = VivIAUtils.parseIaResponseList(response);

                        if (results != null || results.getActions() == null) {
                            Log.d("IA", "Mensagem: " + results.message);
                            for (IAResponse act : results.actions) {
                                Log.d("IA", act.action + " " + act.target + " " + act.value);
                            }
                        } else {
                            Log.e("IA", "Falha ao interpretar a resposta da IA");
                        }

                        // Executar ações na thread principal
                        runOnUiThread(() -> {
                            for (IAResponse ia : results.getActions()) {
                                dispatcher.dispatch(MainActivity.this, ia); // ✅ use Activity como contexto
                            }
                            runOnUiThread(() -> {
                                btnSend.setEnabled(true);
                                progressBar.setVisibility(View.GONE);
                                textView.setText(results.getMessage());
                            });

                        });

                    } catch (Exception e) {
                        Log.e("HttpClient", "Erro ao parsear resposta: " + response, e);
                        runOnUiThread(() -> {
                            btnSend.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                            textView.setText("Ops! Algo deu errado não está certo. Tente novamente mais tarde.");
                        });

                    }
                }).start();

            }
            runOnUiThread(() -> {
                editText.setText("");
                //editText.performClick();
            });
        });

          speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {
                Log.i("🌍 IdiomasSuportados", "Reconhecimento iniciado para: " +
                    intent.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE));
                textView.setText("Fale agora...");
            }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float dB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onError(int error) {
                pulseAnimator.stop();
                Log.e("🌍 IdiomasSuportados", "Erro no reconhecimento: " + error);
                textViewVivia.setText("Erro: " + error);
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
                runOnUiThread(() -> {
                    pulseAnimator.stop();
                    if (matches != null && !matches.isEmpty()) {
                        editText.setText(matches.get(0));
                        textView.setText(matches.get(0));
                    }
                });
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> partial = partialResults.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
                runOnUiThread(() -> {
                    if (partial != null && !partial.isEmpty())
                        textView.setText("Parcial: " + partial.get(0));
                });

            }

            @Override public void onEvent(int eventType, Bundle params) {}
        });

        btnMic.setOnClickListener(v -> {
            // Pedir permissão de microfone
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
            }else{
                pulseAnimator.start(btnMic);
                textViewVivia.setText("Escutando...");
                speechRecognizer.startListening(intent);
            }
        });

    }

    private void requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Agora o compilador encontrará a classe Manifest
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSION);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull     String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão Bluetooth concedida!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissão Bluetooth negada!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    private void initViews() {
        btnMic = findViewById(R.id.btnMic);
        btnSend= findViewById(R.id.btnSend);
        editText = (EditText) findViewById(R.id.editTextCommand);
        dispatcher = new ActionDispatcher();
        textView = findViewById(R.id.textResponse);
        textViewVivia = findViewById(R.id.textViewVivia);
        progressBar = findViewById(R.id.progressBar);
    }
}

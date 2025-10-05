package ao.easy.vvia;
//Key_api::::::

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import ao.easy.vvia.core.http.HttpClient;
import ao.easy.vvia.dispatcher.ActionDispatcher;
import ao.easy.vvia.models.IAResponse;
import ao.easy.vvia.models.IAResponseList;
import ao.easy.vvia.utils.VivIAUtils;
import ao.easy.vvia.wigdets.ClickableDrawableEditText;

public class MainActivity extends AppCompatActivity {
    Button buttonSend;
    //EditText editText;
    ClickableDrawableEditText editText;
    TextView textView;
    ImageView btnMic;
    private ActionDispatcher dispatcher;
    ProgressBar progressBar;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        requestBluetoothPermission();

        btnMic = findViewById(R.id.btnMic);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(btnMic, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(btnMic, "scaleY", 1f, 1.2f, 1f);

// Configura repetição nos animadores individuais
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.setRepeatMode(ValueAnimator.REVERSE);

        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatMode(ValueAnimator.REVERSE);

// Junta os dois em um AnimatorSet
        AnimatorSet pulse = new AnimatorSet();
        pulse.playTogether(scaleX, scaleY);
        pulse.setDuration(2000);
        pulse.start();





        editText = (ClickableDrawableEditText) findViewById(R.id.editTextCommand);
        dispatcher = new ActionDispatcher();
        textView = findViewById(R.id.textResponse);
        progressBar = findViewById(R.id.progressBar);


        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (editText.getCompoundDrawables()[2] != null) { // drawableRight existe
                        // verifica se o clique foi dentro da largura do drawableRight
                        if (event.getX() >= (editText.getWidth() - editText.getPaddingRight() -
                            editText.getCompoundDrawables()[2].getBounds().width())) {

                            String message = editText.getText().toString();
                            progressBar.setVisibility(View.VISIBLE);

                            if (message.isEmpty()) {
                                Toast.makeText(getApplicationContext(), "Mensagem invaliada: " + message, Toast.LENGTH_LONG).show();

                            }else{


                                new Thread(() -> {
                                    HttpClient aiClient = new HttpClient();
                                    String response = aiClient.sendMessage(message);
                                    if (response == null) {
                                        runOnUiThread(() -> {
                                            editText.setEnabled(true);
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
                                                editText.setEnabled(true);
                                                progressBar.setVisibility(View.GONE);
                                                textView.setText(results.getMessage());
                                            });

                                        });

                                    } catch (Exception e) {
                                        Log.e("HttpClient", "Erro ao parsear resposta: " + response, e);
                                        runOnUiThread(() -> {
                                            editText.setEnabled(true);
                                            progressBar.setVisibility(View.GONE);
                                            textView.setText("Ops! Algo deu errado não está certo. Tente novamente mais tarde.");
                                        });

                                    }
                                }).start();




                            }

                            editText.setText("");
                            editText.performClick();
                            return true; // evento consumido
                        }
                    }
                }
                return false; // deixa o EditText processar normalmente o clique
            }

        });


        /*
        buttonSend = findViewById(R.id.btnSend);




        //send sms in vivia
        buttonSend.setOnClickListener(v->{
            buttonSend.setEnabled(false);// Desativa o botão
            String message = editText.getText().toString();
            progressBar.setVisibility(View.VISIBLE);

                if (message.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Mensagem invaliada: " + message, Toast.LENGTH_LONG).show();

                }else{


                    new Thread(() -> {
                        HttpClient aiClient = new HttpClient();
                        String response = aiClient.sendMessage(message);
                        if (response == null) {
                            runOnUiThread(() -> {
                                buttonSend.setEnabled(true);
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
                                    buttonSend.setEnabled(true);
                                    progressBar.setVisibility(View.GONE);
                                    textView.setText(results.getMessage());
                                });

                            });

                        } catch (Exception e) {
                            Log.e("HttpClient", "Erro ao parsear resposta: " + response, e);
                            runOnUiThread(() -> {
                                buttonSend.setEnabled(true);
                                progressBar.setVisibility(View.GONE);
                                textView.setText("Ops! Algo deu errado não está certo. Tente novamente mais tarde.");
                            });

                        }
                    }).start();




                }

        });
        */

    }



    private IAResponse parseIaResponse(String responseBody) throws JSONException {
        JSONObject root = new JSONObject(responseBody);
        JSONObject message = root.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message");

        // conteúdo como string JSON
        String content = message.getString("content");

        // parse do conteúdo JSON interno
        JSONObject aiJson = new JSONObject(content);

        String action = aiJson.getString("action");
        String target = aiJson.getString("target");
        int value = aiJson.getInt("value");

        return new IAResponse(action, target, value);
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

}

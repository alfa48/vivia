package ao.easy.vvia;
//Key_api::::::

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import ao.easy.vvia.core.http.HttpClient;
import ao.easy.vvia.dispatcher.ActionDispatcher;
import ao.easy.vvia.models.IAResponse;

public class MainActivity extends AppCompatActivity {
    Button btnL, btnV, btnSend;
    EditText editText;
    private ActionDispatcher dispatcher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        editText = findViewById(R.id.editTextTextMultiLine);
        dispatcher = new ActionDispatcher();

        // Simulação: resposta da IA (ex: "Altera o volume para 5%")
        IAResponse aiResponse = new IAResponse();
        aiResponse.setAction("set");
        aiResponse.setTarget("volume");
        aiResponse.setValue(40);


        // Simulação: resposta da IA (ex: "Altera o volume para 5%")
        IAResponse aiResponse1 = new IAResponse();
        aiResponse1.setAction("set");
        aiResponse1.setTarget("brightness");
        aiResponse1.setValue(15);

        findViewById(R.id.buttonV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Executa a ação
                dispatcher.dispatch(view.getContext(), aiResponse);
            }
        });
        findViewById(R.id.buttonL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatcher.dispatch(view.getContext(), aiResponse1);
            }
        });


        //send sms in vivia
        findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = editText.getText().toString();
                if (message.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Mensagem invaliada: " + message, Toast.LENGTH_LONG).show();

                }else{
                    new Thread(() -> {
                        HttpClient aiClient = new HttpClient();
                        //Toast.makeText(getApplicationContext(), "Mensagem enviada: " + message, Toast.LENGTH_LONG).show();

                        String responseFromSendMessage = aiClient.sendMessage(message);
                        try {
                            IAResponse iaResponse = IaparseIaResponse(responseFromSendMessage);
                            Log.d("MinhaAppDebug", "Resposta do sendMessage: " + iaResponse.getAction() + " " + iaResponse.getTarget() + " " + iaResponse.getValue());
                            dispatcher.dispatch(getApplicationContext(), iaResponse);
                        } catch (JSONException e) {
                            Log.d("MinhaAppDebug", "Erro: Resposta do sendMessage: " + responseFromSendMessage);
                            throw new RuntimeException(e);
                        }
                    }).start();
                }

            }
        });
    }

    private IAResponse IaparseIaResponse(String responseBody) throws JSONException {
        // 1. Parse do corpo inteiro da resposta da API
        JSONObject root = new JSONObject(responseBody);

        // 2. Caminho até o "content"
        String content = root
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");

        // 3. "content" é uma String contendo outro JSON → parse de novo
        JSONObject aiJson = new JSONObject(content);

        // 4. Extrair campos
        String action = aiJson.getString("action");
        String target = aiJson.getString("target");
        int value = aiJson.getInt("value");

        return new IAResponse(action, target, value);
    }

}
package com.pingapp.chat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    EditText inputMessage;
    Button btnSend;
    TextView textResponse;
    
    String SERVER_IP = "10.0.2.2"; 
    int SERVER_PORT = 4040;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputMessage = findViewById(R.id.inputMessage);
        btnSend = findViewById(R.id.btnSend);
        textResponse = findViewById(R.id.textResponse);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageToSend = inputMessage.getText().toString();
                if(!messageToSend.isEmpty()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sendMessageToServer(messageToSend);
                        }
                    }).start();
                }
            }
        });
    }

    private void sendMessageToServer(String msg) {
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(msg);
            String response = in.readLine();
            
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textResponse.setText(response);
                }
            });

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textResponse.setText("خطأ: تعذر الاتصال بسيرفر PingApp!");
                }
            });
        }
    }
}

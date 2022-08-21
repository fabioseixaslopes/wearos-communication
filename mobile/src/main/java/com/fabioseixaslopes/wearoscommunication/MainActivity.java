package com.fabioseixaslopes.wearoscommunication;

import android.content.BroadcastReceiver;
import android.widget.Button;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.wearable.Node;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MainActivity extends AppCompatActivity {

    Button sendButton;
    TextView textView;
    int valueToSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        sendButton = findViewById(R.id.sendButton);

        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

        sendButton.setOnClickListener(v -> {
            Random rand = new Random();
            valueToSend = rand.nextInt(100);
            String text = "Sending value to wearable: " + valueToSend;
            textView.setText(text);
            String message = String.valueOf(valueToSend);
            new sendMessage("/random_value", message).start();
        });
    }

    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String received = intent.getStringExtra("message");
            String message = "Received value from wearable: " + received;
            textView.setText(message);
        }
    }

    class sendMessage extends Thread {
        String path;
        String message;

        sendMessage(String p, String m) {
            path = p;
            message = m;
        }

        public void run() {
            Task<List<Node>> wearableList =
                    Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            try {
                List<Node> nodes = Tasks.await(wearableList);
                for (Node node : nodes) {
                    Task<Integer> sendMessageTask =
                            Wearable.getMessageClient(MainActivity.this).sendMessage(node.getId(), path, message.getBytes());
                    try {
                        Tasks.await(sendMessageTask);
                    } catch (ExecutionException | InterruptedException exception) {
                        exception.printStackTrace();
                    }
                }
            } catch (ExecutionException | InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }
}
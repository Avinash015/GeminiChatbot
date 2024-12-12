package com.example.myapplication;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    TextView welcomeTextView;
    EditText messageEditText;
    ImageButton sendButton;
    List<Message> messageList;
    MessageAdapter messageAdapter;
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messageList = new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_view);
        welcomeTextView = findViewById(R.id.welcome_text);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_btn);

        //setup recycler view
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(false);

        sendButton.setOnClickListener((v) -> {
            String question = messageEditText.getText().toString().trim();
            addToChat(question, Message.SENT_BY_ME);
            messageEditText.setText("");
            welcomeTextView.setVisibility(View.GONE);

            // Create GenerativeModel with your model name and API key
            GenerativeModel gm = new GenerativeModel(
                    "gemini-1.5-flash",
                    "APIKEY"
            );

            // Create a model instance
            GenerativeModelFutures model = GenerativeModelFutures.from(gm);

            // Create content to send
            Content content = new Content.Builder().addText(question).build();

            // For handling the response, use an executor
            Executor executor = Executors.newSingleThreadExecutor();

            // Call the API asynchronously
            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

            Futures.addCallback(
                    response,
                    new FutureCallback<GenerateContentResponse>() {
                        @Override
                        public void onSuccess(GenerateContentResponse result) {
                            // Get the generated response and add it to the chat
                            String resultText = result.getText();
                            addResponse(resultText);
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            t.printStackTrace();
                            addResponse("Failed to load response: " + t.getMessage());
                        }
                    },
                    executor
            );
        });

    }

    void addToChat(String message,String sentBy){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message,sentBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    }

    void addResponse(String response){

        addToChat(response,Message.SENT_BY_BOT);
    }









}
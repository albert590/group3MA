package com.example.group3ma;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AIChatActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageButton btnSend;
    private List<ChatMessage> chatMessages;
    private ChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        chatMessages = new ArrayList<>();
        adapter = new ChatAdapter(chatMessages);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);
        
        chatMessages.add(new ChatMessage("Hello! I'm your MMUST Hostel Assistant. How can I help you find a hostel today?", false));
        adapter.notifyItemInserted(0);

        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                chatMessages.add(new ChatMessage(message, true));
                adapter.notifyItemInserted(chatMessages.size() - 1);
                rvChat.scrollToPosition(chatMessages.size() - 1);
                etMessage.setText("");
                processAIResponse(message);
            }
        });
    }

    private void processAIResponse(String userMessage) {
        String response;
        String msg = userMessage.toLowerCase();

        if (msg.contains("hostel") && (msg.contains("cheap") || msg.contains("6000") || msg.contains("price"))) {
            response = "I found several affordable hostels! Joventure, Sunrise, and Blue Nile usually have rooms under KSh 6,000. Would you like to see the full list in the 'Hostels' section?";
        } else if (msg.contains("female") || msg.contains("girls") || msg.contains("ladies")) {
            response = "For female students, Queens Park and Shalom are highly recommended for their security and proximity to the main gate.";
        } else if (msg.contains("security") || msg.contains("safe")) {
            response = "Security is a top priority at MMUST Hostel. Hostels like 'The Hub' and 'MMUST Hostels' have 24/7 CCTV and professional guards. You can see the 'Verified' badge on hostels we've personally checked.";
        } else if (msg.contains("book") || msg.contains("pay") || msg.contains("reserve")) {
            response = "To book a hostel: \n1. Go to the Hostels list\n2. Select a hostel you like\n3. Click 'Book This Hostel'\n4. Fill the form and pay via M-Pesa. It's that simple!";
        } else if (msg.contains("how") && msg.contains("work")) {
            response = "MMUST Hostel helps you find, favorite, and book hostels easily. We also have a marketplace for students and roommate matching!";
        } else if (msg.contains("hello") || msg.contains("hi") || msg.contains("hey")) {
            response = "Hello! I'm here to help you with anything related to MMUST hostels, roommate matching, or the student marketplace. What's on your mind?";
        } else {
            response = "That's a great question! While I'm still learning, I can definitely help you find hostels by price, gender, or security features. Try asking 'Which hostels are cheap?' or 'Show me safe hostels'.";
        }

        rvChat.postDelayed(() -> {
            chatMessages.add(new ChatMessage(response, false));
            adapter.notifyItemInserted(chatMessages.size() - 1);
            rvChat.scrollToPosition(chatMessages.size() - 1);
        }, 1000);
    }
}

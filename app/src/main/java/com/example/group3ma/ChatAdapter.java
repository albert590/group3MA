package com.example.group3ma;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> chatMessages;

    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        holder.tvMessage.setText(message.getMessage());

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.cardMessage.getLayoutParams();
        if (message.isUser()) {
            holder.cardMessage.setCardBackgroundColor(0xFF1976D2); // Material Blue
            params.setMarginStart(100);
            params.setMarginEnd(0);
        } else {
            holder.cardMessage.setCardBackgroundColor(0xFF424242); // Grey
            params.setMarginStart(0);
            params.setMarginEnd(100);
        }
        holder.cardMessage.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        MaterialCardView cardMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            cardMessage = itemView.findViewById(R.id.cardMessage);
        }
    }
}

package com.hmy.chatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hmy.chatapp.listeners.ConversationListener;
import com.hmy.chatapp.databinding.ItemContainerRecentConversationBinding;
import com.hmy.chatapp.models.ChatMessage;
import com.hmy.chatapp.models.User;

import java.util.List;

public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.ConversationViewHolder>{

    private final List<ChatMessage> chatMessages;
    private final ConversationListener conversationListener;

    public RecentConversationAdapter(List<ChatMessage> chatMessages, ConversationListener conversationListener) {
        this.chatMessages = chatMessages;
        this.conversationListener = conversationListener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationViewHolder(
                ItemContainerRecentConversationBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) { //用于绑定数据
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {

        final ItemContainerRecentConversationBinding binding;

        public ConversationViewHolder(ItemContainerRecentConversationBinding itemContainerRecentConversationBinding) {
            super(itemContainerRecentConversationBinding.getRoot());
            binding = itemContainerRecentConversationBinding;
        }

        void setData(ChatMessage chatMessage) {
            binding.imageProfile.setImageBitmap(getConversationImage(chatMessage.getConversationImage()));
            binding.textName.setText(chatMessage.getConversationName());
            binding.textRecentMessage.setText(chatMessage.getMessage());
            binding.textDate.setText(chatMessage.getDateTime());
            binding.getRoot().setOnClickListener(v -> { //点击Recent Conversation中的会话后跳转到聊天界面
                User user = new User();
                user.setId(chatMessage.getConversationId());
                user.setName(chatMessage.getConversationName());
                user.setImage(chatMessage.getConversationImage());
                conversationListener.onConversationClicked(user);
            });
        }
    }


    private Bitmap getConversationImage (String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}

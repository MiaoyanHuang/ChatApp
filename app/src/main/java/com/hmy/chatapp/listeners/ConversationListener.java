package com.hmy.chatapp.listeners;

import com.hmy.chatapp.models.User;

public interface ConversationListener {
    void onConversationClicked(User user);
}

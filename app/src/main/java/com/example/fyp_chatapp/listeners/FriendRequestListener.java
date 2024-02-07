package com.example.fyp_chatapp.listeners;

import com.example.fyp_chatapp.models.FriendRequest;

public interface FriendRequestListener {

    void onAcceptRequest(FriendRequest friendRequest);
    void onRejectRequest(FriendRequest friendRequest);
}

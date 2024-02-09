package com.hmy.chatapp.listeners;

import com.hmy.chatapp.models.FriendRequest;

public interface FriendRequestListener {

    void onAcceptRequest(FriendRequest friendRequest);
    void onRejectRequest(FriendRequest friendRequest);
}

package com.example.fyp_chatapp.utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_REAL_NAME = "realName";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_DOB = "dob";
    public static final String KEY_REGION = "region";
    public static final String KEY_OCCUPATION = "occupation";
    public static final String KEY_AGE = "age";
    public static final String KEY_FRIENDS = "friends";
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";

    public static final String KEY_COLLECTION_REQUEST_ADD_FRIEND = "requestAddFriend";
    public static final String KEY_REQUEST_ADD_REQUEST_FROM_ID = "requestAddFriendFromId";
    public static final String KEY_REQUEST_ADD_REQUEST_FROM_NAME = "requestAddFriendFromName";
    public static final String KEY_REQUEST_ADD_REQUEST_FROM_IMAGE = "requestAddFriendFromImage";
    public static final String KEY_REQUEST_ADD_REQUEST_FROM_EMAIL = "requestAddFriendFromEmail";

    public static final String KEY_REQUEST_ADD_REQUEST_TO_ID = "requestAddFriendToId";
    public static final String KEY_REQUEST_ADD_REQUEST_TO_NAME = "requestAddFriendToName";
    public static final String KEY_REQUEST_ADD_REQUEST_TO_EMAIL = "requestAddFriendToEmail";
    public static final String KEY_REQUEST_ADD_REQUEST_TO_IMAGE = "requestAddFriendToImage";

    public static final String KEY_REQUEST_ADD_REQUEST_STATUS = "requestAddFriendStatus";
    public static final String KEY_REQUEST_ADD_REQUEST_STATUS_PENDING = "pending";
    public static final String KEY_REQUEST_ADD_REQUEST_STATUS_ACCEPTED = "accepted";
    public static final String KEY_REQUEST_ADD_REQUEST_STATUS_REJECTED = "rejected";

    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";

    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_AVAILABILITY = "availability";

    //Firebase Cloud Messaging
    public static final String REMOTE_MEG_KEY = "key=AAAA6gaNe6k:APA91bGfjecd2F9QBkYMfkr4NwGoGKYZeOH4W5hp88V-RcLVdCIvOJc9HGogzIsm81TpNgyhR4NMjYvGrnRwpLhTIc2MIbjwX0YecSmKqMMW1978WN6r9t_gX9yJhax1GGNdvn5b7b8B";
    public static final String REMOTE_MEG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static HashMap<String, String> remoteMsgHeaders = null;
    public static HashMap<String, String> getRemoteMsgHeaders() {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(REMOTE_MEG_AUTHORIZATION, REMOTE_MEG_KEY);
            remoteMsgHeaders.put(REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }
}

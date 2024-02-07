package com.example.fyp_chatapp.fragment;

import static android.app.Activity.RESULT_OK;

import static java.lang.Thread.sleep;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.fyp_chatapp.activities.ChatActivity;
import com.example.fyp_chatapp.adapters.RecentConversationAdapter;
import com.example.fyp_chatapp.databinding.FragmentHomeBinding;
import com.example.fyp_chatapp.listeners.ConversationListener;
import com.example.fyp_chatapp.models.ChatMessage;
import com.example.fyp_chatapp.models.User;
import com.example.fyp_chatapp.utilities.Constants;
import com.example.fyp_chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class HomeFragment extends Fragment implements ConversationListener {

    private FragmentHomeBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentConversationAdapter conversationsAdapter;
    private FirebaseFirestore database;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentHomeBinding.inflate(getLayoutInflater()); //initialize binding
        preferenceManager = new PreferenceManager(Objects.requireNonNull(getContext()));
        init(); //initialize variables
        getToken(); //initialize token
        listenConversations(); //initialize conversations
        return binding.getRoot();
    }

    private final ActivityResultLauncher<Intent> isRemoved = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK){
                    Log.d("TAG", "Yes removeddddd");
                    new Thread(() -> {
                        try {
                            sleep(1000);
                            conversations.clear();
                            listenConversations();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }
    );

    private void init(){
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationAdapter(conversations, this);
        //this是因为MainActivity实现了ConversationListener，
        //所以可以传入this，也就是MainActivity，
        //this是一个listener，用于监听点击事件
        //当点击某个聊天记录的时候，就会调用onConversationClicked方法
        binding.conversationsRecyclerView.setAdapter(conversationsAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void listenConversations(){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }

        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                String lastMessage = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);

                // 判断消息是否已存在于conversations列表中
                boolean conversationExists = false;
                int existingConversationIndex = -1;

                for (int i = 0; i < conversations.size(); i++) {
                    ChatMessage chatMessage = conversations.get(i);
                    if (chatMessage.senderId.equals(senderId) && chatMessage.receiverId.equals(receiverId)) {
                        conversationExists = true;
                        existingConversationIndex = i;
                        break;
                    }
                }

                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    if (!conversationExists) {
                        // 添加新的聊天记录
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.senderId = senderId;
                        chatMessage.receiverId = receiverId;
                        if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                            chatMessage.conversationImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                            chatMessage.conversationName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                            chatMessage.conversationId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        } else {
                            chatMessage.conversationImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                            chatMessage.conversationName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                            chatMessage.conversationId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        }
                        chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                        chatMessage.message = lastMessage;
                        chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                        conversations.add(chatMessage);
                    } else {
                        // 更新现有的聊天记录
                        ChatMessage existingChatMessage = conversations.get(existingConversationIndex);
                        existingChatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                        existingChatMessage.message = lastMessage;
                        existingChatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    }
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    if (conversationExists) {
                        // 更新现有的聊天记录
                        ChatMessage existingChatMessage = conversations.get(existingConversationIndex);
                        existingChatMessage.message = lastMessage;
                        existingChatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                        existingChatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    }
                }
            }

            // 排序并刷新适配器
            conversations.sort((obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationsAdapter.notifyDataSetChanged();

            // 设置滚动到顶部并显示视图
            if (binding != null) {
                binding.conversationsRecyclerView.smoothScrollToPosition(0);
                binding.conversationsRecyclerView.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
            }
        }
    };

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMM. dd, hh:mm a", Locale.getDefault()).format(date);
    }

    private void updateToken(String token){
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                // .addOnSuccessListener(unused -> showToast("Token updated successfully")) //用于测试token更新是否成功 正常情况下不需要
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Unable to update token", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onConversationClicked(User user) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        isRemoved.launch(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        listenConversations();
    }
}
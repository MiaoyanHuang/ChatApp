package com.hmy.chatapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.hmy.chatapp.adapters.AddFriendAdapter;
import com.hmy.chatapp.databinding.ActivityAddFriendBinding;
import com.hmy.chatapp.listeners.AddFriendListener;
import com.hmy.chatapp.models.User;
import com.hmy.chatapp.utilities.Constants;
import com.hmy.chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.List;

public class AddFriendActivity extends BaseActivity implements AddFriendListener{

    private ActivityAddFriendBinding binding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddFriendBinding.inflate(getLayoutInflater());
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        binding.backBtn.setOnClickListener(v -> {
            onBackPressed();
            finish();
        });
        binding.searchBtn.setOnClickListener(v -> {
            String email = binding.searchEdit.getText().toString().trim();
            if (email.isEmpty()) {
                binding.searchEdit.setError("Email cannot be empty");
                binding.searchEdit.requestFocus();
            } else {
                querySearchedUser(email); // 用于查询是否已经是好友
            }
        });
    }

    private void querySearchedUser(String email){
        binding.usersRecyclerView.setVisibility(View.GONE);
        loading(true);
        if (binding.searchEdit.getText().toString().equals(preferenceManager.getString(Constants.KEY_EMAIL))){
            loading(false);
            showMessages("You cannot add yourself");
            return;
        }
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().size() > 0) {
                        User user = new User();
                        user.setId(task.getResult().getDocuments().get(0).getId());
                        user.setName(task.getResult().getDocuments().get(0).getString(Constants.KEY_NAME));
                        user.setEmail(task.getResult().getDocuments().get(0).getString(Constants.KEY_EMAIL));
                        user.setImage(task.getResult().getDocuments().get(0).getString(Constants.KEY_IMAGE));
                        user.setToken(task.getResult().getDocuments().get(0).getString(Constants.KEY_FCM_TOKEN));
                        queryFriendList(user); //拿到user后，查询当前用户的好友列表验证是否已经是好友
                    } else { //如果没有查询到用户，说明该email的用户不存在，显示错误信息
                        loading(false);
                        binding.usersRecyclerView.setVisibility(View.GONE);
                        binding.textErrorMessage.setVisibility(View.VISIBLE);
                        binding.textErrorMessage.setText(String.format("%s", "No user found"));
                    }
                })
                .addOnFailureListener(e -> {
                    loading(false);
                    binding.progressBar.setVisibility(View.INVISIBLE);
                    showMessages("Unable to process request");
                });

    }

    private void queryFriendList(User user) {
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<String> friends = (List<String>) task.getResult().get(Constants.KEY_FRIENDS);
                        if (friends != null && friends.contains(user.getId())) {
                            loading(false);
                            showMessages("You are already friend");
                        } else {
                            querySentRequest(user); //如果不是好友，查询是否已经发送过好友请求
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    loading(false);
                    binding.progressBar.setVisibility(View.INVISIBLE);
                    showMessages("Unable to process request");
                });
    }

    private void querySentRequest(User user) {
        database.collection(Constants.KEY_COLLECTION_REQUEST_ADD_FRIEND)
                .whereEqualTo(Constants.KEY_REQUEST_ADD_REQUEST_FROM_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL))
                .whereEqualTo(Constants.KEY_REQUEST_ADD_REQUEST_TO_EMAIL, user.getEmail())
                .whereEqualTo(Constants.KEY_REQUEST_ADD_REQUEST_STATUS, Constants.KEY_REQUEST_ADD_REQUEST_STATUS_PENDING)
                .get()
                .addOnCompleteListener(task -> {
                    //将用户信息和是否已经发送过好友请求传入下一个函数
                    displayUser(user, task.isSuccessful() && task.getResult() != null && task.getResult().size() > 0);
                })
                .addOnFailureListener(e -> {
                    loading(false);
                    binding.progressBar.setVisibility(View.INVISIBLE);
                    showMessages("Unable to process request");
                });
    }

    private void displayUser(User user, boolean isRequestSent) { //根据是否已经发送过好友请求，显示不同的界面
        AddFriendAdapter addFriendAdapter = new AddFriendAdapter(user, isRequestSent,this);
        binding.usersRecyclerView.setAdapter(addFriendAdapter);
        binding.usersRecyclerView.setVisibility(View.VISIBLE);
        loading(false);
    }

    private void showMessages(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onAddClicked(User user) {
        HashMap<String, Object> request = new HashMap<>();
        request.put(Constants.KEY_REQUEST_ADD_REQUEST_FROM_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        request.put(Constants.KEY_REQUEST_ADD_REQUEST_FROM_NAME, preferenceManager.getString(Constants.KEY_NAME));
        request.put(Constants.KEY_REQUEST_ADD_REQUEST_FROM_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
        request.put(Constants.KEY_REQUEST_ADD_REQUEST_FROM_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
        request.put(Constants.KEY_REQUEST_ADD_REQUEST_TO_ID, user.getId());
        request.put(Constants.KEY_REQUEST_ADD_REQUEST_TO_NAME, user.getName());
        request.put(Constants.KEY_REQUEST_ADD_REQUEST_TO_EMAIL, user.getEmail());
        request.put(Constants.KEY_REQUEST_ADD_REQUEST_TO_IMAGE, user.getImage());
        request.put(Constants.KEY_REQUEST_ADD_REQUEST_STATUS, Constants.KEY_REQUEST_ADD_REQUEST_STATUS_PENDING);
        database.collection(Constants.KEY_COLLECTION_REQUEST_ADD_FRIEND)
                .add(request)
                .addOnSuccessListener(documentReference -> {
                    //binding.usersRecyclerView.setVisibility(View.GONE);
                    binding.textErrorMessage.setVisibility(View.VISIBLE);
                    showMessages("Request sent");
                })
                .addOnFailureListener(e -> {
                    binding.usersRecyclerView.setVisibility(View.GONE);
                    binding.textErrorMessage.setVisibility(View.VISIBLE);
                    showMessages("Error: " + e.getMessage());
                });
    }
}
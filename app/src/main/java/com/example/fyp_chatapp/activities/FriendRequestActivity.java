package com.example.fyp_chatapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.example.fyp_chatapp.adapters.FriendRequestAdapter;
import com.example.fyp_chatapp.databinding.ActivityFriendRequestBinding;
import com.example.fyp_chatapp.listeners.FriendRequestListener;
import com.example.fyp_chatapp.models.FriendRequest;
import com.example.fyp_chatapp.utilities.Constants;
import com.example.fyp_chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class FriendRequestActivity extends BaseActivity implements FriendRequestListener {

    private ActivityFriendRequestBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private List<FriendRequest> friendRequestList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFriendRequestBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        setListener();
        setContentView(binding.getRoot());
        queryFriendRequest();
    }

    private void setListener() {
        binding.backBtn.setOnClickListener(v -> onBackPressed());
    }

    private void queryFriendRequest() {
        loading(true);
        database.collection(Constants.KEY_COLLECTION_REQUEST_ADD_FRIEND)
                .whereEqualTo(Constants.KEY_REQUEST_ADD_REQUEST_TO_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_REQUEST_ADD_REQUEST_STATUS, Constants.KEY_REQUEST_ADD_REQUEST_STATUS_PENDING)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    if(task.isSuccessful() && task.getResult() != null){
                        friendRequestList = new ArrayList<>();
                        for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                            FriendRequest friendRequest = new FriendRequest();
                            friendRequest.documentId = documentSnapshot.getId();
                            friendRequest.requestFromId = documentSnapshot.getString(Constants.KEY_REQUEST_ADD_REQUEST_FROM_ID);
                            friendRequest.requestFromName = documentSnapshot.getString(Constants.KEY_REQUEST_ADD_REQUEST_FROM_NAME);
                            friendRequest.requestFromEmail = documentSnapshot.getString(Constants.KEY_REQUEST_ADD_REQUEST_FROM_EMAIL);
                            friendRequest.requestFromImage = documentSnapshot.getString(Constants.KEY_REQUEST_ADD_REQUEST_FROM_IMAGE);
                            friendRequestList.add(friendRequest);
                        }
                        if (friendRequestList.size() > 0) {
                            Log.d("TAG", "queryFriendRequest: " + friendRequestList.size());
                            binding.friendRequestRecyclerView.setAdapter(new FriendRequestAdapter(friendRequestList, this));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    loading(false);
                    Toast.makeText(this, "Unable to fetch friend request", Toast.LENGTH_SHORT).show();
                });
    }

    private void loading(boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void addFriend(FriendRequest friendRequest){
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .update("friends", FieldValue.arrayUnion(friendRequest.requestFromId))
                .addOnSuccessListener(task -> database.collection(Constants.KEY_COLLECTION_USERS)
                        .document(friendRequest.requestFromId)
                        .update("friends", FieldValue.arrayUnion(preferenceManager.getString(Constants.KEY_USER_ID)))
                        .addOnSuccessListener(task1 -> {
                            Toast.makeText(this, "Friend added", Toast.LENGTH_SHORT).show();
                            updateRequest(true, friendRequest);
                        }));
    }

    private void updateRequest(boolean isAccepted, FriendRequest friendRequest) {
        database.collection(Constants.KEY_COLLECTION_REQUEST_ADD_FRIEND)
                .document(friendRequest.documentId)
                .update(Constants.KEY_REQUEST_ADD_REQUEST_STATUS,
                        isAccepted ? Constants.KEY_REQUEST_ADD_REQUEST_STATUS_ACCEPTED : Constants.KEY_REQUEST_ADD_REQUEST_STATUS_REJECTED)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Friend request rejected", Toast.LENGTH_SHORT).show();
                    friendRequestList.remove(friendRequest);
                    //更新recyclerview
                    binding.friendRequestRecyclerView.setAdapter(new FriendRequestAdapter(friendRequestList, this));
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Unable to reject friend request", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onAcceptRequest(FriendRequest friendRequest) {
        addFriend(friendRequest);
    }

    @Override
    public void onRejectRequest(FriendRequest friendRequest) {
        updateRequest(false, friendRequest);
    }
}
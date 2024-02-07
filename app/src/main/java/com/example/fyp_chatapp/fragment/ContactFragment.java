package com.example.fyp_chatapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.fyp_chatapp.activities.AddFriendActivity;
import com.example.fyp_chatapp.activities.FriendProfileActivity;
import com.example.fyp_chatapp.activities.FriendRequestActivity;
import com.example.fyp_chatapp.adapters.UsersAdapter;
import com.example.fyp_chatapp.databinding.FragmentContactBinding;
import com.example.fyp_chatapp.listeners.UserListener;
import com.example.fyp_chatapp.models.User;
import com.example.fyp_chatapp.utilities.Constants;
import com.example.fyp_chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ContactFragment extends Fragment implements UserListener {

    private FragmentContactBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentContactBinding.inflate(inflater, container, false);
        preferenceManager = new PreferenceManager(Objects.requireNonNull(getActivity()));
        database = FirebaseFirestore.getInstance();
        init(); //initialize variables
        //getUsers(); //initialize users
        queryFriend();
        queryFriendRequest(); //initialize friend request
        return binding.getRoot();
    }

    private void queryFriendRequest() {
        binding.requestBlock.setVisibility(View.GONE);
        database.collection(Constants.KEY_COLLECTION_REQUEST_ADD_FRIEND)
                .whereEqualTo(Constants.KEY_REQUEST_ADD_REQUEST_TO_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_REQUEST_ADD_REQUEST_STATUS, Constants.KEY_REQUEST_ADD_REQUEST_STATUS_PENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult().size() != 0){
                        Log.d("TAG", "SIZE::: " + task.getResult().size());
                        String message = "You have " + task.getResult().size() + " friend request";
                        if(binding != null){
                            binding.requestBlock.setText(message);
                            binding.requestBlock.setVisibility(View.VISIBLE);
                            binding.requestBlock.setOnClickListener(v -> startActivity(new Intent(getActivity(), FriendRequestActivity.class)));
                        }
                    }
                });
    }


    private void init() {
        binding.imageAdd.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddFriendActivity.class)));
    }

    private void queryFriend(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    if(task.isSuccessful() && task.getResult() != null){
                        List<String> friends = new ArrayList<>();
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if(documentSnapshot.get(Constants.KEY_FRIENDS) != null){
                            friends = (List<String>) documentSnapshot.get(Constants.KEY_FRIENDS);
                        }
                        if (friends != null && friends.size() > 0) {
                            Log.d("TAG", "friends size: " + friends.size());
                            getFriend(friends);
                        } else {
                            loading(false);
                        }
                    }

                });

    }

    private void getFriend(List<String> friends) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereIn(FieldPath.documentId(), friends)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null){
                        List<User> users = new ArrayList<>();
                        for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                            User user = new User();
                            user.name = documentSnapshot.getString(Constants.KEY_NAME);
                            user.email = documentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = documentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = documentSnapshot.getId();
                            users.add(user);
                        }
                        if(getActivity() != null && users.size() > 0){
                            Log.d("TAG", "getFriend: " + users.size());
                            UsersAdapter usersAdapter = new UsersAdapter(users,this);
                            binding.friendRecyclerView.setAdapter(usersAdapter);
                            loading(false);
                        }
                    }
                });
    }

    private void loading(Boolean isLoading){
        if(getActivity() != null){
            if(isLoading){
                binding.progressBar.setVisibility(View.VISIBLE);
            }else{
                binding.progressBar.setVisibility(View.INVISIBLE);
            }
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        queryFriendRequest();
        queryFriend();
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getActivity(), FriendProfileActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }
}
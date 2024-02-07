package com.example.fyp_chatapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.fyp_chatapp.databinding.ActivityUpdateProfileBinding;
import com.example.fyp_chatapp.utilities.Constants;
import com.example.fyp_chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;

public class UpdateProfileActivity extends BaseActivity {

    private ActivityUpdateProfileBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        getProfile();
        init();
    }

    private void init(){
        binding.uploadBtn.setOnClickListener(v -> updateProfile());
        binding.backBtn.setOnClickListener(v -> onBackPressed());
    }

    private void updateProfile() {
        //将用户输入的信息更新到firestore中
        loading(true);
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_REAL_NAME, binding.inputRealName.getText().toString());
        updates.put(Constants.KEY_GENDER, binding.inputGender.getText().toString());
        updates.put(Constants.KEY_AGE, binding.inputAge.getText().toString());
        updates.put(Constants.KEY_DOB, binding.inputDob.getText().toString());
        updates.put(Constants.KEY_REGION, binding.inputRegion.getText().toString());
        updates.put(Constants.KEY_PHONE, binding.inputPhoneNo.getText().toString());
        updates.put(Constants.KEY_OCCUPATION, binding.inputOccupation.getText().toString());
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .update(updates)
                .addOnSuccessListener(unused -> {
                    loading(false);
                    showMessages("Profile updated successfully");
                    finish();
                })
                .addOnFailureListener(e -> {
                    loading(false);
                    showMessages(e.getMessage());
                });

    }

    private void showMessages(String message) {
        Toast.makeText(UpdateProfileActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.uploadBtn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.uploadBtn.setVisibility(View.VISIBLE);
        }
    }

    private void getProfile() {
        //从firestore的名字为user的collection中获取当前用户的信息，并设置到UI上
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    //如果获取到了用户信息
                    if (task.isSuccessful() && task.getResult() != null) {
                        //将用户信息设置到UI上
                        binding.inputRealName.setText(task.getResult().getString(Constants.KEY_REAL_NAME));
                        binding.inputGender.setText(task.getResult().getString(Constants.KEY_GENDER));
                        binding.inputAge.setText(task.getResult().getString(Constants.KEY_AGE));
                        binding.inputDob.setText(task.getResult().getString(Constants.KEY_DOB));
                        binding.inputRegion.setText(task.getResult().getString(Constants.KEY_REGION));
                        binding.inputPhoneNo.setText(task.getResult().getString(Constants.KEY_PHONE));
                        binding.inputOccupation.setText(task.getResult().getString(Constants.KEY_OCCUPATION));
                    }
                });
    }
}
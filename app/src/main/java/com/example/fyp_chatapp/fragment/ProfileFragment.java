package com.example.fyp_chatapp.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.fyp_chatapp.activities.UpdateProfileActivity;
import com.example.fyp_chatapp.activities.ViewAvatarActivity;
import com.example.fyp_chatapp.activities.MainActivity;
import com.example.fyp_chatapp.databinding.FragmentProfileBinding;
import com.example.fyp_chatapp.utilities.Constants;
import com.example.fyp_chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        preferenceManager = new PreferenceManager(Objects.requireNonNull(getContext()));
        database = FirebaseFirestore.getInstance();
        setListeners();
        getProfile();
        return binding.getRoot();
    }

    private void getProfile() {
        //从firestore中获取user的collection中当前用户的realName， age， region， dob并设置到视图中
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if (getActivity()!=null && task.isSuccessful() && task.getResult() != null){
                        String encodedImage = (String) task.getResult().get(Constants.KEY_IMAGE);
                        byte[] bytes = Base64.decode(encodedImage, android.util.Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        binding.photo.setImageBitmap(bitmap);
                        binding.email.setText(task.getResult().getString(Constants.KEY_EMAIL));
                        binding.nickName.setText(task.getResult().getString(Constants.KEY_NAME));
                        binding.realName.setText(task.getResult().getString(Constants.KEY_REAL_NAME));
                        binding.gender.setText(task.getResult().getString(Constants.KEY_GENDER));
                        binding.age.setText(task.getResult().getString(Constants.KEY_AGE));
                        binding.birthday.setText(task.getResult().getString(Constants.KEY_REGION));
                        binding.region.setText(task.getResult().getString(Constants.KEY_DOB));
                        binding.phoneNo.setText(task.getResult().getString(Constants.KEY_PHONE));
                        binding.occupation.setText(task.getResult().getString(Constants.KEY_OCCUPATION));
                    }
                });
    }

    private void setListeners() {
        binding.signOutBtn.setOnClickListener(v -> signOut());
        binding.viewAvatarBtn.setOnClickListener(v -> startActivity(new Intent(getContext(), ViewAvatarActivity.class)));
        binding.updateProfileBtn.setOnClickListener(v -> startActivity(new Intent(getContext(), UpdateProfileActivity.class)));
    }

    private void signOut(){
        showToast("Signing out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, null);
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getContext(), MainActivity.class));
                    Objects.requireNonNull(getActivity()).finish();
                })
                .addOnFailureListener(e -> showToast("Unable to sign out"));
    }

    private void showToast(String message){
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        getProfile();
    }
}
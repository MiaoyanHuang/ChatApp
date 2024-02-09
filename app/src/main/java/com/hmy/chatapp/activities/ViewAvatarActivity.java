package com.hmy.chatapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.hmy.chatapp.databinding.ActivityViewAvatarBinding;
import com.hmy.chatapp.utilities.Constants;
import com.hmy.chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ViewAvatarActivity extends BaseActivity {

    private ActivityViewAvatarBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewAvatarBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        setContentView(binding.getRoot());
        inti();
        getAvatar();
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK){
                    if (result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageAvatar.setImageBitmap(bitmap);
                            String userImage = encodeImage(bitmap);
                            updateImage(userImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private void updateImage(String userImage) {
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .update(Constants.KEY_IMAGE, userImage)
                .addOnSuccessListener(unused -> {
                    showToast("Image updated successfully");
                    preferenceManager.putString(Constants.KEY_IMAGE, userImage);
                }).addOnFailureListener(e -> showToast("Unable to update image"));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void getAvatar() {
        loading(true);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    if(task.isSuccessful() && task.getResult() != null){
                        String image = task.getResult().getString(Constants.KEY_IMAGE);
                        if(image != null){
                            byte[] bytes = Base64.decode(image, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            binding.imageAvatar.setImageBitmap(bitmap);
                        }
                    }
                }).addOnFailureListener(e -> {
                    loading(false);
                    Toast.makeText(ViewAvatarActivity.this, "Unable to get avatar from database", Toast.LENGTH_SHORT).show();
                });
    }

    private void loading(Boolean isLoading) {
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void inti() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.updatePhotoBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private String encodeImage(Bitmap image){
        int previewWidth = 150;
        int previewHeight = image.getHeight() / (image.getWidth() / previewWidth);
        Bitmap previewBitmap = Bitmap.createScaledBitmap(image, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
}
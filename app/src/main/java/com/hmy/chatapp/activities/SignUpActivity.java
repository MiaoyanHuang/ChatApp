package com.hmy.chatapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.hmy.chatapp.databinding.ActivitySignUpBinding;
import com.hmy.chatapp.utilities.PreferenceManager;
import com.hmy.chatapp.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners(); //initialize listeners
    }

    private void setListeners() {
        binding.textSignIn.setOnClickListener(v ->
            onBackPressed() //go back to previous activity
        );
        binding.buttonSignUp.setOnClickListener(v -> {
            if(isValidSignUpDetails()){
                checkIsEmailExists();
            }
        });
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void checkIsEmailExists() { //check if email already exists
        loading(true); //打开loading progress bar
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && !task.getResult().getDocuments().isEmpty()){
                        loading(false);
                        showToast("Email already exists, please use another email");
                    } else {
                        signUp();
                    }
                })
                .addOnFailureListener(e -> {
                    loading(false);
                    showToast(e.getMessage());
                });
    }

    private void signUp(){
        HashMap<String, Object> user = new HashMap<>();
        /*
        首先你需要创建一个HashMap来储存你的user信息
        你可以用put()方法来添加信息
        示例： user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
        注意：1. HashMap的Key：Constants.KEY_NAME 是你在Firebase中储存username值的key叫什么名字 我这里是用一个constant class来储存这些值
             2. HashMap的Value：binding.inputName.getText().toString()是从UI上获取用户界面上输入框id为“inputName“的component的值
        假如你的Firebase database保存username值的key为name 然后输入框的id为”inputName“ 可以这样写：
        user.put("name", binding.inputName.getText().toString());
         */
        user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
        user.put(Constants.KEY_IMAGE, encodedImage);

        // 如果你需要声明一个FirebaseFirestore的对象
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        // 通过collection()方法来指定你要保存信息的collection的名字 这个方法需要传入一个String类型的参数 也就是你要把数据放在哪个collection的名字
        database.collection(Constants.KEY_COLLECTION_USERS) //  Constants.KEY_COLLECTION_USERS的值是"user"
                .add(user) //通过add()方法来添加数据到collection中 这个方法需要传入一个HashMap类型的参数 也就是你之前添加到HashMap中的数据
                .addOnSuccessListener(documentReference -> { // 如果添加成功后的执行逻辑
                    // 以下是我自己写的成功添加到firebase的逻辑
                    // 主要是当用户成功注册后 自动登录用户刚刚注册的账号 并保存用户的信息为全局变量
                    // 你可以删除这段代码 换成你自己的逻辑
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                    loading(false);
                    Intent intent = new Intent(getApplicationContext(), HomepageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> { // 如果添加失败后的执行逻辑
                    loading(false);
                    showToast(e.getMessage());
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
    
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK){
                    if (result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap image = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(image);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodeImage(image);
                        } catch (IOException e) {
                            showToast(e.getMessage());
                        }
                    }
                }
            }
    );

    public Boolean isValidSignUpDetails(){
        if(encodedImage == null){
            showToast("Please select a profile picture");
            return false;
        } else if (binding.inputName.getText().toString().trim().isEmpty()){
            showToast("Please enter your name");
            return false;
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Please enter your email");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Please enter your password");
            return false;
        } else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()){
            showToast("Please confirm your password");
            return false;
        } else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())){
            showToast("Passwords do not match");
            return false;
        } else {
            return true;
        }
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
        }
    }
}
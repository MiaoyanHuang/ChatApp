package com.hmy.chatapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.hmy.chatapp.databinding.ActivityFriendProfileBinding;
import com.hmy.chatapp.utilities.PreferenceManager;
import com.hmy.chatapp.models.User;
import com.hmy.chatapp.utilities.Constants;
import com.hmy.chatapp.utilities.FileUtils;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FriendProfileActivity extends BaseActivity {

    private ActivityFriendProfileBinding binding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFriendProfileBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra(Constants.KEY_USER);
        init();
        displayProfile(user);
    }

    //取代原本的startActivityForResult
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK){
                    if (result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            saveBackgroundImageToTempDir(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private void init() {
        Intent intent = getIntent();
        boolean isFromChatActivity = intent.getBooleanExtra("isFromChatActivity", false);
        if(isFromChatActivity){
            binding.sendMsgBtn.setVisibility(View.INVISIBLE);
        } else {
            binding.setBackgroundBtn.setVisibility(View.INVISIBLE);
        }

        binding.backBtn.setOnClickListener(v -> {
            onBackPressed();
            finish();
        });
        binding.sendMsgBtn.setOnClickListener(v -> {
            Intent intent1 = new Intent(getApplicationContext(), ChatActivity.class);
            intent1.putExtra(Constants.KEY_USER, user);
            startActivity(intent1);
            finish();
        });
        binding.removeBtn.setOnClickListener(v -> {
            removeFriend();
            //setResult(RESULT_OK);finish();
        });
        binding.setBackgroundBtn.setOnClickListener(v ->
                pickImage.launch(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
        );
    }

    private void removeFriend() {
        //从当前用户的好友列表中删除
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .update(Constants.KEY_FRIENDS, FieldValue.arrayRemove(user.getId()));

        //从对方的好友列表中删除当前用户
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(user.getId())
                .update(Constants.KEY_FRIENDS, FieldValue.arrayRemove(preferenceManager.getString(Constants.KEY_USER_ID)));

        //删除当前用户的近期的会话
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, user.getId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        String documentId = task.getResult().getDocuments().get(0).getId();
                        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                                .document(documentId)
                                .delete();
                    }
                });

        //删除被删用户的近期的会话
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, user.getId())
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful() && task1.getResult() != null && task1.getResult().getDocuments().size() > 0) {
                        String documentId = task1.getResult().getDocuments().get(0).getId();
                        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                                .document(documentId)
                                .delete();
                    }
                });

        //删除聊天记录
        database.collection(Constants.KEY_COLLECTION_CHAT).whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, user.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        documentSnapshot.getReference().delete();
                    }
                });

        //删除聊天记录
        database.collection(Constants.KEY_COLLECTION_CHAT).whereEqualTo(Constants.KEY_SENDER_ID, user.getId())
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        documentSnapshot.getReference().delete();
                    }
                    showMessage(user.getName() + " has been removed");
                    setResult(RESULT_OK);
                    finish();
                });
    }

    private void showMessage(String friend_removed) {
        Toast.makeText(getApplicationContext(), friend_removed, Toast.LENGTH_SHORT).show();
    }

    private void displayProfile(User user) {
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(user.getId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot != null){
                        binding.photo.setImageBitmap(getBitmapFromEncodedString(documentSnapshot.getString(Constants.KEY_IMAGE)));
                        binding.nickName.setText(documentSnapshot.getString(Constants.KEY_NAME));
                        binding.email.setText(documentSnapshot.getString(Constants.KEY_EMAIL));
                        binding.realName.setText(documentSnapshot.getString(Constants.KEY_REAL_NAME));
                        binding.gender.setText(documentSnapshot.getString(Constants.KEY_GENDER));
                        binding.age.setText(documentSnapshot.getString(Constants.KEY_AGE));
                        binding.birthday.setText(documentSnapshot.getString(Constants.KEY_DOB));
                        binding.region.setText(documentSnapshot.getString(Constants.KEY_REGION));
                        binding.phoneNo.setText(documentSnapshot.getString(Constants.KEY_PHONE));
                        binding.occupation.setText(documentSnapshot.getString(Constants.KEY_OCCUPATION));
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(FriendProfileActivity.this, "Unable to get user profile", Toast.LENGTH_SHORT).show());
    }

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if(encodedImage != null){
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT); //将Base64编码的字符串解码为字节数组
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length); //将字节数组解码Bitmap对象
        }
        return null;
    }


    private void saveBackgroundImageToTempDir(Bitmap bitmap) { //设置聊天背景
        String tempDirPath = FileUtils.getTempDirPath(this); // 获取临时目录路径，例如：/data/data/com.example.myapp/temp/
        String userId = preferenceManager.getString(Constants.KEY_USER_ID); // 获取当前用户的id
        String friendId = user.getId(); // 获取好友的id
        String filename = "background_" + userId + "&" + friendId + ".png"; // 为照片添加userId + friendId的名称
        File tempFile = new File(tempDirPath, filename);

        try {
            FileOutputStream out = new FileOutputStream(tempFile); // 创建文件输出流
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Unable to set background");
        }
    }

}
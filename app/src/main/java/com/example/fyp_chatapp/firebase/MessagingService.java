package com.example.fyp_chatapp.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.fyp_chatapp.R;
import com.example.fyp_chatapp.activities.ChatActivity;
import com.example.fyp_chatapp.models.User;
import com.example.fyp_chatapp.utilities.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "MessagingService";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Token: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        User user = new User();
        user.id = remoteMessage.getData().get(Constants.KEY_USER_ID);
        user.name = remoteMessage.getData().get(Constants.KEY_NAME);
        user.token = remoteMessage.getData().get(Constants.KEY_FCM_TOKEN);

        int notificationId = new Random().nextInt(); //用于生成随机数
        String channelId = "chat_message"; //通知渠道的id

        Intent intent = new Intent(this, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //设置intent的标志位,清除栈顶的activity,并且创建新的activity,这样就不会出现重复的activity
        intent.putExtra(Constants.KEY_USER, user);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE); //PendingIntent是一种特殊的Intent,它的意图是包装其他应用的Intent,它可以在其他应用中执行

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentTitle(user.name);
        builder.setContentText(remoteMessage.getData().get(Constants.KEY_MESSAGE));
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(
                remoteMessage.getData().get(Constants.KEY_MESSAGE)
        ));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent); //设置通知的点击事件
        builder.setAutoCancel(true); //设置点击通知后自动取消通知
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC); //设置通知的可见性

        CharSequence channelName = "Chat Message"; //通知渠道的名称
        String channelDescription = "This notification channel is used for chat message notification"; //通知渠道的描述
        int importance = NotificationManager.IMPORTANCE_DEFAULT; //通知渠道的重要性

        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance); //创建通知渠道
        channel.setDescription(channelDescription); //设置通知渠道的描述

        NotificationManager notificationManager = getSystemService(NotificationManager.class); //获取通知管理器
        notificationManager.createNotificationChannel(channel); //创建通知渠道
        notificationManager.notify(notificationId, builder.build()); //发送通知
    }
}

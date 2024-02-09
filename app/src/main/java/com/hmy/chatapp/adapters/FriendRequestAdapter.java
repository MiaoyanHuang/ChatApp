package com.hmy.chatapp.adapters;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hmy.chatapp.listeners.FriendRequestListener;
import com.hmy.chatapp.databinding.ItemContainerFriendRequestBinding;
import com.hmy.chatapp.models.FriendRequest;

import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.FriendRequestViewHolder>{

    private final List<FriendRequest> users;
    private final FriendRequestListener friendRequestListener;

    public FriendRequestAdapter(List<FriendRequest> users, FriendRequestListener friendRequestListener) {
        this.users = users;
        this.friendRequestListener = friendRequestListener;
    }

    @NonNull
    @Override
    public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerFriendRequestBinding  itemContainerFriendRequestBinding= ItemContainerFriendRequestBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FriendRequestViewHolder(itemContainerFriendRequestBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        //Log.d("TAG", "getItemCount: "+users.size());
        return users.size();
    }

    class FriendRequestViewHolder extends RecyclerView.ViewHolder {

        final ItemContainerFriendRequestBinding binding;

        public FriendRequestViewHolder(@NonNull ItemContainerFriendRequestBinding itemContainerFriendRequestBinding) {
            super(itemContainerFriendRequestBinding.getRoot());
            this.binding = itemContainerFriendRequestBinding;
        }

        void setUserData(@NonNull FriendRequest friendRequest){
            binding.textName.setText(friendRequest.getRequestFromName());
            binding.textEmail.setText(friendRequest.getRequestFromEmail());
            binding.imageProfile.setImageBitmap(getUserImage(friendRequest.getRequestFromImage()));
            binding.acceptBtn.setOnClickListener(v -> {
                binding.progressBar.setVisibility(View.VISIBLE);
                friendRequestListener.onAcceptRequest(friendRequest);
                binding.progressBar.setVisibility(View.INVISIBLE);
            });
            binding.rejectBtn.setOnClickListener(v -> {
                binding.progressBar.setVisibility(View.VISIBLE);
                friendRequestListener.onRejectRequest(friendRequest);
                binding.progressBar.setVisibility(View.INVISIBLE);
            });
        }
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}

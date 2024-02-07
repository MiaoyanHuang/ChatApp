package com.example.fyp_chatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fyp_chatapp.databinding.ItemContainerAddFriendBinding;
import com.example.fyp_chatapp.listeners.AddFriendListener;
import com.example.fyp_chatapp.models.User;

public class AddFriendAdapter extends RecyclerView.Adapter<AddFriendAdapter.AddFriendViewHolder>{

    private final User users;
    private final AddFriendListener addFriendListener;
    private final Boolean isRequest;

    public AddFriendAdapter(User users, Boolean isRequest, AddFriendListener addFriendListener) {
        this.users = users;
        this.addFriendListener = addFriendListener;
        this.isRequest = isRequest;
    }

    @NonNull
    @Override
    public AddFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerAddFriendBinding itemContainerAddFriendBinding = ItemContainerAddFriendBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new AddFriendViewHolder(itemContainerAddFriendBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull AddFriendViewHolder holder, int position) {
        holder.setUserData(users);
    }

    @Override
    public int getItemCount() {
        return users == null ? 0 : 1;
    }

    class AddFriendViewHolder extends RecyclerView.ViewHolder {

        final ItemContainerAddFriendBinding binding;

        public AddFriendViewHolder(ItemContainerAddFriendBinding itemContainerAddFriendBinding) {
            super(itemContainerAddFriendBinding.getRoot());
            this.binding = itemContainerAddFriendBinding;
        }

        void setUserData(User user){
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            if (isRequest){
                binding.textPending.setVisibility(View.VISIBLE);
                binding.addBtn.setVisibility(View.GONE);
            } else {
                binding.textPending.setVisibility(View.GONE);
                binding.addBtn.setVisibility(View.VISIBLE);
                binding.addBtn.setOnClickListener(v -> {
                    addFriendListener.onAddClicked(user);
                    binding.addBtn.setVisibility(View.GONE);
                    binding.textPending.setVisibility(View.VISIBLE);
                });
            }
        }
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}

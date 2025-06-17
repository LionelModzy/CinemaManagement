package ai.movie.modzy.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

import ai.movie.modzy.Model.User;
import ai.movie.modzy.R;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> userList;
    private Context context;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onEdit(User user);
        void onDelete(User user);
    }

    public UserAdapter(List<User> userList, Context context, OnUserActionListener listener) {
        this.userList = userList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvName.setText(user.getName());
        holder.tvEmail.setText(user.getEmail());
        holder.tvRole.setText(user.getRole());

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(context).load(user.getAvatarUrl()).into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_person);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(user));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar, btnEdit, btnDelete;
        TextView tvName, tvEmail, tvRole;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRole = itemView.findViewById(R.id.tvRole);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}


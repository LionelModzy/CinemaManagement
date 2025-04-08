package ai.movie.modzy.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import ai.movie.modzy.Activity.Movie.AddMovieActivity;
import ai.movie.modzy.Adapter.MovieAdapter;
import ai.movie.modzy.Model.Movies;
import ai.movie.modzy.R;

public class HomeFragment extends Fragment {
    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_home_fragment, container, false);

        // TODO: Viết UI khác cho Home, ví dụ text chào mừng, ảnh động, banner...

        return view;
    }
}


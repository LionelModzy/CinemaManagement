// HomeFragment.java
package ai.movie.modzy.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.movie.modzy.Activity.Movie.GenreMoviesActivity;
import ai.movie.modzy.Adapter.GenreMovieAdapter;
import ai.movie.modzy.Adapter.MovieGridAdapter;
import ai.movie.modzy.Adapter.MovieSliderAdapter;
import ai.movie.modzy.Model.Movies;
import ai.movie.modzy.R;
import ai.movie.modzy.Adapter.SimpleTextWatcher;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private TextView filterMenu;
    private RecyclerView recyclerMovies;
    private androidx.appcompat.widget.AppCompatEditText searchHotMovies;

    private FirebaseFirestore db;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private MovieSliderAdapter sliderAdapter;
    private MovieGridAdapter gridAdapter;

    private List<Movies> allMoviesList = new ArrayList<>();
    private List<Movies> hotMoviesList = new ArrayList<>();
    // Thêm các biến mới
    private LinearLayout genreContainer;
    private Map<String, List<Movies>> moviesByGenre = new HashMap<>();
    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_home_fragment, container, false);

        viewPager       = view.findViewById(R.id.viewpager_hot_movies);
        tabLayout       = view.findViewById(R.id.tab_indicator);
        searchHotMovies = view.findViewById(R.id.search_hot_movies);
        filterMenu      = view.findViewById(R.id.search_movies);
        recyclerMovies  = view.findViewById(R.id.recycler_movies);
        genreContainer = view.findViewById(R.id.genre_container);
        db = FirebaseFirestore.getInstance();

        setupSlider();
        setupGrid();
        loadHotMovies();
        loadAllMovies();
        startAutoSlide();
        setupSearch();
        setupFilterMenu();
        loadMoviesByGenre();
        return view;
    }

    private void setupSlider() {
        sliderAdapter = new MovieSliderAdapter(requireContext(), new ArrayList<>());
        viewPager.setAdapter(sliderAdapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setPageTransformer((page, position) -> {
            float offset = position * -30;
            page.setTranslationX(offset);
            float scale = 0.9f + (1 - Math.abs(position)) * 0.1f;
            page.setScaleX(scale);
            page.setScaleY(scale);
        });
        new TabLayoutMediator(tabLayout, viewPager, (tab, pos) -> {}).attach();
    }

    private void setupGrid() {
        recyclerMovies.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        gridAdapter = new MovieGridAdapter(requireContext(), new ArrayList<>());
        recyclerMovies.setAdapter(gridAdapter);
        recyclerMovies.setNestedScrollingEnabled(false);
    }

    private void setupSearch() {
        searchHotMovies.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterHot(s.toString());
                filterGrid(s.toString());
            }
        });
    }

    private void setupFilterMenu() {
        filterMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), v);
            popup.getMenu().add("Tên A → Z");
            popup.getMenu().add("Tên Z → A");
            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (title.contains("A → Z")) {
                    Collections.sort(allMoviesList, Comparator.comparing(m -> m.getTitle().toLowerCase()));
                } else {
                    Collections.sort(allMoviesList, (m1, m2) -> m2.getTitle().toLowerCase()
                            .compareTo(m1.getTitle().toLowerCase()));
                }
                gridAdapter.setMovieList(new ArrayList<>(allMoviesList));
                return true;
            });
            popup.show();
        });
    }

    private void filterHot(String query) {
        List<Movies> temp = new ArrayList<>();
        for (Movies m : hotMoviesList) {
            if (m.getTitle().toLowerCase().contains(query.toLowerCase())) {
                temp.add(m);
            }
        }
        sliderAdapter.setMovieList(temp);
    }

    private void filterGrid(String query) {
        List<Movies> temp = new ArrayList<>();
        for (Movies m : allMoviesList) {
            if (m.getTitle().toLowerCase().contains(query.toLowerCase())) {
                temp.add(m);
            }
        }
        gridAdapter.setMovieList(temp);
    }

    private void startAutoSlide() {
        sliderRunnable = () -> {
            if (sliderAdapter.getItemCount() > 0) {
                int next = (viewPager.getCurrentItem() + 1) % sliderAdapter.getItemCount();
                viewPager.setCurrentItem(next, true);
            }
            sliderHandler.postDelayed(sliderRunnable, 3000);
        };
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    private void loadHotMovies() {
        db.collection("movies")
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(snap -> {
                    hotMoviesList.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        hotMoviesList.add(doc.toObject(Movies.class));
                    }
                    sliderAdapter.setMovieList(new ArrayList<>(hotMoviesList));
                })
                .addOnFailureListener(e -> Log.e("HomeFragment", "Error hot", e));
    }

    private void loadAllMovies() {
        db.collection("movies")
                .orderBy("title", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    allMoviesList.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        allMoviesList.add(doc.toObject(Movies.class));
                    }
                    gridAdapter.setMovieList(new ArrayList<>(allMoviesList));
                })
                .addOnFailureListener(e -> Log.e("HomeFragment", "Error all", e));
    }
    private void loadMoviesByGenre() {
        db.collection("movies")
                .get()
                .addOnSuccessListener(snap -> {
                    allMoviesList.clear();
                    moviesByGenre.clear();

                    for (QueryDocumentSnapshot doc : snap) {
                        Movies movie = doc.toObject(Movies.class);
                        allMoviesList.add(movie);

                        // Phân loại phim theo thể loại
                        String[] genres = movie.getGenre().split(", ");
                        for (String genre : genres) {
                            if (!moviesByGenre.containsKey(genre)) {
                                moviesByGenre.put(genre, new ArrayList<>());
                            }
                            moviesByGenre.get(genre).add(movie);
                        }
                    }

                    // Hiển thị các thể loại
                    displayGenreSections();

                    // Cập nhật grid adapter
                    gridAdapter.setMovieList(new ArrayList<>(allMoviesList));
                })
                .addOnFailureListener(e -> Log.e("HomeFragment", "Error loading movies by genre", e));
    }

    private void displayGenreSections() {
        genreContainer.removeAllViews();

        for (Map.Entry<String, List<Movies>> entry : moviesByGenre.entrySet()) {
            if (entry.getValue().size() > 0) {
                View genreView = LayoutInflater.from(requireContext())
                        .inflate(R.layout.section_genre_movies, genreContainer, false);

                TextView genreTitle = genreView.findViewById(R.id.tv_genre_title);
                TextView viewAll = genreView.findViewById(R.id.tv_view_all);
                RecyclerView recyclerGenreMovies = genreView.findViewById(R.id.recycler_genre_movies);

                String genre = entry.getKey();
                genreTitle.setText(genre);

                // Setup RecyclerView ngang
                recyclerGenreMovies.setLayoutManager(new LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                ));
                recyclerGenreMovies.setAdapter(new GenreMovieAdapter(
                        requireContext(),
                        entry.getValue(),
                        genre
                ));

                // Xử lý click "Xem tất cả"
                viewAll.setOnClickListener(v -> {
                    Intent intent = new Intent(requireContext(), GenreMoviesActivity.class);
                    intent.putExtra("genre", genre);
                    startActivity(intent);
                });

                genreContainer.addView(genreView);
            }
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sliderHandler.removeCallbacks(sliderRunnable);
    }
}

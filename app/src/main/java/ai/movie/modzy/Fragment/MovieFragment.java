package ai.movie.modzy.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.widget.SearchView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.text.Normalizer;
import java.util.regex.Pattern;
import ai.movie.modzy.Activity.Movie.AddMovieActivity;
import ai.movie.modzy.Adapter.MovieAdapter;
import ai.movie.modzy.Api.SaveNewMovieFromApiToFirestore;
import ai.movie.modzy.Model.Movies;
import ai.movie.modzy.R;

public class MovieFragment extends Fragment {
    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private List<Movies> movieList;
    private FirebaseFirestore db;
    private Button btnAddMovie;
    private String role;
    private Button btnLoadFromApi;
    private SearchView searchView;
    private Spinner spinnerSort;
    private List<Movies> currentList;
    private LinearLayout paginationLayout, layoutPaginationContainer;
    private Button btnPrev, btnNext, btnGoPage;
    private EditText editPage;
    private int currentPage = 1;
    private int itemsPerPage = 10;
    private int totalPages = 1;
    private TextView txtPageInfo;
    // Khai báo launcher để nhận kết quả từ AddMovieActivity
    private ActivityResultLauncher<Intent> addMovieLauncher;


    public MovieFragment() {}
    public static String removeVietnameseDiacritics(String str) {
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").replaceAll("đ", "d").replaceAll("Đ", "D");
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Khởi tạo launcher
        addMovieLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        loadMovies(); // Tải lại phim sau khi thêm/chỉnh sửa
                    }
                }
        );
    }

    @Nullable
    @Override
    public android.view.View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                          @Nullable Bundle savedInstanceState) {
        android.view.View view = inflater.inflate(R.layout.activity_movie_fragment, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
//        btnAddMovie = view.findViewById(R.id.btnAddMovie);
//        btnLoadFromApi = view.findViewById(R.id.btnLoadFromApi);

        movieList = new ArrayList<>();
        role = getArguments() != null ? getArguments().getString("role", "user") : "user";

        adapter = new MovieAdapter(requireContext(), movieList, role, addMovieLauncher);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        paginationLayout = view.findViewById(R.id.paginationLayout);
        btnPrev = view.findViewById(R.id.btnPrev);
        btnNext = view.findViewById(R.id.btnNext);
        btnGoPage = view.findViewById(R.id.btnGoPage);
        editPage = view.findViewById(R.id.editPage);
        txtPageInfo = view.findViewById(R.id.txtPageInfo);
        layoutPaginationContainer = view.findViewById(R.id.layoutPaginationContainer);


//        if ("admin".equals(role)) {
//            btnAddMovie.setVisibility(View.VISIBLE);
//            btnLoadFromApi.setVisibility(View.VISIBLE);
//        } else {
//            btnAddMovie.setVisibility(View.GONE);
//            btnLoadFromApi.setVisibility(View.GONE);
//        }

        db = FirebaseFirestore.getInstance();
        loadMovies();

        searchView = view.findViewById(R.id.searchView);
        spinnerSort = view.findViewById(R.id.spinnerSort);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterMovies(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterMovies(newText);
                return false;
            }
        });

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortMovies(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        btnPrev.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                paginateMovies();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                currentPage++;
                paginateMovies();
                // Cuộn thanh phân trang để hiển thị nút trang mới
                paginationLayout.post(() -> {
                    View lastChild = paginationLayout.getChildAt(paginationLayout.getChildCount() - 1);
                    if (lastChild != null) {
                        lastChild.requestFocus();
                    }
                });
            }
        });

        btnGoPage.setOnClickListener(v -> {
            String input = editPage.getText().toString();
            if (!input.isEmpty()) {
                int page = Integer.parseInt(input);
                if (page >= 1 && page <= totalPages) {
                    currentPage = page;
                    paginateMovies();
                } else {
                    Toast.makeText(getContext(), "Số trang không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    // Hiển thị phân trang khi cuộn gần đến cuối danh sách
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 1
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= itemsPerPage) {
                        layoutPaginationContainer.setVisibility(View.VISIBLE);
                        layoutPaginationContainer.animate().alpha(1f).setDuration(200);

                    } else {
                        layoutPaginationContainer.setVisibility(View.VISIBLE);
                        layoutPaginationContainer.animate().alpha(1f).setDuration(200);

                    }
                }
            }
        });




//        // Bấm nút "Thêm phim"
//        btnAddMovie.setOnClickListener(v -> {
//            Intent intent = new Intent(getActivity(), AddMovieActivity.class);
//            addMovieLauncher.launch(intent);
//        });
//// Bấm nút "Load phim từ API"
//        btnLoadFromApi.setOnClickListener(v -> {
//            Intent intent = new Intent(getActivity(), SaveNewMovieFromApiToFirestore.class);
//            startActivity(intent);
//        });
//        // Khi click vào phim để sửa
//        adapter.setOnMovieClickListener(movie -> {
//            Intent intent = new Intent(getActivity(), AddMovieActivity.class);
//            intent.putExtra("movie_id", movie.getId());
//            addMovieLauncher.launch(intent);
//        });

        return view;
    }

    private void loadMovies() {
        layoutPaginationContainer.setVisibility(View.GONE); // Ẩn phân trang khi bắt đầu tải
        db.collection("movies")
                .orderBy("id")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        movieList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Movies movie = document.toObject(Movies.class);
                            movieList.add(movie);
                        }
                        currentList = new ArrayList<>(movieList);
                        adapter.updateList(currentList);
                        spinnerSort.setSelection(0);
                        paginateMovies();
                    }
                });
    }
    private void filterMovies(String query) {
        String normalizedQuery = removeVietnameseDiacritics(query.toLowerCase());
        List<Movies> filteredList = new ArrayList<>();
        for (Movies movie : movieList) {
            String normalizedTitle = removeVietnameseDiacritics(movie.getTitle().toLowerCase());
            if (normalizedTitle.contains(normalizedQuery)) {
                filteredList.add(movie);
            }
        }
        currentList = filteredList;
        currentPage = 1;
        paginateMovies();
    }


    private void sortMovies(int position) {
        if (currentList == null) return;

        switch (position) {
            case 0:
                Collections.sort(currentList, Comparator.comparing(Movies::getTitle));
                break;
            case 1:
                Collections.sort(currentList, (m1, m2) -> m2.getTitle().compareTo(m1.getTitle()));
                break;
            case 2:
                Collections.sort(currentList, (m1, m2) -> m2.getReleaseDate().compareTo(m1.getReleaseDate()));
                break;
            case 3:
                Collections.sort(currentList, Comparator.comparing(Movies::getReleaseDate));
                break;
        }

        // Reset lại phân trang về trang 1
        currentPage = 1;

//        // Ẩn ô nhập trang
//        editPage.setVisibility(View.GONE);
//        btnGoPage.setVisibility(View.GONE);

        // Hiện lại khi người dùng quay về sort mặc định
        if (position == 0) {
            editPage.setVisibility(View.VISIBLE);
            btnGoPage.setVisibility(View.VISIBLE);
        }

        paginateMovies();
    }



    private void paginateMovies() {
        int totalItems = currentList.size();
        totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);

        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, totalItems);

        List<Movies> pageList = currentList.subList(start, end);
        adapter.updateList(pageList);
        updatePaginationButtons();

        // Chỉ hiển thị phân trang nếu có nhiều hơn 1 trang
//        if (totalPages > 1) {
//            // Kiểm tra xem người dùng đã cuộn xuống cuối chưa
//            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
//            if (layoutManager != null) {
//                int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
//                if (lastVisiblePosition >= pageList.size() - 1) {
//                    layoutPaginationContainer.setVisibility(View.VISIBLE);
//                }
//            }
//        } else {
            layoutPaginationContainer.setVisibility(View.VISIBLE);

    }



    private void updatePaginationButtons() {
        txtPageInfo.setText(String.format("Trang %d/%d", currentPage, totalPages));

        // Xóa các nút trang cũ (giữ lại nút Prev và Next)
        int childCount = paginationLayout.getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            View child = paginationLayout.getChildAt(i);
            if (child != btnPrev && child != btnNext) {
                paginationLayout.removeViewAt(i);
            }
        }

        // Logic hiển thị nút trang
        if (totalPages <= 5) {
            // Hiển thị tất cả nút trang nếu ít hơn 5 trang
            for (int i = 1; i <= totalPages; i++) {
                addPageButton(i);
            }
        } else {
            // Luôn hiển thị trang đầu
            addPageButton(1);

            if (currentPage > 3) {
                addDotsTextView();
            }

            // Hiển thị các trang xung quanh trang hiện tại
            int start = Math.max(2, currentPage - 1);
            int end = Math.min(totalPages - 1, currentPage + 1);
            for (int i = start; i <= end; i++) {
                if (i != 1 && i != totalPages) {
                    addPageButton(i);
                }
            }

            if (currentPage < totalPages - 2) {
                addDotsTextView();
            }

            // Luôn hiển thị trang cuối
            addPageButton(totalPages);
        }

        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);
    }

    private void addPageButton(int pageNumber) {
        Button pageButton = new Button(getContext());
        pageButton.setText(String.valueOf(pageNumber));

        // Kích thước nút
        int buttonSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 36, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                buttonSize, // width
                buttonSize // height
        );
        params.setMargins(4, 0, 4, 0);
        pageButton.setLayoutParams(params);

        // Thiết lập style
        pageButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        pageButton.setBackgroundResource(R.drawable.page_button_background);

        if (pageNumber == currentPage) {
            pageButton.setEnabled(false);
            pageButton.setBackgroundResource(R.drawable.current_page_background);
        }

        pageButton.setOnClickListener(v -> {
            currentPage = pageNumber;
            paginateMovies();
        });

        // Thêm nút vào trước nút "Sau"
        int insertPosition = paginationLayout.indexOfChild(btnNext);
        paginationLayout.addView(pageButton, insertPosition);
    }

    private void addDotsTextView() {
        TextView dots = new TextView(getContext());
        dots.setText("...");
        dots.setTextSize(14);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(4, 0, 4, 0);
        dots.setLayoutParams(params);

        // Thêm vào trước nút "Sau"
        int insertPosition = paginationLayout.indexOfChild(btnNext);
        paginationLayout.addView(dots, insertPosition);
    }







    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if ("admin".equals(role)) {
            inflater.inflate(R.menu.menu_movie_fragment, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if ("admin".equals(role)) {
            if (item.getItemId() == R.id.action_add_movie) {
                Intent intent = new Intent(getActivity(), AddMovieActivity.class);
                addMovieLauncher.launch(intent);
                return true;
            } else if (item.getItemId() == R.id.action_load_from_api) {
                Intent intent = new Intent(getActivity(), SaveNewMovieFromApiToFirestore.class);
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

}

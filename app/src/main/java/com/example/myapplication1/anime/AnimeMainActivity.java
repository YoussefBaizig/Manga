package com.example.myapplication1.anime;


import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.compose.ui.platform.ComposeView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication1.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AnimeMainActivity extends AppCompatActivity {
    private Spinner spinnerGenres;
    private RecyclerView recyclerView;
    private AnimeAdapter animeAdapter;
    private List<Anime> animeList = new ArrayList<>();
    private List<Genre> genreList = new ArrayList<>();

    private int currentPage = 1;
    private Integer currentGenreId = null;  // null pour "Tous"
    private Button loadMoreButton;

    private SearchView searchViewAnime;
    private Button previousButton;
    private String currentSearchQuery = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinnerGenres = findViewById(R.id.spinnerGenres);
        recyclerView = findViewById(R.id.recyclerViewAnime);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        animeAdapter = new AnimeAdapter(animeList, this);
        recyclerView.setAdapter(animeAdapter);

        // Dans AnimeMainActivity.java
        ComposeView composeView = findViewById(R.id.compose_view_bottom_nav);
        AnimeNavBridge.setBottomNavigation(composeView);



        searchViewAnime = findViewById(R.id.searchViewAnime);
        EditText searchEditText =
                searchViewAnime.findViewById(androidx.appcompat.R.id.search_src_text);

        searchEditText.setTextColor(Color.WHITE);
        searchEditText.setHintTextColor(Color.LTGRAY);

        ImageView closeButton =
                searchViewAnime.findViewById(androidx.appcompat.R.id.search_close_btn);
        closeButton.setColorFilter(Color.WHITE);



        // Initialiser Retrofit pour Jikan API
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.jikan.moe/v4/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JikanApi api = retrofit.create(JikanApi.class);

        loadMoreButton = findViewById(R.id.loadMoreButton);
        loadMoreButton.setOnClickListener(v -> {
            currentPage++;
            loadAnime(api, currentGenreId, currentPage, true);
        });

        previousButton = findViewById(R.id.previousButton);
        previousButton.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                loadAnime(api, currentGenreId, currentPage,true);
            }
        });

        searchViewAnime.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(api,query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() >= 3) {  // Évite appels inutiles pour 1-2 lettres
                    performSearch(api,newText);
                } else if (newText.isEmpty()) {
                    currentSearchQuery = null;
                    currentPage = 1;
                    loadAnime(api, currentGenreId, currentPage, false);
                }
                return false;
            }
        });

        api.getAnimeGenres().enqueue(new Callback<GenreResponse>() {
            @Override
            public void onResponse(Call<GenreResponse> call, Response<GenreResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    genreList.clear();
                    genreList.addAll(response.body().getData());

                    List<String> names = new ArrayList<>();
                    names.add("Tous");

                    for (Genre g : genreList) {
                        names.add(g.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AnimeMainActivity.this,
                            android.R.layout.simple_spinner_item,
                            names
                    );
                    adapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item
                    );

                    spinnerGenres.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<GenreResponse> call, Throwable t) {
                // gestion erreur si tu veux
            }
        });


        // Listener sur le Spinner pour filtrage par genre
        spinnerGenres.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentPage = 1;

                if (position == 0) {
                    // "Tous"
                    currentGenreId = null;
                    loadAnime(api, null, currentPage, false);
                } else {
                    // position - 1 car "Tous" est à l'index 0
                    Genre selectedGenre = genreList.get(position - 1);
                    currentGenreId = selectedGenre.getId();
                    loadAnime(api, currentGenreId, currentPage, false);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void performSearch(JikanApi api,String query) {
        String trimmed = query.trim();
        if (trimmed.isEmpty()) return;
        currentSearchQuery = trimmed;
        currentPage = 1;
        loadAnime(api, currentGenreId, currentPage, false);
    }

    // Méthode pour charger les animes (optionnellement filtrés)
    private void loadAnime(JikanApi api, Integer genreId,int page, boolean isLoadMore) {
        Call<AnimeResponse> call;
        if (currentSearchQuery != null && !currentSearchQuery.isEmpty()) {
            if (currentGenreId != null) {
                call = api.searchAnimeWithGenre(currentSearchQuery, currentGenreId, page);
            } else {
                call = api.searchAnime(currentSearchQuery, page);
            }
        } else if (currentGenreId != null) {
            call = api.getAnimeByGenre(currentGenreId, page);
        } else {
            call = api.getTopAnime(page);
        }

        call.enqueue(new Callback<AnimeResponse>() {
            @Override
            public void onResponse(Call<AnimeResponse> call, Response<AnimeResponse> response) {
                if (response.isSuccessful() && response.body()!=null) {
                    animeList.clear();
                    for (Anime a : response.body().getData()) {
                        a.generateSlug();
                        animeList.add(a);
                    }
                    animeAdapter.notifyDataSetChanged();

                    if (response.body().getPagination() != null && response.body().getPagination().hasNextPage()) {
                        loadMoreButton.setVisibility(View.VISIBLE);
                    } else {
                        loadMoreButton.setVisibility(View.GONE);
                    }
                }
            }
            @Override public void onFailure(Call<AnimeResponse> call, Throwable t) {}
        });
    }



}
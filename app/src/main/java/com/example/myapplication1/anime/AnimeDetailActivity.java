package com.example.myapplication1.anime;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
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

public class AnimeDetailActivity extends AppCompatActivity {
    private RecyclerView recyclerEpisodes;
    private TextView textTitle;
    private EpisodeAdapter episodeAdapter;
    private List<Episode> episodes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_detail);
        String animeSlug = getIntent().getStringExtra("ANIME_SLUG");
        textTitle = findViewById(R.id.textViewAnimeTitle);
        recyclerEpisodes = findViewById(R.id.recyclerViewEpisodes);
        ImageButton btnBack = findViewById(R.id.buttonBack);
        btnBack.setOnClickListener(v -> finish());
        recyclerEpisodes.setLayoutManager(new LinearLayoutManager(this));
        episodeAdapter = new EpisodeAdapter(episodes, this, animeSlug);

        recyclerEpisodes.setAdapter(episodeAdapter);

        // Récupérer l'animeId et titre depuis l'Intent
        int animeId = getIntent().getIntExtra("animeId", -1);
        String animeTitle = getIntent().getStringExtra("animeTitle");
        textTitle.setText(animeTitle);

        // Configurer Retrofit (même code de baseUrl et converter que MainActivity)
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.jikan.moe/v4/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JikanApi api = retrofit.create(JikanApi.class);

        // Appeler endpoint /anime/{id}/episodes
        api.getAnimeEpisodes(animeId).enqueue(new Callback<EpisodeResponse>() {
            @Override
            public void onResponse(Call<EpisodeResponse> call, Response<EpisodeResponse> response) {
                if (response.isSuccessful() && response.body()!=null) {
                    episodes.clear();
                    for (Episode e : response.body().getData()) {
                        episodes.add(e);
                    }
                    episodeAdapter.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(Call<EpisodeResponse> call, Throwable t) {}
        });
    }
}
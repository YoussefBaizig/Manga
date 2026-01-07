package com.example.myapplication1.anime;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface JikanApi {

    @GET("top/anime")
    Call<AnimeResponse> getTopAnime(@Query("page") int page);

    @GET("genres/anime")
    Call<GenreResponse> getAnimeGenres();

    @GET("anime")
    Call<AnimeResponse> getAnimeByGenre(@Query("genres") int genreId,@Query("page") int page);

    @GET("anime/{id}/episodes")
    Call<EpisodeResponse> getAnimeEpisodes(@Path("id") int animeId);


    @GET("anime")
    Call<AnimeResponse> searchAnime(@Query("q") String query, @Query("page") int page);

    @GET("anime")
    Call<AnimeResponse> searchAnimeWithGenre(@Query("q") String query, @Query("genres") int genreId, @Query("page") int page);
}
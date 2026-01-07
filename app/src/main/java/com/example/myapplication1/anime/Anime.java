package com.example.myapplication1.anime;


import com.google.gson.annotations.SerializedName;

public class Anime {

    @SerializedName("mal_id")
    private int id;

    private String title;

    @SerializedName("images")
    private ImageWrapper images;

    // Nouveau : slug pour construire l'URL propre
    private String slug;

    public void generateSlug() {
        if (title != null) {
            this.slug = title.toLowerCase()
                    .replaceAll("[^a-z0-9\\s-]", "")  // enlève accents, apostrophes, etc.
                    .replaceAll("\\s+", "-")          // espaces → tirets
                    .replaceAll("-+", "-");           // évite doubles tirets
        }
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getImageUrl() { return images.jpg.image_url; }
    public String getSlug() { return slug; }
}
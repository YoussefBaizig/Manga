package com.example.myapplication1.anime;

import com.google.gson.annotations.SerializedName;

public class Pagination {  // Nouvelle classe
    @SerializedName("has_next_page")
    private boolean hasNextPage;
    @SerializedName("last_visible_page")
    private int lastVisiblePage;
    @SerializedName("current_page")
    private int currentPage;

    public boolean hasNextPage() { return hasNextPage; }
    // Ajoute getters si besoin
}

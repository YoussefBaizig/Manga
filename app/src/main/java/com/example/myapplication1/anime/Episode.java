package com.example.myapplication1.anime;

import com.google.gson.annotations.SerializedName;

public class Episode {
    @SerializedName("mal_id")
    private int id;
    private String title;

    public String getTitle() { return title; }
}
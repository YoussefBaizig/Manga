package com.example.myapplication1.anime;

import com.google.gson.annotations.SerializedName;

public class Genre {
    @SerializedName("mal_id")
    private int id;
    private String name;

    public int getId() { return id; }
    public String getName() { return name; }
}
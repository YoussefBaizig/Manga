package com.example.myapplication1.anime;

import java.util.List;

public class AnimeResponse {
    private List<Anime> data;
    private Pagination pagination;

    public List<Anime> getData() { return data; }
    public Pagination getPagination() { return pagination; }
}


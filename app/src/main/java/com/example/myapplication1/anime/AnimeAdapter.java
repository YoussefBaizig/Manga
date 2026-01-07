package com.example.myapplication1.anime;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication1.R;

import java.util.List;

public class AnimeAdapter extends RecyclerView.Adapter<AnimeAdapter.ViewHolder> {

    private List<Anime> list;
    private Context context;

    public AnimeAdapter(List<Anime> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.anime_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Anime anime = list.get(position);
        holder.title.setText(anime.getTitle());

        Glide.with(context)
                .load(anime.getImageUrl())
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> {
            // Vérifier si lien disponible (ton code existant)
            if (anime.getSlug() == null) {  // Remplace l'ancien check streamUrl
                Toast.makeText(v.getContext(), "Épisodes non disponibles", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent i = new Intent(context, AnimeDetailActivity.class);
            i.putExtra("animeId", anime.getId());
            i.putExtra("animeTitle", anime.getTitle());
            i.putExtra("ANIME_SLUG", anime.getSlug());  // ← AJOUTE CETTE LIGNE SEULEMENT
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;

        ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageViewPoster);
            title = itemView.findViewById(R.id.textViewTitle);
        }
    }
}
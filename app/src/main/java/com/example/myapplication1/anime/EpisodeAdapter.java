package com.example.myapplication1.anime;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication1.R;

import java.util.List;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.ViewHolder> {

    private List<Episode> episodeList;
    private Context context;
    private String animeSlug;  // On passe maintenant l'anime complet (avec slug)

    public EpisodeAdapter(List<Episode> episodeList, Context context, String animeSlug) {
        this.episodeList = episodeList;
        this.context = context;
        this.animeSlug = animeSlug;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.episode_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int episodeNumber = position + 1;
        holder.title.setText("Épisode " + episodeNumber);

        holder.itemView.setOnClickListener(v -> {
            // On passe le numéro d'épisode à la tâche (plus besoin d'URL ici)
            new ScrapeVideoTask(episodeNumber).execute();
        });
    }

    @Override
    public int getItemCount() {
        return episodeList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textViewEpisodeTitle);
        }
    }

    // Tâche asynchrone modifiée : Prend le numéro d'épisode, génère 2 URLs, essaie séquentiellement
    private class ScrapeVideoTask extends AsyncTask<Void, Void, String> {
        private int episodeNumber;

        public ScrapeVideoTask(int episodeNumber) {
            this.episodeNumber = episodeNumber;
        }

        @Override
        protected String doInBackground(Void... voids) {

            String url1 = "https://v6.voiranime.com/anime/" +
                    animeSlug + "/" +
                    animeSlug + "-" + episodeNumber + "-vostfr/?host=LECTEUR FHD1";

            String iframeUrl = VideoScraper.getIframeUrl(url1);
            if (iframeUrl != null && !iframeUrl.isEmpty()) {
                return iframeUrl;
            }


            String paddedNumber = String.format("%03d", episodeNumber);
            String url2 = "https://v6.voiranime.com/anime/" +
                    animeSlug + "/" +
                    animeSlug + "-" + paddedNumber + "-vostfr/?host=LECTEUR FHD1";

            iframeUrl = VideoScraper.getIframeUrl(url2);
            if (iframeUrl != null && !iframeUrl.isEmpty()) {
                return iframeUrl;
            }

            String url3 = "https://v6.voiranime.com/anime/" +
                    animeSlug + "/" +
                    animeSlug + "-0" + episodeNumber + "-vostfr/?host=LECTEUR FHD1";

            iframeUrl = VideoScraper.getIframeUrl(url3);
            if (iframeUrl != null && !iframeUrl.isEmpty()) {
                return iframeUrl;
            }

            return null;
        }

        @Override
        protected void onPostExecute(String iframeUrl) {
            if (iframeUrl != null && !iframeUrl.isEmpty()) {
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("STREAM_URL", iframeUrl);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Vidéo non disponible pour cet épisode", Toast.LENGTH_LONG).show();
            }
        }
    }
}
package com.example.myapplication1.anime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class VideoScraper {

    public static String getIframeUrl(String pageUrl) {
        try {
            Document document = Jsoup.connect(pageUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            Element iframe = document
                    .select("div#chapter-video-frame iframe")
                    .first();

            if (iframe != null) {
                return iframe.attr("src");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

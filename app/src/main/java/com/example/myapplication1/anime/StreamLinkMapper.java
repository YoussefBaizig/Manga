package com.example.myapplication1.anime;

import java.util.HashMap;
import java.util.Map;

public class StreamLinkMapper {

    private static final Map<Integer, String> STREAM_LINKS = new HashMap<>();

    static {
        STREAM_LINKS.put(11061, "https://streamtape.com/e/prxL663adYtr4Xy");
        STREAM_LINKS.put(38524, "https://streamtape.com/e/goge8O7PpwtqDpq");
        STREAM_LINKS.put(19, "https://streamtape.com/e/r0oglQ0ABrS81r");
        STREAM_LINKS.put(41467, "https://streamtape.com/e/p4ZjkeWjbAHr2P9");
        STREAM_LINKS.put(52991, " https://streamtape.com/e/y09aoRaaj3t3Kj");
        STREAM_LINKS.put(5114, "https://streamtape.com/e/myJvjA1x8rfbwz9");
        STREAM_LINKS.put(9253, "https://streamtape.com/e/1z1A21zebMceaoA");
        STREAM_LINKS.put(28977, "https://streamtape.com/e/zJoMVoGX06tYwpm");
        STREAM_LINKS.put(918, "https://streamtape.com/e/mOM4o1kpV9FbZdg");
        STREAM_LINKS.put(16498, "https://voe.sx/e/be5f5qx0evy5");
        STREAM_LINKS.put(20, "https://streamtape.com/e/pajZWVkB3vtrd8m");
        STREAM_LINKS.put(1735, "https://streamtape.com/e/vodzMWPRx1HYxa");
        STREAM_LINKS.put(21, "https://streamtape.com/e/3popy1kWWesard");
        STREAM_LINKS.put(813, "https://streamtape.com/e/4q14x7AGjvUKPxY");
        STREAM_LINKS.put(30694, "https://streamtape.com/e/BGvLQ2jPYmfyaQ6");
        STREAM_LINKS.put(1535, "https://streamtape.com/e/OXkeAvW9eOUggK");
        STREAM_LINKS.put(820, "https://streamtape.com/e/lQ9G36Z12Wu7Jv8");
        STREAM_LINKS.put(43608, "https://streamtape.com/e/ePgZXo1JOOhYG1R");
        STREAM_LINKS.put(42938, "https://streamtape.com/e/41pjPVzJeOfKo3R");
        STREAM_LINKS.put(4181, "https://streamtape.com/e/9RX3PrRJq6takp3");








    }

    public static String getStreamLink(int malId) {
        return STREAM_LINKS.get(malId);
    }
}

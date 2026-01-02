package com.musicplayer.services;

import com.musicplayer.model.Song;
import javafx.application.Platform;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONObject;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

public class LyricsService {

    private final Map<Double, String> timedLyrics = new TreeMap<>();
    private static final String LYRICS_DIR = "src/main/resources/lyrics/";
    private final OkHttpClient client = new OkHttpClient();
    private boolean isSynced = false;

    public boolean isSynced() {
        return isSynced;
    }

    // This method now takes a callback so it's non-blocking
    public void loadLyrics(Song song, Runnable onComplete) {
        CompletableFuture.runAsync(() -> {
            isSynced = false;
            Map<Double, String> lyrics = tryLocalLrc(song);
            if (lyrics != null) {
                isSynced = true;
            } else {
                lyrics = fetchFromInternet(song);
                if (lyrics != null) {
                    isSynced = false;
                } else {
                    lyrics = createSampleLyricsMap();
                    isSynced = true;
                }
            }

            Map<Double, String> finalLyrics = lyrics;
            Platform.runLater(() -> {
                applyLyrics(finalLyrics);
                if (onComplete != null) {
                    onComplete.run();
                }
            });
        });
    }

    private Map<Double, String> tryLocalLrc(Song song) {
        String lrcPath = LYRICS_DIR + song.getFile().getName()
                .replaceAll("\\.(mp3|wav|m4a|aac)$", ".lrc");
        File file = new File(lrcPath);
        return file.exists() ? loadFromLrcFile(file) : null;
    }

    private Map<Double, String> loadFromLrcFile(File file) {
        Map<Double, String> map = new TreeMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                parseLrcLine(line, map);
            }
        } catch (Exception e) {
            return null;
        }
        return map.isEmpty() ? null : map;
    }

    private void parseLrcLine(String line, Map<Double, String> map) {
        if (line.matches("\\[\\d{2}:\\d{2}\\.\\d{2,3}].+")) {
            String time = line.substring(1, 10);
            String lyric = line.substring(10).trim();
            try {
                String[] p = time.split(":");
                double total = Double.parseDouble(p[0]) * 60 + Double.parseDouble(p[1]);
                map.put(total, lyric);
            } catch (Exception ignored) {}
        }
    }

    private Map<Double, String> fetchFromInternet(Song song) {
        try {
            String artist = URLEncoder.encode(song.getArtist(), StandardCharsets.UTF_8);
            String title = URLEncoder.encode(song.getTitle(), StandardCharsets.UTF_8);
            String url = "https://api.lyrics.ovh/v1/" + artist + "/" + title;

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) return null;
                
                ResponseBody body = response.body();
                if (body == null) return null;
                
                String responseBody = body.string();
                JSONObject json = new JSONObject(responseBody);
                
                if (json.has("lyrics")) {
                    String lyrics = json.getString("lyrics");
                    return convertToTimedMap(lyrics);
                }
            }
        } catch (Exception e) {
            // e.printStackTrace(); // Optional: log error
            return null;
        }
        return null;
    }

    private Map<Double, String> convertToTimedMap(String text) {
        Map<Double, String> map = new TreeMap<>();
        String[] lines = text.split("\n");
        double time = 0.0;
        for (String l : lines) {
            if (!l.trim().isEmpty()) {
                map.put(time, l.trim());
                time += 4.2; // Approximate timing for non-synced lyrics
            }
        }
        return map;
    }

    private Map<Double, String> createSampleLyricsMap() {
        Map<Double, String> map = new TreeMap<>();
        map.put(2.25, "Feel your eyes, they all over me");
        map.put(4.69, "Don't be shy, take control of me");
        map.put(6.97, "Get the vibe, it's gonna be lit tonight");
        map.put(10.75, "Baby girl, yuh ah carry ten ton a phatness gimme some a dat");
        map.put(215.05, "It's gonna be lit tonight, no lie");
        return map;
    }

    private void applyLyrics(Map<Double, String> map) {
        timedLyrics.clear();
        if (map != null) {
            timedLyrics.putAll(map);
        }
    }

    public Map<Double, String> getTimedLyrics() {
        return new TreeMap<>(timedLyrics);
    }
}
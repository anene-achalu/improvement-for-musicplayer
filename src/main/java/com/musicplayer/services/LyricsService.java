package com.musicplayer.services;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class LyricsService {

    private Map<Double, String> timedLyrics;
    private double[] timestamps;

    public LyricsService() {
        timedLyrics = new TreeMap<>();
    }

    public void loadLyrics(String lyricsPath) {
        timedLyrics.clear();

        File lyricsFile = new File(lyricsPath);
        if (lyricsFile.exists()) {
            loadLyricsFromFile(lyricsFile);
        } else {
            createSampleLyrics();
        }

        // Create timestamps array for quick searching
        timestamps = timedLyrics.keySet().stream()
                .mapToDouble(Double::doubleValue)
                .toArray();
    }

    private void loadLyricsFromFile(File lyricsFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(lyricsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                parseLyricLine(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading lyrics file: " + e.getMessage());
            createSampleLyrics();
        }
    }

    private void parseLyricLine(String line) {
        // Parse LRC format: [mm:ss.xx]lyric text
        if (line.matches("\\[\\d{2}:\\d{2}\\.\\d{2}\\].+")) {
            String timeStr = line.substring(1, 9);
            String lyric = line.substring(10);

            try {
                String[] parts = timeStr.split(":");
                double minutes = Double.parseDouble(parts[0]);
                double seconds = Double.parseDouble(parts[1]);
                double totalSeconds = minutes * 60 + seconds;

                timedLyrics.put(totalSeconds, lyric);
            } catch (NumberFormatException e) {
                System.out.println("Error parsing time: " + timeStr);
            }
        }
    }

private void createSampleLyrics() {
    // Hypnotic - Shenseea ft. Shaneil Muir
    timedLyrics.put(2.25, "Feel your eyes, they all over me");
    timedLyrics.put(4.69, "Don't be shy, take control of me");
    timedLyrics.put(6.97, "Get the vibe, it's gonna be lit tonight");
    timedLyrics.put(10.75, "Baby girl, yuh ah carry ten ton a phatness gimme some a dat");
    timedLyrics.put(13.95, "Mixed wid di badness, look how she hot");
    timedLyrics.put(16.40, "Shaped like goddess, but a nah jus dat");
    timedLyrics.put(18.40, "It's a good piece of mentals under di cap");
    timedLyrics.put(21.17, "Hot piece of game an' mi love how yuh trod");
    timedLyrics.put(23.31, "Watching every step a di pepper deh weh yuh got");
    timedLyrics.put(25.71, "Stayin' in my brain, memory nah detach");
    timedLyrics.put(28.05, "Mainly my aim is to give yuh this love");
    timedLyrics.put(30.16, "Hypnotic the way you move");
    timedLyrics.put(31.98, "Let me acknowledge the way you do");
    timedLyrics.put(34.53, "And I would not lie, baby, you");
    timedLyrics.put(36.68, "Beam me up like Scotty");
    timedLyrics.put(39.27, "It's so hypnotic, the way you move");
    timedLyrics.put(41.57, "That's why I wanted to get to you");
    timedLyrics.put(43.92, "And I would not lie, baby, you");
    timedLyrics.put(46.10, "Move so hypnotic");
    timedLyrics.put(47.92, "No lie");
    timedLyrics.put(49.35, "Feel your eyes, they all over me");
    timedLyrics.put(51.65, "Don't be shy, take control of me");
    timedLyrics.put(54.06, "Get the vibe, it's gonna be lit tonight");
    timedLyrics.put(56.67, "Gyal, we never miss, gyal, we never miss");
    timedLyrics.put(58.76, "Hypnotized, pour another one");
    timedLyrics.put(61.09, "It's alright, I know what you want");
    timedLyrics.put(63.42, "Get the vibe, it's gonna be lit tonight");
    timedLyrics.put(66.04, "Gyal, we never miss, gyal, we never miss");
    timedLyrics.put(68.22, "Same suh we do it");
    timedLyrics.put(70.39, "Suh wi set to it");
    timedLyrics.put(72.79, "Same suh we do it");
    timedLyrics.put(73.92, "It's gonna be lit tonight, no lie");
    timedLyrics.put(77.64, "Same suh we do it");
    timedLyrics.put(79.91, "Suh wi set to it");
    timedLyrics.put(82.22, "Same suh we do it");
    timedLyrics.put(83.29, "It's gonna be lit tonight, no lie");
    timedLyrics.put(86.90, "I'm so lit, so lit, my girl");
    timedLyrics.put(88.65, "Suh lemme see yuh roll it, roll it, my girl");
    timedLyrics.put(90.96, "Mi love it when yuh bend and fold it, now let mi bone it");
    timedLyrics.put(93.72, "And let mi own it, my girl");
    timedLyrics.put(95.86, "Give yuh all the styles dat I have mastered");
    timedLyrics.put(98.12, "Hoist you up, baby girl, that's my word");
    timedLyrics.put(100.59, "Give yuh di good lovin' that is preferred");
    timedLyrics.put(102.85, "You deserve it, so don't be scared");
    timedLyrics.put(105.20, "It's hypnotic, the way you move");
    timedLyrics.put(107.33, "Let me acknowledge the way you do");
    timedLyrics.put(109.67, "And I would not lie, baby, you");
    timedLyrics.put(112.01, "Beam me up like Scotty");
    timedLyrics.put(114.53, "It's so hypnotic, the way you move");
    timedLyrics.put(116.87, "That's why I wanted to get to you");
    timedLyrics.put(119.23, "And I would not lie, baby, you");
    timedLyrics.put(121.46, "Move so hypnotic");
    timedLyrics.put(123.01, "No lie");
    timedLyrics.put(124.67, "Feel your eyes, they all over me");
    timedLyrics.put(127.02, "Don't be shy, take control of me");
    timedLyrics.put(129.33, "Get the vibe, it's gonna be lit tonight");
    timedLyrics.put(132.07, "Gyal, we never miss, gyal, we never miss");
    timedLyrics.put(134.04, "Hypnotized, pour another one");
    timedLyrics.put(136.43, "It's alright, I know what you want");
    timedLyrics.put(138.76, "Get the vibe, it's gonna be lit tonight");
    timedLyrics.put(141.29, "Gyal, we never miss, gyal, we never miss");
    timedLyrics.put(143.45, "Same suh we do it");
    timedLyrics.put(145.76, "Suh wi set to it");
    timedLyrics.put(148.12, "Same suh we do it");
    timedLyrics.put(149.20, "It's gonna be lit tonight, no lie");
    timedLyrics.put(152.95, "Same suh we do it");
    timedLyrics.put(155.18, "Suh wi set to it");
    timedLyrics.put(157.50, "Same suh we do it");
    timedLyrics.put(158.64, "It's gonna be lit tonight, no lie");
    timedLyrics.put(162.29, "Shake dat body, lemme see you just do it");
    timedLyrics.put(164.36, "Give dem hundred percent");
    timedLyrics.put(167.07, "Move dat body, lemme see you just do it");
    timedLyrics.put(169.07, "Gyal, gwaan represent");
    timedLyrics.put(171.63, "Shake dat body, lemme see you just do it");
    timedLyrics.put(173.83, "To the fullest extent");
    timedLyrics.put(176.44, "Move dat body, lemme see you just do it");
    timedLyrics.put(178.48, "Gyal, yuh magnificent");
    timedLyrics.put(181.10, "Feel your eyes, they all over me");
    timedLyrics.put(183.46, "Don't be shy, take control of me");
    timedLyrics.put(185.83, "Get the vibe, it's gonna be lit tonight");
    timedLyrics.put(188.50, "Gyal, we never miss, gyal, we never miss");
    timedLyrics.put(190.56, "Hypnotized, pour another one");
    timedLyrics.put(192.86, "It's alright, I know what you want");
    timedLyrics.put(195.21, "Get the vibe, it's gonna be lit tonight");
    timedLyrics.put(197.87, "Gyal, we never miss, gyal, we never miss");
    timedLyrics.put(199.99, "Same suh we do it");
    timedLyrics.put(202.23, "Suh wi set to it");
    timedLyrics.put(204.58, "Same suh we do it");
    timedLyrics.put(205.69, "It's gonna be lit tonight, no lie");
    timedLyrics.put(209.36, "Same suh we do it");
    timedLyrics.put(211.65, "Suh wi set to it");
    timedLyrics.put(213.97, "Same suh we do it");
    timedLyrics.put(215.05, "It's gonna be lit tonight, no lie");
}

    public Map<Double, String> getTimedLyrics() {
        return new HashMap<>(timedLyrics);
    }

    public double[] getTimestamps() {
        return timestamps != null ? timestamps.clone() : new double[0];
    }

    public String getLyricAtTime(double currentTime) {
        if (timestamps == null || timestamps.length == 0) {
            return "";
        }

        for (int i = timestamps.length - 1; i >= 0; i--) {
            if (timestamps[i] <= currentTime) {
                return timedLyrics.get(timestamps[i]);
            }
        }
        return timedLyrics.get(timestamps[0]);
    }

    public double getTimestampForLyric(double currentTime) {
        if (timestamps == null || timestamps.length == 0) {
            return 0.0;
        }

        for (int i = timestamps.length - 1; i >= 0; i--) {
            if (timestamps[i] <= currentTime) {
                return timestamps[i];
            }
        }
        return timestamps[0];
    }
}

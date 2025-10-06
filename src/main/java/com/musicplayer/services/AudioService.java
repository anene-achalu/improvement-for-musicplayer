package com.musicplayer.services;

import com.musicplayer.model.Song;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class AudioService {

    private MediaPlayer mediaPlayer;
    private Song currentSong;
    private boolean isPlaying = false;

    public interface PlaybackListener {

        void onReady(Song song, Duration totalDuration);

        void onTimeUpdate(Duration currentTime);

        void onPlaybackEnd();

        void onError(String message);
    }

    private PlaybackListener listener;

    public void setPlaybackListener(PlaybackListener listener) {
        this.listener = listener;
    }

    public void loadSong(Song song) {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }

        try {
            Media media = new Media(song.getFile().toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            currentSong = song;

            mediaPlayer.setOnReady(() -> {
                if (listener != null) {
                    listener.onReady(song, mediaPlayer.getTotalDuration());
                }
            });

            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (listener != null) {
                    listener.onTimeUpdate(newTime);
                }
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                if (listener != null) {
                    listener.onPlaybackEnd();
                }
            });

            mediaPlayer.setOnError(() -> {
                if (listener != null) {
                    listener.onError(mediaPlayer.getError().getMessage());
                }
            });

        } catch (Exception e) {
            if (listener != null) {
                listener.onError("Error loading song: " + e.getMessage());
            }
        }
    }

    public void play() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
            isPlaying = true;
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isPlaying = false;
        }
    }

    public void seek(Duration duration) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(duration);
        }
    }

    public void setVolume(double volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume);
        }
    }

    public void setMute(boolean mute) {
        if (mediaPlayer != null) {
            mediaPlayer.setMute(mute);
        }
    }

    public Duration getCurrentTime() {
        return mediaPlayer != null ? mediaPlayer.getCurrentTime() : Duration.ZERO;
    }

    public Duration getTotalDuration() {
        return mediaPlayer != null ? mediaPlayer.getTotalDuration() : Duration.ZERO;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public void dispose() {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }
}

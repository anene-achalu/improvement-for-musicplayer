package com.musicplayer.model;

import javafx.beans.property.*;
import java.io.File;

public class Song {

    private final StringProperty title;
    private final StringProperty artist;
    private final StringProperty duration;
    private final ObjectProperty<File> file;
    private final StringProperty lyricsPath;

    public Song(File file) {
        this.file = new SimpleObjectProperty<>(file);
        this.title = new SimpleStringProperty(extractTitle(file.getName()));
        this.artist = new SimpleStringProperty("Unknown Artist");
        this.duration = new SimpleStringProperty("00:00");
        this.lyricsPath = new SimpleStringProperty(generateLyricsPath(file.getName()));
    }

    public Song(String title, String artist, String duration, File file) {
        this.title = new SimpleStringProperty(title);
        this.artist = new SimpleStringProperty(artist);
        this.duration = new SimpleStringProperty(duration);
        this.file = new SimpleObjectProperty<>(file);
        this.lyricsPath = new SimpleStringProperty(generateLyricsPath(file.getName()));
    }

    private String extractTitle(String fileName) {
        return fileName.replace(".mp3", "").replace(".wav", "").replace(".m4a", "");
    }

    private String generateLyricsPath(String fileName) {
        return "lyrics/" + fileName.replace(".mp3", ".lrc")
                .replace(".wav", ".lrc")
                .replace(".m4a", ".lrc");
    }

    // Getters for properties
    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty artistProperty() {
        return artist;
    }

    public StringProperty durationProperty() {
        return duration;
    }

    public ObjectProperty<File> fileProperty() {
        return file;
    }

    public StringProperty lyricsPathProperty() {
        return lyricsPath;
    }

    // Standard getters
    public String getTitle() {
        return title.get();
    }

    public String getArtist() {
        return artist.get();
    }

    public String getDuration() {
        return duration.get();
    }

    public File getFile() {
        return file.get();
    }

    public String getLyricsPath() {
        return lyricsPath.get();
    }

    // Setters
    public void setDuration(String duration) {
        this.duration.set(duration);
    }

    public void setArtist(String artist) {
        this.artist.set(artist);
    }
}

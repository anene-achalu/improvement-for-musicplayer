package com.musicplayer.model;

import javafx.beans.property.*;
import javafx.scene.image.Image;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Song {

    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty artist = new SimpleStringProperty("Unknown Artist");
    private final StringProperty duration = new SimpleStringProperty("00:00");
    private final ObjectProperty<File> file = new SimpleObjectProperty<>();
    private final StringProperty lyricsPath = new SimpleStringProperty();
    private final ObjectProperty<Image> albumArt = new SimpleObjectProperty<>();
    private final BooleanProperty isFavorite = new SimpleBooleanProperty(false);

    public Song(File file) {
        this.file.set(file);
        String name = file.getName();
        String cleanName = name.substring(0, name.lastIndexOf('.'));

        // Smart split: "Artist - Title.mp3" → artist + title
        if (cleanName.contains(" - ")) {
            String[] parts = cleanName.split(" - ", 2);
            this.artist.set(parts[0].trim());
            this.title.set(parts[1].trim());
        } else {
            this.title.set(cleanName);
        }

        this.lyricsPath.set("src/main/resources/lyrics/" +
                name.replaceAll("\\.(mp3|wav|m4a|aac)$", ".lrc"));

        // Beautiful placeholder with song title
        String encodedTitle = URLEncoder.encode(
                title.get().length() > 20 ? title.get().substring(0, 20) + "..." : title.get(),
                StandardCharsets.UTF_8
        );
        this.albumArt.set(new Image(
                "https://via.placeholder.com/300x300/2c2c2c/ffffff?text=" + encodedTitle
        ));
    }

    // Property getters
    public StringProperty titleProperty() { return title; }
    public StringProperty artistProperty() { return artist; }
    public StringProperty durationProperty() { return duration; }
    public ObjectProperty<File> fileProperty() { return file; }
    public StringProperty lyricsPathProperty() { return lyricsPath; }
    public ObjectProperty<Image> albumArtProperty() { return albumArt; }
    public BooleanProperty isFavoriteProperty() { return isFavorite; }

    // Regular getters
    public String getTitle() { return title.get(); }
    public String getArtist() { return artist.get(); }
    public String getDuration() { return duration.get(); }
    public File getFile() { return file.get(); }
    public String getLyricsPath() { return lyricsPath.get(); }
    public Image getAlbumArt() { return albumArt.get(); }
    public boolean isFavorite() { return isFavorite.get(); }

    // Setters
    public void setDuration(String d) { duration.set(d); }
    public void setFavorite(boolean favorite) { isFavorite.set(favorite); }

    @Override
    public String toString() {
        return getTitle() + " - " + getArtist();
    }
}
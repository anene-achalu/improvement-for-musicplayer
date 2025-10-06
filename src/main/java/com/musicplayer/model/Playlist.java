package com.musicplayer.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.File;
import java.util.List;

public class Playlist {

    private final ObservableList<Song> songs;
    private String name;

    public Playlist(String name) {
        this.name = name;
        this.songs = FXCollections.observableArrayList();
    }

    public void addSong(Song song) {
        songs.add(song);
    }

    public void addSongs(List<File> files) {
        for (File file : files) {
            songs.add(new Song(file));
        }
    }

    public void removeSong(Song song) {
        songs.remove(song);
    }

    public void clear() {
        songs.clear();
    }

    public ObservableList<Song> getSongs() {
        return songs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int size() {
        return songs.size();
    }

    public boolean isEmpty() {
        return songs.isEmpty();
    }
}

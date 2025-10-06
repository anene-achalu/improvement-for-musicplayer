package com.musicplayer.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class LyricsController implements Initializable {

    @FXML
    private VBox lyricsContainer;
    @FXML
    private ScrollPane lyricsScrollPane;

    private Map<Double, String> currentLyrics;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initial empty state
        Label instruction = new Label("Lyrics will appear here when a song is playing");
        instruction.getStyleClass().add("instruction");
        lyricsContainer.getChildren().add(instruction);
    }

    public void displayLyrics(Map<Double, String> timedLyrics) {
        currentLyrics = timedLyrics;
        lyricsContainer.getChildren().clear();

        if (timedLyrics.isEmpty()) {
            Label noLyrics = new Label("No lyrics available for this song");
            noLyrics.getStyleClass().add("no-lyrics");
            lyricsContainer.getChildren().add(noLyrics);
            return;
        }

        timedLyrics.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    Label lyricLabel = new Label(entry.getValue());
                    lyricLabel.getStyleClass().add("lyric-line");
                    lyricLabel.setWrapText(true);
                    lyricLabel.setUserData(entry.getKey()); // Store timestamp
                    lyricsContainer.getChildren().add(lyricLabel);
                });
    }

    public void highlightCurrentLyric(double currentTimestamp) {
        for (int i = 0; i < lyricsContainer.getChildren().size(); i++) {
            Label lyricLabel = (Label) lyricsContainer.getChildren().get(i);
            Double labelTimestamp = (Double) lyricLabel.getUserData();

            if (labelTimestamp != null && Math.abs(labelTimestamp - currentTimestamp) < 0.5) {
                lyricLabel.getStyleClass().add("current-lyric");

                // Auto-scroll to current lyric
                lyricLabel.requestFocus();
            } else {
                lyricLabel.getStyleClass().remove("current-lyric");
            }
        }
    }
}

package com.musicplayer.controller;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class LyricsController implements Initializable {

    @FXML private VBox lyricsContainer;
    @FXML private ScrollPane lyricsScrollPane;

    private Map<Double, String> currentLyrics;
    private Timeline scrollAnimation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showPlaceholder("Lyrics will appear here when a song is playing");
    }

    public void displayLyrics(Map<Double, String> timedLyrics) {
        this.currentLyrics = timedLyrics;
        lyricsContainer.getChildren().clear();

        if (timedLyrics == null || timedLyrics.isEmpty()) {
            showPlaceholder("No lyrics available for this song");
            return;
        }

        timedLyrics.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    Label lyricLabel = new Label(entry.getValue());
                    lyricLabel.getStyleClass().add("lyric-line");
                    lyricLabel.setWrapText(true);
                    lyricLabel.setUserData(entry.getKey());
                    lyricsContainer.getChildren().add(lyricLabel);
                });
    }

    public void highlightCurrentLyric(double currentTimestamp) {
        if (lyricsContainer.getChildren().isEmpty()) return;

        // THIS IS THE MAGIC LINE — adjust the number until it's perfect
        double adjustedTime = currentTimestamp + 0.3;  // ← try 0.0 to 1.5

        Label currentLine = null;
        double closestDiff = Double.MAX_VALUE;

        for (Object nodeObj : lyricsContainer.getChildren()) {
            if (!(nodeObj instanceof Label)) continue;
            Label label = (Label) nodeObj;

            Object userData = label.getUserData();
            if (!(userData instanceof Double)) continue;
            Double timestamp = (Double) userData;

            double diff = Math.abs(timestamp - adjustedTime);
            if (diff < closestDiff) {
                closestDiff = diff;
                currentLine = label;
            }

            // Remove highlight from all lines
            label.getStyleClass().remove("current-lyric");
        }

        if (currentLine != null && closestDiff < 1.0) {
            // Removed the line that adds "current-lyric" style class
            // currentLine.getStyleClass().add("current-lyric");

            // Auto-scroll
            double lineY = currentLine.getLayoutY();
            double containerHeight = lyricsContainer.getHeight();
            double viewportHeight = lyricsScrollPane.getViewportBounds().getHeight();

            if (containerHeight > viewportHeight) {
                double targetV = (lineY - viewportHeight / 2 + currentLine.getHeight() / 2)
                        / (containerHeight - viewportHeight);
                targetV = Math.max(0, Math.min(1, targetV));

                // Smooth scrolling
                if (scrollAnimation != null) {
                    scrollAnimation.stop();
                }
                
                // Only scroll if the target is significantly different to avoid jitter
                if (Math.abs(lyricsScrollPane.getVvalue() - targetV) > 0.001) {
                    scrollAnimation = new Timeline(
                        new KeyFrame(Duration.millis(400), 
                            new KeyValue(lyricsScrollPane.vvalueProperty(), targetV, Interpolator.EASE_OUT))
                    );
                    scrollAnimation.play();
                }
            }
        }
    }
    public void clearLyrics() {
        lyricsContainer.getChildren().clear();
        showPlaceholder("Loading lyrics...");
    }

    private void showPlaceholder(String text) {
        Label placeholder = new Label(text);
        placeholder.getStyleClass().add("instruction");
        placeholder.setWrapText(true);
        lyricsContainer.getChildren().add(placeholder);
    }
}
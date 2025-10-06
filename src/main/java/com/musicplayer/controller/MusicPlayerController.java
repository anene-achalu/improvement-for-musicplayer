package com.musicplayer.controller;

import com.musicplayer.model.Playlist;
import com.musicplayer.model.Song;
import com.musicplayer.services.AudioService;
import com.musicplayer.services.LyricsService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.Parent;

public class MusicPlayerController implements Initializable {

    @FXML
    private BorderPane mainContainer;
    @FXML
    private Label songTitleLabel;
    @FXML
    private Label currentTimeLabel;
    @FXML
    private Label totalTimeLabel;
    @FXML
    private Slider progressSlider;
    @FXML
    private Slider volumeSlider;
    @FXML
    private Button playButton;
    @FXML
    private Button pauseButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button prevButton;
    @FXML
    private Button muteButton;
    @FXML
    private ListView<Song> playlistView;
    @FXML
    private VBox lyricsContainer;

    private Playlist playlist;
    private AudioService audioService;
    private LyricsService lyricsService;
    private LyricsController lyricsController;

    private int currentSongIndex = -1;
    private boolean isMuted = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeServices();
        setupPlaylist();
        setupEventHandlers();
        setupBindings();
        loadLyricsDisplay();
        
        // Initialize button states
        updateButtonStates(false);
    }

    private void initializeServices() {
        playlist = new Playlist("My Playlist");
        audioService = new AudioService();
        lyricsService = new LyricsService();

        audioService.setPlaybackListener(new AudioService.PlaybackListener() {
            @Override
            public void onReady(Song song, Duration totalDuration) {
                updateSongInfo(song, totalDuration);
                if (lyricsController != null && lyricsService != null) {
                    lyricsService.loadLyrics(song.getLyricsPath());
                    lyricsController.displayLyrics(lyricsService.getTimedLyrics());
                }
            }

            @Override
            public void onTimeUpdate(Duration currentTime) {
                updateProgress(currentTime);
                if (lyricsController != null && lyricsService != null) {
                    lyricsController.highlightCurrentLyric(lyricsService.getTimestampForLyric(currentTime.toSeconds()));
                }
            }

            @Override
            public void onPlaybackEnd() {
                playNextSong();
            }

            @Override
            public void onError(String message) {
                showError("Playback Error", message);
            }
        });
    }

    private void setupPlaylist() {
        playlistView.setItems(playlist.getSongs());
        playlistView.setCellFactory(lv -> new ListCell<Song>() {
            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);
                setText(empty ? null : song.getTitle() + " - " + song.getArtist());
            }
        });
    }

    private void setupEventHandlers() {
        playlistView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSong, newSong) -> {
                    if (newSong != null) {
                        playSelectedSong();
                    }
                }
        );

        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (progressSlider.isValueChanging()) {
                Duration seekTime = audioService.getTotalDuration().multiply(newVal.doubleValue() / 100);
                audioService.seek(seekTime);
            }
        });

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            audioService.setVolume(newVal.doubleValue() / 100);
        });
    }

    private void setupBindings() {
        // REMOVED the problematic binding
        // pauseButton.disableProperty().bind(playButton.disabledProperty().not());
    }

    private void loadLyricsDisplay() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LyricsDisplay.fxml"));

            // Use Parent - this will work regardless of what the root element is
            Parent lyricsContent = loader.load();
            lyricsController = loader.getController();
            lyricsContainer.getChildren().clear();
            lyricsContainer.getChildren().add(lyricsContent);

        } catch (IOException e) {
            showError("UI Error", "Could not load lyrics display: " + e.getMessage());
            // Add fallback content
            Label fallbackLabel = new Label("Lyrics will appear here when playing a song");
            fallbackLabel.getStyleClass().add("instruction");
            lyricsContainer.getChildren().add(fallbackLabel);
        }
    }

    @FXML
    private void handleAddSongs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.m4a", "*.aac")
        );

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            playlist.addSongs(selectedFiles);
            if (currentSongIndex == -1) {
                currentSongIndex = 0;
                playlistView.getSelectionModel().selectFirst();
            }
        }
    }

    @FXML
    private void handlePlay() {
        if (playlist.isEmpty()) {
            return;
        }

        if (currentSongIndex == -1) {
            currentSongIndex = 0;
            playSelectedSong();
        } else {
            audioService.play();
            updateButtonStates(true); // Use the new method
        }
    }

    @FXML
    private void handlePause() {
        audioService.pause();
        updateButtonStates(false); // Use the new method
    }

    @FXML
    private void handleStop() {
        audioService.stop();
        updateButtonStates(false); // Use the new method
        progressSlider.setValue(0);
        currentTimeLabel.setText("00:00");
    }

    @FXML
    private void handleNext() {
        playNextSong();
    }

    @FXML
    private void handlePrevious() {
        playPreviousSong();
    }

    @FXML
    private void handleMute() {
        isMuted = !isMuted;
        audioService.setMute(isMuted);
        muteButton.setText(isMuted ? "🔊" : "🔇");
    }

    private void playSelectedSong() {
        currentSongIndex = playlistView.getSelectionModel().getSelectedIndex();
        Song selectedSong = playlist.getSongs().get(currentSongIndex);

        audioService.loadSong(selectedSong);
        audioService.play();

        updateButtonStates(true); // Use the new method
        songTitleLabel.setText(selectedSong.getTitle() + " - " + selectedSong.getArtist());
    }

    private void playNextSong() {
        if (playlist.isEmpty()) {
            return;
        }

        currentSongIndex = (currentSongIndex + 1) % playlist.size();
        playlistView.getSelectionModel().select(currentSongIndex);
    }

    private void playPreviousSong() {
        if (playlist.isEmpty()) {
            return;
        }

        currentSongIndex = (currentSongIndex - 1 + playlist.size()) % playlist.size();
        playlistView.getSelectionModel().select(currentSongIndex);
    }

    private void updateSongInfo(Song song, Duration totalDuration) {
        songTitleLabel.setText(song.getTitle() + " - " + song.getArtist());
        totalTimeLabel.setText(formatTime(totalDuration));
        song.setDuration(formatTime(totalDuration));
    }

    private void updateProgress(Duration currentTime) {
        if (!progressSlider.isValueChanging()) {
            double progress = (currentTime.toSeconds() / audioService.getTotalDuration().toSeconds()) * 100;
            progressSlider.setValue(progress);
            currentTimeLabel.setText(formatTime(currentTime));
        }
    }

    private String formatTime(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) duration.toSeconds() % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void updateButtonStates(boolean isPlaying) {
        playButton.setDisable(isPlaying);
        pauseButton.setDisable(!isPlaying);
        stopButton.setDisable(!isPlaying);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
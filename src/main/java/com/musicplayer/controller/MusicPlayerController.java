package com.musicplayer.controller;

import com.musicplayer.model.Playlist;
import com.musicplayer.model.Song;
import com.musicplayer.services.AudioService;
import com.musicplayer.services.LyricsService;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.scene.Parent;
import javafx.application.Platform;

public class MusicPlayerController implements Initializable {

    @FXML
    private BorderPane mainContainer;
    @FXML
    private ImageView albumArtImageView;
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
    private Label volumeLabel;
    @FXML
    private Button prevButton;
    @FXML
    private ToggleButton playPauseButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button muteButton;
    @FXML
    private ListView<Song> playlistView;
    @FXML
    private VBox lyricsContainer;
    @FXML
    private TextField searchField;
    @FXML
    private ToggleButton showFavoritesButton;
    @FXML
    private ToggleButton shuffleButton;
    @FXML
    private Button repeatButton;
    @FXML
    private CheckMenuItem themeMenuItem;

    private Playlist playlist;
    private FilteredList<Song> filteredPlaylist;
    private AudioService audioService;
    private LyricsService lyricsService;
    private LyricsController lyricsController;

    private int currentSongIndex = -1;
    private boolean isMuted = false;
    private boolean isShuffle = false;
    
    private enum RepeatMode {
        NONE, ALL, ONE
    }
    private RepeatMode repeatMode = RepeatMode.ALL;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeServices();
        setupPlaylist();
        setupEventHandlers();
        setupBindings();
        setupDragAndDrop();
        loadLyricsDisplay();
        setupKeyboardShortcuts();
        
        // Initialize button states
        updateButtonStates(false);
        repeatButton.setTooltip(new Tooltip());
        updateRepeatButtonState();
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
                    lyricsService.loadLyrics(song, () -> {
                        if (lyricsController != null) {
                            lyricsController.displayLyrics(lyricsService.getTimedLyrics());
                        }
                    });

                    Platform.runLater(() -> {
                        lyricsController.displayLyrics(lyricsService.getTimedLyrics());
                        lyricsController.highlightCurrentLyric(0); 
                    });
                }
            }

            @Override
            public void onTimeUpdate(Duration currentTime) {
                updateProgress(currentTime);
                if (lyricsController != null && lyricsService != null) {
                    lyricsController.highlightCurrentLyric(currentTime.toSeconds());
                }
            }

            @Override
            public void onPlaybackEnd() {
                if (repeatMode == RepeatMode.ONE) {
                    audioService.seek(Duration.ZERO);
                    audioService.play();
                } else {
                    playNextSong();
                }
            }

            @Override
            public void onError(String message) {
                showError("Playback Error", message);
            }
        });
    }

    private void setupPlaylist() {
        filteredPlaylist = new FilteredList<>(playlist.getSongs(), p -> true);
        playlistView.setItems(filteredPlaylist);
        playlistView.setCellFactory(lv -> new FavoriteListCell());
    }

    private class FavoriteListCell extends ListCell<Song> {
        private HBox hbox = new HBox(10);
        private Label label = new Label();
        private Button favoriteButton = new Button("❤");
        private Region spacer = new Region();

        public FavoriteListCell() {
            super();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            hbox.getChildren().addAll(label, spacer, favoriteButton);
            
            favoriteButton.setOnAction(event -> {
                Song song = getItem();
                if (song != null) {
                    song.setFavorite(!song.isFavorite());
                    updateItem(song, false); 
                    
                    if (showFavoritesButton.isSelected() && !song.isFavorite()) {
                        updatePlaylistFilter();
                    }
                }
            });
        }

        @Override
        protected void updateItem(Song song, boolean empty) {
            super.updateItem(song, empty);
            if (empty || song == null) {
                setGraphic(null);
                setText(null);
                setStyle("");
            } else {
                String text = song.getTitle() + " - " + song.getArtist();
                if (audioService.getCurrentSong() == song) {
                    label.setText("▶ " + text);
                    label.setStyle("-fx-font-weight: bold;");
                } else {
                    label.setText(text);
                    label.setStyle("");
                }
                
                if (song.isFavorite()) {
                    favoriteButton.setStyle("-fx-text-fill: red;");
                } else {
                    favoriteButton.setStyle("-fx-text-fill: grey;");
                }
                
                setGraphic(hbox);
            }
        }
    }

    private void setupEventHandlers() {
        playlistView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSong, newSong) -> {
                    if (newSong != null) {
                        playSelectedSong();
                    }
                }
        );

        progressSlider.setOnMouseClicked((MouseEvent event) -> {
            double value = (event.getX() / progressSlider.getWidth()) * progressSlider.getMax();
            progressSlider.setValue(value);
            Duration seekTime = audioService.getTotalDuration().multiply(value / 100.0);
            audioService.seek(seekTime);
        });

        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (progressSlider.isValueChanging()) {
                Duration seekTime = audioService.getTotalDuration().multiply(newVal.doubleValue() / 100);
                audioService.seek(seekTime);
            }
        });

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            audioService.setVolume(newVal.doubleValue() / 100);
            volumeLabel.setText(String.format("%.0f%%", newVal.doubleValue()));
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePlaylistFilter();
        });
    }

    private void updatePlaylistFilter() {
        String searchText = searchField.getText();
        boolean showFavoritesOnly = showFavoritesButton != null && showFavoritesButton.isSelected();

        filteredPlaylist.setPredicate(song -> {
            if (showFavoritesOnly && !song.isFavorite()) {
                return false;
            }

            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            String lowerCaseFilter = searchText.toLowerCase();
            if (song.getTitle().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (song.getArtist().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }
            return false;
        });
    }

    private void setupBindings() {
    }

    private void setupDragAndDrop() {
        if (mainContainer != null) {
            mainContainer.setOnDragOver(event -> {
                if (event.getDragboard().hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
                event.consume();
            });

            mainContainer.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    List<File> files = db.getFiles();
                    List<File> audioFiles = files.stream()
                            .filter(this::isAudioFile)
                            .collect(Collectors.toList());

                    if (!audioFiles.isEmpty()) {
                        playlist.addSongs(audioFiles);
                        if (currentSongIndex == -1) {
                            currentSongIndex = 0;
                            playlistView.getSelectionModel().selectFirst();
                        }
                        success = true;
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            });
        }
    }

    private void setupKeyboardShortcuts() {
        if (mainContainer != null) {
            mainContainer.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.SPACE) {
                    playPauseButton.fire();
                    event.consume();
                } else if (event.getCode() == KeyCode.RIGHT) {
                    handleNext();
                    event.consume();
                } else if (event.getCode() == KeyCode.LEFT) {
                    handlePrevious();
                    event.consume();
                }
            });
        }
    }

    private boolean isAudioFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".m4a") || name.endsWith(".aac");
    }

    private void loadLyricsDisplay() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LyricsDisplay.fxml"));
            Parent lyricsContent = loader.load();
            lyricsController = loader.getController();
            lyricsContainer.getChildren().clear();
            lyricsContainer.getChildren().add(lyricsContent);

        } catch (IOException e) {
            showError("UI Error", "Could not load lyrics display: " + e.getMessage());
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
    private void handleRemoveSong() {
        Song selected = playlistView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (audioService.getCurrentSong() == selected) {
                audioService.stop();
                updateButtonStates(false);
            }
            
            int indexInOriginal = playlist.getSongs().indexOf(selected);
            
            playlist.removeSong(selected);
            
            if (indexInOriginal != -1) {
                 if (indexInOriginal < currentSongIndex) {
                     currentSongIndex--;
                 } else if (indexInOriginal == currentSongIndex) {
                     if (currentSongIndex >= playlist.size()) {
                         currentSongIndex = 0;
                     }
                     if (playlist.isEmpty()) {
                         currentSongIndex = -1;
                     }
                 }
            }
        }
    }

    @FXML
    private void handleSavePlaylist() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Playlist");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Playlist Files", "*.txt"));
        File file = fileChooser.showSaveDialog(mainContainer.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                for (Song song : playlist.getSongs()) {
                    writer.println(song.getFile().getAbsolutePath());
                }
            } catch (IOException e) {
                showError("Save Error", "Could not save playlist: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleLoadPlaylist() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Playlist");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Playlist Files", "*.txt"));
        File file = fileChooser.showOpenDialog(mainContainer.getScene().getWindow());

        if (file != null) {
            try {
                List<String> lines = Files.readAllLines(file.toPath());
                List<File> files = lines.stream()
                        .map(File::new)
                        .filter(f -> f.exists() && isAudioFile(f))
                        .collect(Collectors.toList());

                if (!files.isEmpty()) {
                    playlist.clear();
                    playlist.addSongs(files);
                    if (currentSongIndex == -1 && !playlist.isEmpty()) {
                        currentSongIndex = 0;
                        playlistView.getSelectionModel().selectFirst();
                    }
                }
            } catch (IOException e) {
                showError("Load Error", "Could not load playlist: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    private void handleShowFavorites() {
        updatePlaylistFilter();
    }

    @FXML
    private void handleShuffle() {
        isShuffle = shuffleButton.isSelected();
        if (isShuffle) {
            Song currentSong = audioService.getCurrentSong();
            
            playlist.shuffle();
            
            if (currentSong != null) {
                currentSongIndex = playlist.getSongs().indexOf(currentSong);
                playlistView.getSelectionModel().select(currentSong);
            }
        }
    }

    @FXML
    private void handleRepeat() {
        if (repeatMode == RepeatMode.ALL) {
            repeatMode = RepeatMode.ONE;
        } else if (repeatMode == RepeatMode.ONE) {
            repeatMode = RepeatMode.NONE;
        } else { // NONE
            repeatMode = RepeatMode.ALL;
        }
        updateRepeatButtonState();
    }

    private void updateRepeatButtonState() {
        Tooltip tooltip = repeatButton.getTooltip();
        if (repeatMode == RepeatMode.ALL) {
            repeatButton.setText("🔁");
            repeatButton.getStyleClass().remove("repeat-one");
            repeatButton.getStyleClass().add("repeat-all");
            tooltip.setText("Repeat: All");
        } else if (repeatMode == RepeatMode.ONE) {
            repeatButton.setText("🔂");
            repeatButton.getStyleClass().remove("repeat-all");
            repeatButton.getStyleClass().add("repeat-one");
            tooltip.setText("Repeat: One");
        } else { // NONE
            repeatButton.setText("🔁");
            repeatButton.getStyleClass().removeAll("repeat-all", "repeat-one");
            tooltip.setText("Repeat: Off");
        }
    }

    @FXML
    private void handleThemeToggle() {
        if (mainContainer != null && mainContainer.getScene() != null) {
            if (themeMenuItem.isSelected()) {
                mainContainer.getScene().getStylesheets().add(getClass().getResource("/css/light-theme.css").toExternalForm());
            } else {
                mainContainer.getScene().getStylesheets().remove(getClass().getResource("/css/light-theme.css").toExternalForm());
            }
        }
    }

    @FXML
    private void handlePlayPause() {
        if (playlist.isEmpty()) {
            playPauseButton.setSelected(false);
            return;
        }

        if (playPauseButton.isSelected()) {
            if (currentSongIndex == -1) {
                currentSongIndex = 0;
                playSelectedSong();
            } else {
                audioService.play();
            }
            updateButtonStates(true);
        } else {
            audioService.pause();
            updateButtonStates(false);
        }
    }

    @FXML
    private void handleStop() {
        audioService.stop();
        updateButtonStates(false);
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
        muteButton.setText(isMuted ? "🔇" : "🔊");
    }

    private void playSelectedSong() {
        Song selected = playlistView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        currentSongIndex = playlist.getSongs().indexOf(selected);

        audioService.stop();
        audioService.loadSong(selected);
        audioService.setMute(isMuted); // Apply mute state to new song
        audioService.play();

        updateSongInfo(selected, Duration.ZERO);

        if (lyricsController != null) {
            lyricsController.clearLyrics();
        }

        lyricsService.loadLyrics(selected, () -> {
            if (lyricsController != null) {
                lyricsController.displayLyrics(lyricsService.getTimedLyrics());
            }
        });
    }
    private void playNextSong() {
        if (playlist.isEmpty()) {
            return;
        }

        int nextIndex = currentSongIndex + 1;

        if (nextIndex >= playlist.size()) {
            if (repeatMode == RepeatMode.ALL) {
                nextIndex = 0; // Loop back to the start
            } else { // RepeatMode.NONE
                audioService.stop();
                updateButtonStates(false);
                return; // Stop playback
            }
        }
        
        currentSongIndex = nextIndex;
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
        
        if (albumArtImageView != null) {
            albumArtImageView.setImage(song.getAlbumArt());
        }
        
        playlistView.refresh();
    }

    private void updateProgress(Duration currentTime) {
        if (!progressSlider.isValueChanging() && audioService.getTotalDuration().toSeconds() > 0) {
            double progress = (currentTime.toSeconds() / audioService.getTotalDuration().toSeconds()) * 100;
            progressSlider.setValue(progress);
        }
        currentTimeLabel.setText(formatTime(currentTime));
    }

    private String formatTime(Duration duration) {
        long seconds = (long) duration.toSeconds();
        long minutes = seconds / 60;
        seconds %= 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void updateButtonStates(boolean isPlaying) {
        playPauseButton.setSelected(isPlaying);
        playPauseButton.setText(isPlaying ? "⏸" : "▶");
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
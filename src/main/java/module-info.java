module com.musicplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.graphics;  // Add this
    requires javafx.base;      // Add this
    requires java.desktop;     // Add this for file operations
    requires okhttp3;
    requires org.json;

    // Open packages to JavaFX for reflection (FXML loading)
    opens com.musicplayer to javafx.fxml;
    opens com.musicplayer.controller to javafx.fxml;
    opens com.musicplayer.model to javafx.fxml;
    opens com.musicplayer.services to javafx.fxml;
    
    // Also open resources package for FXML and CSS loading
    opens css to javafx.fxml;
    opens view to javafx.fxml;

    // Export packages so other parts of the app (or external modules) can use them
    exports com.musicplayer;
    exports com.musicplayer.controller;
    exports com.musicplayer.model;
    exports com.musicplayer.services;
}
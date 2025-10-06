## MyMusicPlayer — JavaFX Music Player (student-friendly contribution repo)

This is a small JavaFX-based music player (MVC-style) meant as a teaching / contribution project. It plays local audio files using JavaFX Media, shows a playlist, and displays timed lyrics (currently either loaded from a local .lrc or a hard-coded sample). The codebase is intentionally simple and has many places where students can contribute improvements for extra credit.

Below you'll find a short description of what the project contains, how to run it, a list of meaningful contribution tasks (beginner → advanced), suggested AI-powered features, and some creative/"fun" ideas.

## What the project contains

- Entry point: `com.musicplayer.MusicApp` (JavaFX Application)
- Controllers: `com.musicplayer.controller.MusicPlayerController`, `LyricsController`
- Services: `com.musicplayer.services.AudioService`, `LyricsService`
- Models: `com.musicplayer.model.Song`, `Playlist`
- Views: `src/main/resources/view/MusicPlayer.fxml`, `LyricsDisplay.fxml`
- Styling: `src/main/resources/css/styles.css`
- Build: `pom.xml` (Maven + javafx-maven-plugin)

What works today

- Add local audio files (.mp3, .wav, .m4a, .aac) via the Add Songs button
- Play / Pause / Stop / Prev / Next controls
- Volume and mute control
- Display of timed lyrics (from a local .lrc or the sample created by `LyricsService`)

Known limitations / quick notes

- The `pom.xml`'s javafx-maven-plugin has `mainClass` set to a non-existent class (`com.musicplayer.mymusicplayer.App`). To run via Maven/`javafx:run` update that to `com.musicplayer.MusicApp` or just run `MusicApp` from your IDE.
- Lyrics are mostly hard-coded in `LyricsService#createSampleLyrics()` and local `.lrc` support is minimal (parser only accepts a narrow LRC format).
- No persistence for playlists (no save/load)
- No network features (fetching lyrics from web, streaming, or downloading) implemented yet

## Requirements

- JDK 11 (project compiled for Java 11) — newer JDKs may work but tests were performed for 11
- JavaFX SDK is required if you're not using the javafx-maven-plugin in a way that downloads modules automatically. The project uses OpenJFX dependencies in `pom.xml`.
- Maven (optional if you run inside NetBeans/IDE)

## How to run (quick)

1. Recommended: Open the project in NetBeans (it was developed with NetBeans). Run `com.musicplayer.MusicApp` from the IDE.

2. Using Maven (command line / PowerShell):
   - First, fix the `mainClass` entry in the `pom.xml` plugin configuration to point to `com.musicplayer.MusicApp` (search for `mainClass` and replace the value).

   - Then run (PowerShell):

```
mvn clean package; mvn javafx:run
```

    If the plugin still references the wrong main class you can run the application directly from the IDE by running the `MusicApp` main method.

## Quick development tips

- If JavaFX Media can't play a file, verify the audio file is not DRM protected and is supported by JavaFX Media on your platform.
- To add a lyrics file next to a song, create the folder `src/main/resources/lyrics/` and place an `.lrc` with the same name as the audio file (the code expects `lyrics/<filename>.lrc` by default).

## Contribution tasks (mapped to files, with difficulty and extra-credit points ideas)

Below are contribution ideas students can pick. Each task includes suggested files to edit and a short acceptance criteria so instructors can grade for extra credit.

Beginner (low friction, 1–2 points)

- Fix `pom.xml` mainClass and add a short note in `readme.md` (files: `pom.xml`, `readme.md`). Acceptance: project runs via `mvn javafx:run`.
- Improve UI affordances: show currently selected song in the playlist with a CSS style, or add a count of songs (files: `MusicPlayer.fxml`, `styles.css`, `MusicPlayerController.java`). Acceptance: UI highlights selected item and displays total songs.
- Add a "Remove Song" button to remove selected songs from the playlist (files: `MusicPlayer.fxml`, `MusicPlayerController.java`, `Playlist.java`). Acceptance: button removes selection and playlist updates.

Intermediate (moderate difficulty, 3–5 points)

- Persist playlists (save/load JSON/XML): implement save/load functions and a small file chooser to export/import playlists (files: `Playlist.java`, new `PlaylistPersistence` util, `MusicPlayerController.java`). Acceptance: user can save a playlist and re-load it later.
- Add metadata reading (artist/album) using a library like `jaudiotagger` so `Song` gets real metadata from audio files (files: `Song.java`, add dependency in `pom.xml`). Acceptance: songs display accurate artist/title when available.
- Improve lyrics parsing: support multiple LRC formats and timestamps with decimals and multiple timestamps per line (files: `LyricsService.java`). Acceptance: more realistic LRCs display correctly.

Advanced (higher effort, 6–10 points)

- Real-time lyrics fetch from a free lyrics API (e.g., `lyrics.ovh`, Vagalume, or Musixmatch — note Musixmatch requires API key). Create a networked `LyricsService.fetchFromApi(title, artist)` that falls back to local `.lrc`. Acceptance: when a song plays the app tries the API and displays found lyrics.
- Add automatic translation of lyrics or generated song summaries (see AI features below). Create a UI control to choose a target language and fetch/translate the lyrics.
- Add streaming support (play from a URL) and a UI to paste a stream URL. Support for HLS or plain mp3 streams is fine. Acceptance: user can paste a remote URL and the player streams it.
- Add unit tests for `LyricsService` and `Playlist` using JUnit and GitHub Actions to run on push.

Expert / Extra-credit projects (large/creative, 10+ points)

- Build an online backend + front-end to host & stream songs (requires server work). Add authentication, remote playlists, and streaming endpoints.
- Implement waveform visualization and karaoke-style highlighting using audio analysis and forced-alignment techniques.
- Implement auto-tagging/recommendation using ML: extract audio features and suggest genres or similar songs.

Notes on grading: each task should include a short PR with a clear description, the files changed, a short demo GIF or screenshots, and a short test/steps to verify.

## AI-powered feature suggestions (good for extra credit)

These are meaningful, modern enhancements that also let students learn about integrating external ML/AI services.

- Multilingual song summaries: generate a short summary (1–3 sentences) of the song by summarizing the lyrics or using audio metadata. Implementation idea: fetch lyrics, call a language model or a summarization model (or an open API such as Hugging Face inference endpoints or a self-hosted model), then show the summary and offer translations. UI: small panel under the title with "Summary / Translate" controls.

- Auto-lyrics fetch + fallback: implement `LyricsService.fetchFromApi(title, artist)` with caching. Use free APIs such as `lyrics.ovh` for a start. Cache results locally to avoid repeated network calls.

- Translate lyrics: use LibreTranslate (self-hostable) or other translation APIs to translate the fetched lyrics into the user's chosen language. Keep offline fallback in mind.

- Keyword extraction / tags from lyrics: extract key topics or sentiment from lyrics to auto-tag songs for search and recommendations.

- Voice / audio feature generation: using on-device or remote ML to generate short previews, create loops, or detect beats / tempo for visualization.

Implementation notes for AI features

- Network access: these features require internet access and API keys for some services. Prefer free/public endpoints or instruct students how to get their own keys.
- Privacy & costs: warn students about rate limits and any costs. If using a paid API, provide a mock or local demo mode.

## "Crazy" / fun ideas (be careful with legality)

- Add an integrated downloader that fetches tracks from a URL and stores them locally for offline playback. WARNING: downloading copyrighted music without permission may be illegal. If you implement this feature for the project, restrict it to user-provided files or clearly use only content that is public-domain or user-owned.
- Add support for streaming from a remote server (HTTP, Icecast, HLS). This makes the player work both offline and online.
- Add collaborative playlists synced over a simple backend (students can run a local server for testing).

## How to propose a contribution

1. Fork the repo and create a topic branch (e.g., `fix/pom-mainclass` or `feature/lyrics-api`).
2. Implement the change with tests when possible.
3. Add usage notes or screenshots to `readme.md` or include a short `docs/` example.
4. Open a Pull Request with a short description and link to a demo.

Checklist for PR reviewers / graders

- Short description of the change and expected behavior
- Files modified and why
- Steps to test locally
- Screenshots / GIFs if the change affects UI
- Tests added / manual verification steps

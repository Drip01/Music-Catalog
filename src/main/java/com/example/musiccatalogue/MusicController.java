package com.example.musiccatalogue;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.*;

/* This class controls the music catalog app's user interface, handling actions like adding songs,
   playing music, deleting songs, and showing play history. It connects the UI elements (like buttons
   and tables) to the database and media player. */
public class MusicController {

	// UI elements linked to the FXML file
	@FXML private Label fileLabel; // Shows the name of the selected music file
	private File selectedFile; // Stores the chosen music file
	private MediaPlayer mediaPlayer; // Plays the music
	private Song currentSong; // The song currently playing
	private boolean isPlaying = false; // Tracks if music is playing or paused
	private String currentlyPlayingPath = ""; // Path of the currently playing song

	@FXML private TabPane tabPane; // The main tab container for Library and History tabs
	@FXML private Tab libraryTab; // The Music Library tab
	@FXML private Tab historyTab; // The Play History tab
	@FXML private ComboBox<String> letterFilterComboBox; // Dropdown to filter songs by first letter
	@FXML private Button playPauseBtn; // Button to play or pause music

	@FXML private TextField titleField, artistField, albumField, genreField, yearField; // Input fields for song details
	@FXML private Label nowPlayingLabel; // Shows what's currently playing
	@FXML private HBox nowPlayingBar; // The bar at the bottom showing the current song

	@FXML private TableView<Song> songTable; // Table displaying all songs
	@FXML private TableColumn<Song, String> titleCol, artistCol, albumCol, genreCol; // Columns for song details
	@FXML private TableColumn<Song, Integer> yearCol; // Column for song year

	@FXML private ListView<Song> historyListView; // List showing recently played songs

	private ObservableList<Song> songList = FXCollections.observableArrayList(); // List of songs for the table
	// ObservableList<String> letters = FXCollections.observableArrayList(); // Commented out: for letter filter

	/* Called when the app starts to set up the song table and load data */
	public void initialize() {
		// Set up table columns to show song details
		titleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
		artistCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getArtist()));
		albumCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAlbum()));
		genreCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGenre()));
		yearCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getYear()).asObject());

		// Make table columns resize automatically to fit content
		songTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		// Load songs and history from the database
		loadSongs();
		loadHistory();

		// Handle clicks on table rows to play or pause songs
		songTable.setRowFactory(tv -> {
			TableRow<Song> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (!row.isEmpty()) {
					Song selected = row.getItem(); // Get the clicked song

					// If it's a new song, play it
					if (mediaPlayer == null || currentSong == null || !currentSong.equals(selected)) {
						if (mediaPlayer != null) mediaPlayer.stop(); // Stop current song
						Media media = new Media(selected.getFilepath()); // Load new song
						mediaPlayer = new MediaPlayer(media); // Create player
						mediaPlayer.play(); // Start playing
						isPlaying = true; // Update playing status
						currentSong = selected; // Update current song
						nowPlayingLabel.setText("🎵 " + selected.getTitle() + " by " + selected.getArtist()); // Show song info
						nowPlayingBar.setVisible(true); // Show the now-playing bar
						nowPlayingBar.setManaged(true);
						addToHistory(selected.getId()); // Add to play history
						loadHistory(); // Refresh history list
					} else {
						// If same song, toggle play/pause
						if (isPlaying) {
							mediaPlayer.pause();
							nowPlayingLabel.setText("⏸️ " + selected.getTitle() + " by " + selected.getArtist());
						} else {
							mediaPlayer.play();
							nowPlayingLabel.setText("🎵 " + selected.getTitle() + " by " + selected.getArtist());
						}
						isPlaying = !isPlaying; // Toggle playing status
					}
				}
			});
			return row;
		});

		historyListView.setOnMouseClicked(event -> {
		Song selected = historyListView.getSelectionModel().getSelectedItem(); // Get clicked song
			if (selected == null) return;

			// If it's a new song, play it
			if (currentSong == null || !currentSong.equals(selected)) {
				if (mediaPlayer != null) mediaPlayer.stop(); // Stop current song
				Media media = new Media(selected.getFilepath()); // Load song
				mediaPlayer = new MediaPlayer(media); // Create player
				mediaPlayer.play(); // Start playing
				currentSong = selected; // Update current song
				isPlaying = true; // Update playing status
				// addToHistory(selected.getId()); // Add to history
				// loadHistory();  Refresh history list
				historyListView.refresh();
				return;
			}

			// If same song, toggle play/pause
			if (isPlaying) {
				mediaPlayer.pause();
				isPlaying = false;
			} else {
				mediaPlayer.play();
				isPlaying = true;
			}
			historyListView.refresh(); // Update list display
		});
	}

	/* Adds a new song to the database and table when the "Add Song" button is clicked */
	@FXML
	public void addSong() {
		// Check if a music file is selected
		if (selectedFile == null) {
			fileLabel.setText("Please select a file first.");
			return;
		}
		// Check if required fields (title, artist, year) are filled
		if (titleField.getText().isEmpty() || artistField.getText().isEmpty() || yearField.getText().isEmpty()) {
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle("Missing Fields");
			alert.setHeaderText("Title, Artist, and Year are required.");
			alert.showAndWait();
			return;
		}

		// SQL command to add a song to the database
		String sql = "INSERT INTO songs (title, artist, album, genre, year, filepath) VALUES (?, ?, ?, ?, ?, ?)";

		try (Connection conn = DBConnection.connect(); // Connect to database
			 PreparedStatement pstmt = conn.prepareStatement(sql)) { // Prepare SQL
			pstmt.setString(1, titleField.getText()); // Set title
			pstmt.setString(2, artistField.getText()); // Set artist
			pstmt.setString(3, albumField.getText().isEmpty() ? "Unknown Album" : albumField.getText()); // Set album or default
			pstmt.setString(4, genreField.getText().isEmpty() ? "Unknown Genre" : genreField.getText()); // Set genre or default
			pstmt.setInt(5, Integer.parseInt(yearField.getText())); // Set year
			pstmt.setString(6, selectedFile.toURI().toString()); // Set file path
			pstmt.executeUpdate(); // Run the SQL to add the song
			loadSongs(); // Refresh the song table
			clearFields(); // Clear input fields
			fileLabel.setText("No file selected"); // Reset file label
			selectedFile = null; // Clear selected file
		} catch (SQLException e) {
			e.printStackTrace(); // Show error if database fails
		}
	}

	/* Loads all songs from the database into the table */
	private void loadSongs() {
		songList.clear(); // Clear the current song list
		String sql = "SELECT * FROM songs"; // SQL to get all songs

		try (Connection conn = DBConnection.connect(); // Connect to database
			 Statement stmt = conn.createStatement(); // Create SQL statement
			 ResultSet rs = stmt.executeQuery(sql)) { // Run query and get results

			// Loop through each song in the database
			while (rs.next()) {
				songList.add(new Song(
						rs.getInt("id"), // Song ID
						rs.getString("title"), // Song title
						rs.getString("artist"), // Artist name
						rs.getString("album"), // Album name
						rs.getString("genre"), // Genre
						rs.getInt("year"), // Year
						rs.getString("filepath") // File path
				));
			}

			// Sort songs alphabetically by title
			FXCollections.sort(songList, (s1, s2) -> s1.getTitle().compareToIgnoreCase(s2.getTitle()));
			songTable.setItems(songList); // Update table with songs
		} catch (SQLException e) {
			e.printStackTrace(); // Show error if database fails
		}
	}

	/* Clears all input fields after adding a song */
	private void clearFields() {
		titleField.clear(); // Clear title field
		artistField.clear(); // Clear artist field
		albumField.clear(); // Clear album field
		genreField.clear(); // Clear genre field
		yearField.clear(); // Clear year field
	}

	/* Deletes a selected song from the database and table */
	@FXML
	public void deleteSong() {
		Song selectedSong = songTable.getSelectionModel().getSelectedItem(); // Get selected song

		// Check if a song is selected
		if (selectedSong == null) {
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle("No Selection");
			alert.setHeaderText("No Song Selected");
			alert.setContentText("Please select a song in the table to delete.");
			alert.showAndWait();
			return;
		}

		try (Connection conn = DBConnection.connect()) { // Connect to database
			// Step 1: Delete related play history entries
			String deleteHistorySQL = "DELETE FROM play_history WHERE song_id = ?";
			try (PreparedStatement historyStmt = conn.prepareStatement(deleteHistorySQL)) {
				historyStmt.setInt(1, selectedSong.getId()); // Set song ID
				historyStmt.executeUpdate(); // Delete history
			}

			// Step 2: Delete the song from the database
			String deleteSongSQL = "DELETE FROM songs WHERE id = ?";
			try (PreparedStatement songStmt = conn.prepareStatement(deleteSongSQL)) {
				songStmt.setInt(1, selectedSong.getId()); // Set song ID
				songStmt.executeUpdate(); // Delete song
			}

			// Step 3: If the deleted song is playing, stop it
			if (currentSong != null && mediaPlayer != null && currentSong.equals(selectedSong)) {
				mediaPlayer.stop(); // Stop playback
				nowPlayingLabel.setText("Nothing is playing"); // Update label
				nowPlayingBar.setVisible(false); // Hide now-playing bar
				nowPlayingBar.setManaged(false);
			}

			songList.remove(selectedSong); // Remove song from table
			songTable.refresh(); // Update table display
			clearFields(); // Clear input fields
		} catch (SQLException e) {
			e.printStackTrace(); // Show error if database fails
		}
	}

	/* Opens a file chooser to select a music file */
	@FXML
	public void chooseFile() {
		FileChooser chooser = new FileChooser(); // Create file chooser
		chooser.setTitle("Choose a Song"); // Set window title
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav")); // Limit to MP3/WAV
		File file = chooser.showOpenDialog(null); // Show file chooser

		// Update label with file name or "No file selected"
		if (file != null) {
			selectedFile = file;
			fileLabel.setText(file.getName());
		} else {
			fileLabel.setText("No file selected");
		}
	}

	/* Plays or pauses a selected song */
	@FXML
	public void togglePlayPause() {
		Song selected = songTable.getSelectionModel().getSelectedItem(); // Get selected song
		if (selected == null) return; // Do nothing if no song is selected

		// If it's a new song, play it
		if (currentSong == null || !currentSong.equals(selected)) {
			if (mediaPlayer != null) mediaPlayer.stop(); // Stop current song
			Media media = new Media(selected.getFilepath()); // Load new song
			mediaPlayer = new MediaPlayer(media); // Create player
			mediaPlayer.play(); // Start playing
			isPlaying = true; // Update playing status
			currentSong = selected; // Update current song
			nowPlayingLabel.setText("🎵 " + selected.getTitle() + " by " + selected.getArtist()); // Show song info
			nowPlayingBar.setVisible(true); // Show now-playing bar
			nowPlayingBar.setManaged(true);
			addToHistory(selected.getId()); // Add to history
			loadHistory(); // Refresh history list
		} else {
			// If same song, toggle play/pause
			if (isPlaying) {
				mediaPlayer.pause();
				nowPlayingLabel.setText("⏸️ " + selected.getTitle() + " by " + selected.getArtist());
			} else {
				mediaPlayer.play();
				nowPlayingLabel.setText("🎵 " + selected.getTitle() + " by " + selected.getArtist());
			}
			isPlaying = !isPlaying; // Toggle playing status
		}
	}

	/* Adds a song to the play history in the database */
	private void addToHistory(int songId) {
		String sql = "INSERT INTO play_history (song_id) VALUES (?)"; // SQL to add history entry

		try (Connection conn = DBConnection.connect(); // Connect to database
			 PreparedStatement stmt = conn.prepareStatement(sql)) { // Prepare SQL
			stmt.setInt(1, songId); // Set song ID
			stmt.executeUpdate(); // Add to history
		} catch (SQLException e) {
			e.printStackTrace(); // Show error if database fails
		}
	}

	/* Loads the play history into the history list */
	private void loadHistory() {
		// SQL to get song details from play history, sorted by most recent
		String sql = """
        SELECT s.id, s.title, s.artist, s.album, s.genre, s.year, s.filepath, h.played_at
        FROM play_history h
        JOIN songs s ON h.song_id = s.id
        ORDER BY h.played_at DESC
        """;

		ObservableList<Song> history = FXCollections.observableArrayList(); // List for history

		try (Connection conn = DBConnection.connect(); // Connect to database
			 Statement stmt = conn.createStatement(); // Create SQL statement
			 ResultSet rs = stmt.executeQuery(sql)) { // Run query

			// Loop through history entries
			while (rs.next()) {
				Song song = new Song(
						rs.getInt("id"), // Song ID
						rs.getString("title"), // Song title
						rs.getString("artist"), // Artist name
						rs.getString("album"), // Album name
						rs.getString("genre"), // Genre
						rs.getInt("year"), // Year
						rs.getString("filepath") // File path
				);
				history.add(song); // Add to history list
			}
			historyListView.setItems(history); // Update history list display
		} catch (SQLException e) {
			e.printStackTrace(); // Show error if database fails
		}
	}

//	/* Plays a song from the history list */
//	private void playSongFromHistory(Song song) {
//		if (mediaPlayer != null) mediaPlayer.stop(); // Stop current song
//		Media media = new Media(song.getFilepath()); // Load song
//		mediaPlayer = new MediaPlayer(media); // Create player
//		mediaPlayer.play(); // Start playing
//		currentSong = song; // Update current song
//		isPlaying = true; // Update playing status
//		addToHistory(song.getId()); // Add to history
//		loadHistory(); // Refresh history list
//	}

	/* Clears all play history */
	@FXML
	public void clearHistory() {
		String sql = "DELETE FROM play_history"; // SQL to clear history

		try (Connection conn = DBConnection.connect(); // Connect to database
			 Statement stmt = conn.createStatement()) { // Create SQL statement
			stmt.executeUpdate(sql); // Clear history
			historyListView.getItems().clear(); // Clear history list display
		} catch (SQLException e) {
			e.printStackTrace(); // Show error if database fails
		}
	}

	/* Switches to the History tab */
	@FXML
	public void goToHistory() {
		tabPane.getSelectionModel().select(historyTab); // Show History tab
	}

	/* Switches to the Library tab */
	@FXML
	public void goToLibrary() {
		tabPane.getSelectionModel().select(libraryTab); // Show Library tab
	}

	/* Filters songs by the first letter of the title */
	@FXML
	public void filterByLetter() {
		String selectedLetter = letterFilterComboBox.getValue(); // Get selected letter

		// If "All" or nothing selected, show all songs
		if (selectedLetter == null || selectedLetter.equals("All")) {
			songTable.setItems(songList);
			return;
		}

		// Create a filtered list of songs starting with the selected letter
		ObservableList<Song> filtered = FXCollections.observableArrayList();
		for (Song song : songList) {
			if (song.getTitle().toUpperCase().startsWith(selectedLetter)) {
				filtered.add(song);
			}
		}
		songTable.setItems(filtered); // Update table with filtered songs
	}

	/* Handles the play/pause button in the now-playing bar */
	@FXML
	private void handlePlayPauseButton() {
		// If no song is selected and table isn't empty, select the first song
		if (songTable.getSelectionModel().getSelectedItem() == null && !songTable.getItems().isEmpty()) {
			songTable.getSelectionModel().selectFirst();
		}

		Song selectedSong = songTable.getSelectionModel().getSelectedItem(); // Get selected song

		// If no song is available, show message
		if (selectedSong == null) {
			System.out.println("No songs available to play.");
			return;
		}

		// If it's a new song, play it
		if (mediaPlayer == null || !currentlyPlayingPath.equals(selectedSong.getFilePath())) {
			if (mediaPlayer != null) {
				mediaPlayer.stop(); // Stop current song
			}

			File songFile = new File(selectedSong.getFilePath()); // Get song file
			if (!songFile.exists()) {
				System.out.println("File does not exist: " + songFile.getAbsolutePath());
				return;
			}

			Media media = new Media(songFile.toURI().toString()); // Load song
			mediaPlayer = new MediaPlayer(media); // Create player
			currentlyPlayingPath = selectedSong.getFilePath(); // Update current path

			// When song ends, reset play button
			mediaPlayer.setOnEndOfMedia(() -> {
				playPauseBtn.setText("▶");
			});

			mediaPlayer.play(); // Start playing
			playPauseBtn.setText("⏸"); // Show pause icon
			nowPlayingLabel.setText("Now Playing: " + selectedSong.getTitle()); // Update label
		} else {
			// Toggle play/pause for current song
			MediaPlayer.Status status = mediaPlayer.getStatus();
			if (status == MediaPlayer.Status.PLAYING) {
				mediaPlayer.pause();
				playPauseBtn.setText("▶"); // Show play icon
			} else {
				mediaPlayer.play();
				playPauseBtn.setText("⏸"); // Show pause icon
			}
		}
	}
}
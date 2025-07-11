package com.example.musiccatalogue;

public class Song {
	private final String filepath;
	private int id;
	private String title, artist, album, genre;
	private int year;

	public Song(int id, String title, String artist, String album, String genre, int year, String filepath) {
		this.id = id;
		this.title = title;
		this.artist = artist;
		this.album = album;
		this.genre = genre;
		this.year = year;
		this.filepath = filepath;
	}

	// Getters
	public int getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getArtist() {
		return artist;
	}

	public String getAlbum() {
		return album;
	}

	public String getGenre() {
		return genre;
	}

	public int getYear() {
		return year;
	}

	public String getFilepath() {
		return filepath;
	}

	// To show song info in history list
	@Override
	public String toString() {
		return title + " - " + artist;
	}

	public String getFilePath() {
		return "";
	}
}
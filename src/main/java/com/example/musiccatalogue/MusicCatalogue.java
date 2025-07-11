package com.example.musiccatalogue;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MusicCatalogue extends Application {
	@Override
	public void start(Stage stage) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(MusicCatalogue.class.getResource("music_catalog.fxml"));
		Scene scene = new Scene(fxmlLoader.load());
		stage.setTitle("Music Catalogue");
		stage.setResizable(true);
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch();
	}
}
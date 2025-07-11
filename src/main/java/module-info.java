module com.example.musiccatalogue {
	requires javafx.controls;
	requires javafx.fxml;
	requires java.sql;
	requires mysql.connector.j;
	requires java.naming;
	requires javafx.media;


	opens com.example.musiccatalogue to javafx.fxml;
	exports com.example.musiccatalogue;
}
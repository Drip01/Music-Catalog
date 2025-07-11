package com.example.musiccatalogue;


// DBConnection.java
import com.mysql.cj.jdbc.MysqlDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
	private static final String URL = "jdbc:mysql://localhost:3306/musiccatalog";
	private static final String USER = "root";  // Change if needed
	private static final String PASSWORD = "Ifeanyi2003";  // Change if needed

	public static Connection connect() throws SQLException {
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}

//	public static void main(String[] args) {
//		Properties props = new Properties();
//		try {
//			props.load(Files.newInputStream(Path.of("music.properties"),
//					StandardOpenOption.READ));
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//
//		var dataSource = new MysqlDataSource();
//		dataSource.setServerName(props.getProperty("serverName"));
//		dataSource.setPort(Integer.parseInt(props.getProperty("port")));
//		dataSource.setDatabaseName(props.getProperty("databaseName"));
//
//		try (var connection = dataSource.getConnection(
//				props.getProperty("user"),
//				System.getenv("MYSQL_PASS"))
//		) {
//			System.out.println("Success");
//		} catch (SQLException e) {
//			throw new RuntimeException(e);
//		}
//	}
}

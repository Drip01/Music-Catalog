package com.example.musiccatalogue;


// DBConnection.java
import com.mysql.cj.jdbc.MysqlDataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
	private static final String URL = "jdbc:mysql://localhost:3306/musiccatalog";
	private static final String USER = "root";
	private static final String PASSWORD = "Ifeanyi2003";

	public static Connection connect() throws SQLException {
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}
}

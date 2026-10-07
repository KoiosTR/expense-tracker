package com.expense.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {
    private static final String CONFIG_FILE = "db.properties";
    private static Properties properties = new Properties();

    static {
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            properties.load(fis);
        } catch (IOException e) {
            System.err.println("Could not load database configuration: " + e.getMessage());
            properties.setProperty("db.url", "jdbc:postgresql://localhost:5432/expense_tracker");
            properties.setProperty("db.user", "postgres");
            properties.setProperty("db.password", "postgres");
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Sürücüsü bulunamadı!");
        }
        
        return DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.user"),
                properties.getProperty("db.password")
        );
    }
}

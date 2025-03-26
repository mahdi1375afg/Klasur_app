package com.klasurapp.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton-Klasse zur Verwaltung der Datenbankverbindung.
 * Stellt sicher, dass in der Anwendung nur eine Datenbankverbindung verwendet wird.
 */
public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    
    // Verbindungsparameter - idealerweise aus Konfigurationsdatei oder Umgebungsvariablen laden
    private static final String URL = "jdbc:postgresql://localhost:5432/klasur_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1375"; // In Produktion aus sicherer Quelle laden
    
    private static DatabaseConnection instance;
    private Connection connection;
    
    /**
     * Privater Konstruktor für Singleton-Pattern
     */
    private DatabaseConnection() {
        try {
            this.connection = createConnection();
            logger.info("Database connection established successfully");
        } catch (SQLException e) {
            logger.error("Failed to establish database connection", e);
            throw new RuntimeException("Failed to connect to database: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gibt die einzige Instanz der DatabaseConnection zurück oder erstellt sie.
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    /**
     * Erzeugt eine neue Verbindung zur Datenbank
     */
    private Connection createConnection() throws SQLException {
        logger.debug("Creating new database connection to {}", URL);
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    /**
     * Gibt die bestehende Verbindung zurück oder erstellt eine neue, falls nötig.
     */
    public Connection getConnection() {
        try {
            // Prüfen, ob die Verbindung noch aktiv ist
            if (connection == null || connection.isClosed()) {
                logger.info("Connection is closed, creating a new one");
                connection = createConnection();
            }
        } catch (SQLException e) {
            logger.error("Error checking connection state", e);
            try {
                connection = createConnection();
            } catch (SQLException ex) {
                logger.error("Failed to recreate database connection", ex);
                throw new RuntimeException("Failed to get database connection: " + ex.getMessage(), ex);
            }
        }
        return connection;
    }
    
    /**
     * Schließt die Datenbankverbindung.
     * Sollte beim Beenden der Anwendung aufgerufen werden.
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Database connection closed successfully");
            } catch (SQLException e) {
                logger.error("Error closing database connection", e);
            }
        }
    }
    
    /**
     * Statische Hilfsmethode zum Erhalt einer Verbindung (für Kompatibilität)
     */
    public static Connection getSharedConnection() {
        return getInstance().getConnection();
    }
}
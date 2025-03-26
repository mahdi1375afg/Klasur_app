package com.klasurapp.dao;

import com.klasurapp.model.Module;
// Korrigierter Import
import com.klasurapp.dao.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleDAO {
    private static final Logger logger = LoggerFactory.getLogger(ModuleDAO.class);

    public Module save(Module module) throws SQLException {
        String sql;
        if (module.getId() == null) {
            // Insert
            sql = "INSERT INTO modules (name, description) VALUES (?, ?) RETURNING id";
            // Geändert zu getSharedConnection
            try (Connection conn = DatabaseConnection.getSharedConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, module.getName());
                stmt.setString(2, module.getDescription());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        module.setId(rs.getInt("id"));
                        logger.info("Created new module with ID: {}", module.getId());
                    }
                }
            }
        } else {
            // Update
            sql = "UPDATE modules SET name = ?, description = ? WHERE id = ?";
            // Geändert zu getSharedConnection
            try (Connection conn = DatabaseConnection.getSharedConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, module.getName());
                stmt.setString(2, module.getDescription());
                stmt.setInt(3, module.getId());
                
                int updated = stmt.executeUpdate();
                logger.info("Updated module, affected rows: {}", updated);
            }
        }
        return module;
    }

    public Optional<Module> findById(int id) throws SQLException {
        String sql = "SELECT id, name, description FROM modules WHERE id = ?";
        // Geändert zu getSharedConnection
        try (Connection conn = DatabaseConnection.getSharedConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Module module = new Module(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description")
                    );
                    return Optional.of(module);
                }
            }
        }
        return Optional.empty();
    }

    public List<Module> findAll() throws SQLException {
        List<Module> modules = new ArrayList<>();
        String sql = "SELECT id, name, description FROM modules ORDER BY name";
        
        // Geändert zu getSharedConnection
        try (Connection conn = DatabaseConnection.getSharedConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Module module = new Module(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description")
                );
                modules.add(module);
            }
        }
        
        return modules;
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM modules WHERE id = ?";
        // Geändert zu getSharedConnection
        try (Connection conn = DatabaseConnection.getSharedConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int deleted = stmt.executeUpdate();
            logger.info("Deleted module, affected rows: {}", deleted);
            return deleted > 0;
        }
    }
}
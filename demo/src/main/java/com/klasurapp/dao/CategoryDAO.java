package com.klasurapp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    public List<String> findAllCategories() throws SQLException {
        String query = "SELECT name FROM categories";
        List<String> categories = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getSharedConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                categories.add(resultSet.getString("name"));
            }
        }
        return categories;
    }
}

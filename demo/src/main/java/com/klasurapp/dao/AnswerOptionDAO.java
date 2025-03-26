package com.klasurapp.dao;

import com.klasurapp.model.AnswerOption;
import com.klasurapp.model.Question;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnswerOptionDAO {
    private static final Logger logger = LoggerFactory.getLogger(AnswerOptionDAO.class);

    public AnswerOption save(AnswerOption option) throws SQLException {
        try (Connection conn = DatabaseConnection.getSharedConnection()) {
            return save(option, conn);
        }
    }

    public AnswerOption save(AnswerOption option, Connection conn) throws SQLException {
        if (option.getId() == null) {
            // Insert
            String sql = "INSERT INTO answer_options (question_id, option_text, is_correct, option_order) " +
                       "VALUES (?, ?, ?, ?) RETURNING id";
                       
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, option.getQuestion().getId());
                stmt.setString(2, option.getOptionText());
                stmt.setBoolean(3, option.isCorrect());
                
                if (option.getOptionOrder() != null) {
                    stmt.setInt(4, option.getOptionOrder());
                } else {
                    stmt.setNull(4, Types.INTEGER);
                }
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        option.setId(rs.getInt("id"));
                        logger.info("Created new answer option with ID: {}", option.getId());
                    }
                }
            }
        } else {
            // Update
            String sql = "UPDATE answer_options SET question_id = ?, option_text = ?, " +
                       "is_correct = ?, option_order = ? WHERE id = ?";
                       
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, option.getQuestion().getId());
                stmt.setString(2, option.getOptionText());
                stmt.setBoolean(3, option.isCorrect());
                
                if (option.getOptionOrder() != null) {
                    stmt.setInt(4, option.getOptionOrder());
                } else {
                    stmt.setNull(4, Types.INTEGER);
                }
                
                stmt.setInt(5, option.getId());
                
                int updated = stmt.executeUpdate();
                logger.info("Updated answer option, affected rows: {}", updated);
            }
        }
        return option;
    }

    public List<AnswerOption> findByQuestionId(int questionId) throws SQLException {
        List<AnswerOption> options = new ArrayList<>();
        String sql = "SELECT id, question_id, option_text, is_correct, option_order " +
                   "FROM answer_options WHERE question_id = ? " +
                   "ORDER BY option_order NULLS LAST, id";
                   
        try (Connection conn = DatabaseConnection.getSharedConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, questionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                Question question = new Question();
                question.setId(questionId);
                
                while (rs.next()) {
                    AnswerOption option = new AnswerOption(
                        rs.getInt("id"),
                        question,
                        rs.getString("option_text"),
                        rs.getBoolean("is_correct"),
                        rs.getObject("option_order", Integer.class)
                    );
                    options.add(option);
                }
            }
        }
        
        return options;
    }

    public boolean deleteByQuestionId(int questionId) throws SQLException {
        try (Connection conn = DatabaseConnection.getSharedConnection()) {
            return deleteByQuestionId(questionId, conn);
        }
    }

    public boolean deleteByQuestionId(int questionId, Connection conn) throws SQLException {
        String sql = "DELETE FROM answer_options WHERE question_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, questionId);
            
            int deleted = stmt.executeUpdate();
            logger.info("Deleted answer options for question {}, affected rows: {}", questionId, deleted);
            return deleted > 0;
        }
    }
}
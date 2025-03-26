package com.klasurapp.dao;

import com.klasurapp.model.Module;
import com.klasurapp.model.Question;
import com.klasurapp.model.AnswerOption;
import com.klasurapp.model.enums.BloomLevel;
import com.klasurapp.model.enums.ClosedQuestionType;
import com.klasurapp.model.enums.QuestionFormat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestionDAO {
    private static final Logger logger = LoggerFactory.getLogger(QuestionDAO.class);
    private ModuleDAO moduleDAO = new ModuleDAO();
    private AnswerOptionDAO answerOptionDAO = new AnswerOptionDAO();

    
    public Question save(Question question) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Hier getSharedConnection statt getConnection verwenden
            conn = DatabaseConnection.getSharedConnection();
            conn.setAutoCommit(false);

            if (question.getId() == null) {
                // Insert
                String sql = "INSERT INTO questions (name, question_text, estimated_time_minutes, module_id, " +
                           "bloom_level, question_format, closed_question_type, solution) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
                
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, question.getName());
                stmt.setString(2, question.getQuestionText());
                stmt.setInt(3, question.getEstimatedTimeMinutes());
                stmt.setInt(4, question.getModule().getId());
                stmt.setInt(5, question.getBloomLevel().getLevel());
                stmt.setString(6, question.getQuestionFormat().name());
                
                if (question.getQuestionFormat() == QuestionFormat.CLOSED && question.getClosedQuestionType() != null) {
                    stmt.setString(7, question.getClosedQuestionType().name());
                } else {
                    stmt.setNull(7, Types.VARCHAR);
                }
                
                stmt.setString(8, question.getSolution());
                
                rs = stmt.executeQuery();
                if (rs.next()) {
                    question.setId(rs.getInt("id"));
                    logger.info("Created new question with ID: {}", question.getId());
                }

                // Save answer options if present - Korrigiert um Lambda-Problem zu beheben
                if (question.getQuestionFormat() == QuestionFormat.CLOSED && !question.getAnswerOptions().isEmpty()) {
                    for (AnswerOption option : question.getAnswerOptions()) {
                        option.setQuestion(question);
                        try {
                            answerOptionDAO.save(option, conn);
                        } catch (SQLException e) {
                            logger.error("Error saving answer option", e);
                        }
                    }
                }
            } else {
                // Update
                String sql = "UPDATE questions SET name = ?, question_text = ?, estimated_time_minutes = ?, " +
                           "module_id = ?, bloom_level = ?, question_format = ?, " +
                           "closed_question_type = ?, solution = ? WHERE id = ?";
                
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, question.getName());
                stmt.setString(2, question.getQuestionText());
                stmt.setInt(3, question.getEstimatedTimeMinutes());
                stmt.setInt(4, question.getModule().getId());
                stmt.setInt(5, question.getBloomLevel().getLevel());
                stmt.setString(6, question.getQuestionFormat().name());
                
                if (question.getQuestionFormat() == QuestionFormat.CLOSED && question.getClosedQuestionType() != null) {
                    stmt.setString(7, question.getClosedQuestionType().name());
                } else {
                    stmt.setNull(7, Types.VARCHAR);
                }
                
                stmt.setString(8, question.getSolution());
                stmt.setInt(9, question.getId());
                
                int updated = stmt.executeUpdate();
                logger.info("Updated question, affected rows: {}", updated);

                // Update answer options
                if (question.getQuestionFormat() == QuestionFormat.CLOSED) {
                    // Delete existing options and insert new ones
                    answerOptionDAO.deleteByQuestionId(question.getId(), conn);
                    
                    for (AnswerOption option : question.getAnswerOptions()) {
                        option.setQuestion(question);
                        answerOptionDAO.save(option, conn);
                    }
                }
            }
            
            conn.commit();
            return question;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error during rollback", ex);
                }
            }
            throw e;
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { /* ignored */ }
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { /* ignored */ }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    // Nicht schließen, da es eine Shared Connection ist
                } catch (SQLException e) { /* ignored */ }
            }
        }
    }

    public Optional<Question> findById(int id) throws SQLException {
        String sql = "SELECT q.id, q.name, q.question_text, q.estimated_time_minutes, " +
                   "q.module_id, q.bloom_level, q.question_format, q.closed_question_type, q.solution, " +
                   "m.name as module_name, m.description as module_description " +
                   "FROM questions q " +
                   "JOIN modules m ON q.module_id = m.id " +
                   "WHERE q.id = ?";
                   
        try (Connection conn = DatabaseConnection.getSharedConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Create the module object
                    Module module = new Module(
                        rs.getInt("module_id"),
                        rs.getString("module_name"),
                        rs.getString("module_description")
                    );
                    
                    // Create the question object
                    Question question = new Question();
                    question.setId(rs.getInt("id"));
                    question.setName(rs.getString("name"));
                    question.setQuestionText(rs.getString("question_text"));
                    question.setEstimatedTimeMinutes(rs.getInt("estimated_time_minutes"));
                    question.setModule(module);
                    question.setBloomLevel(BloomLevel.fromLevel(rs.getInt("bloom_level")));
                    question.setQuestionFormat(QuestionFormat.valueOf(rs.getString("question_format")));
                    
                    String closedTypeStr = rs.getString("closed_question_type");
                    if (closedTypeStr != null) {
                        question.setClosedQuestionType(ClosedQuestionType.valueOf(closedTypeStr));
                    }
                    
                    question.setSolution(rs.getString("solution"));
                    
                    // Load answer options for closed questions
                    if (question.getQuestionFormat() == QuestionFormat.CLOSED) {
                        question.setAnswerOptions(answerOptionDAO.findByQuestionId(question.getId()));
                    }
                    
                    return Optional.of(question);
                }
            }
        }
        return Optional.empty();
    }

    public List<Question> findByModule(int moduleId) throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT q.id, q.name, q.question_text, q.estimated_time_minutes, " +
                   "q.module_id, q.bloom_level, q.question_format, q.closed_question_type, q.solution, " +
                   "m.name as module_name, m.description as module_description " +
                   "FROM questions q " +
                   "JOIN modules m ON q.module_id = m.id " +
                   "WHERE q.module_id = ? " +
                   "ORDER BY q.name";
                   
        try (Connection conn = DatabaseConnection.getSharedConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, moduleId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                Module module = null;
                
                while (rs.next()) {
                    // Create the module object once
                    if (module == null) {
                        module = new Module(
                            rs.getInt("module_id"),
                            rs.getString("module_name"),
                            rs.getString("module_description")
                        );
                    }
                    
                    // Create question
                    Question question = new Question();
                    question.setId(rs.getInt("id"));
                    question.setName(rs.getString("name"));
                    question.setQuestionText(rs.getString("question_text"));
                    question.setEstimatedTimeMinutes(rs.getInt("estimated_time_minutes"));
                    question.setModule(module);
                    question.setBloomLevel(BloomLevel.fromLevel(rs.getInt("bloom_level")));
                    question.setQuestionFormat(QuestionFormat.valueOf(rs.getString("question_format")));
                    
                    String closedTypeStr = rs.getString("closed_question_type");
                    if (closedTypeStr != null) {
                        question.setClosedQuestionType(ClosedQuestionType.valueOf(closedTypeStr));
                    }
                    
                    question.setSolution(rs.getString("solution"));
                    
                    // Add to list
                    questions.add(question);
                }
            }
        }
        
        // Load answer options for all closed questions
        for (Question q : questions) {
            if (q.getQuestionFormat() == QuestionFormat.CLOSED) {
                q.setAnswerOptions(answerOptionDAO.findByQuestionId(q.getId()));
            }
        }
        
        return questions;
    }

    public List<Question> findAll() throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT q.id, q.name, q.question_text, q.estimated_time_minutes, " +
                   "q.module_id, q.bloom_level, q.question_format, q.closed_question_type, q.solution, " +
                   "m.name as module_name, m.description as module_description " +
                   "FROM questions q " +
                   "JOIN modules m ON q.module_id = m.id " +
                   "ORDER BY q.name";
                   
        try (Connection conn = DatabaseConnection.getSharedConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                // Create the module object
                Module module = new Module(
                    rs.getInt("module_id"),
                    rs.getString("module_name"),
                    rs.getString("module_description")
                );
                
                // Create the question object
                Question question = new Question();
                question.setId(rs.getInt("id"));
                question.setName(rs.getString("name"));
                question.setQuestionText(rs.getString("question_text"));
                question.setEstimatedTimeMinutes(rs.getInt("estimated_time_minutes"));
                question.setModule(module);
                question.setBloomLevel(BloomLevel.fromLevel(rs.getInt("bloom_level")));
                question.setQuestionFormat(QuestionFormat.valueOf(rs.getString("question_format")));
                
                String closedTypeStr = rs.getString("closed_question_type");
                if (closedTypeStr != null) {
                    question.setClosedQuestionType(ClosedQuestionType.valueOf(closedTypeStr));
                }
                
                question.setSolution(rs.getString("solution"));
                
                // Add to list
                questions.add(question);
            }
        }
        
        // Load answer options for all closed questions
        for (Question q : questions) {
            if (q.getQuestionFormat() == QuestionFormat.CLOSED) {
                q.setAnswerOptions(answerOptionDAO.findByQuestionId(q.getId()));
            }
        }
        
        return questions;
    }

    public boolean delete(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnection.getSharedConnection();
            conn.setAutoCommit(false);
            
            // First delete answer options (if any)
            answerOptionDAO.deleteByQuestionId(id, conn);
            
            // Then delete the question
            String sql = "DELETE FROM questions WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            
            int deleted = stmt.executeUpdate();
            conn.commit();
            
            logger.info("Deleted question, affected rows: {}", deleted);
            return deleted > 0;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error during rollback", ex);
                }
            }
            throw e;
        } finally {
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { /* ignored */ }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    // Nicht schließen, da es eine Shared Connection ist
                } catch (SQLException e) { /* ignored */ }
            }
        }
    }
}
package com.klasurapp.dao;

import com.klasurapp.model.Exam;
import com.klasurapp.model.Module;
import com.klasurapp.model.Question;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExamDAO {
    private static final Logger logger = LoggerFactory.getLogger(ExamDAO.class);
    private QuestionDAO questionDAO = new QuestionDAO();

    public Exam save(Exam exam) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getSharedConnection();
            conn.setAutoCommit(false);

            if (exam.getId() == null) {
                // Insert
                String sql = "INSERT INTO exams (name, description, module_id, date_created) " +
                           "VALUES (?, ?, ?, ?) RETURNING id";
                
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, exam.getName());
                stmt.setString(2, exam.getDescription());
                
                if (exam.getModule() != null) {
                    stmt.setInt(3, exam.getModule().getId());
                } else {
                    stmt.setNull(3, Types.INTEGER);
                }
                
                stmt.setTimestamp(4, Timestamp.valueOf(exam.getDateCreated()));
                
                rs = stmt.executeQuery();
                if (rs.next()) {
                    exam.setId(rs.getInt("id"));
                    logger.info("Created new exam with ID: {}", exam.getId());
                }

                // Save exam questions
                if (!exam.getQuestions().isEmpty()) {
                    saveExamQuestions(exam.getId(), exam.getQuestions(), conn);
                }
            } else {
                // Update
                String sql = "UPDATE exams SET name = ?, description = ?, module_id = ? WHERE id = ?";
                
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, exam.getName());
                stmt.setString(2, exam.getDescription());
                
                if (exam.getModule() != null) {
                    stmt.setInt(3, exam.getModule().getId());
                } else {
                    stmt.setNull(3, Types.INTEGER);
                }
                
                stmt.setInt(4, exam.getId());
                
                int updated = stmt.executeUpdate();
                logger.info("Updated exam, affected rows: {}", updated);

                // Update exam questions
                deleteExamQuestions(exam.getId(), conn);
                if (!exam.getQuestions().isEmpty()) {
                    saveExamQuestions(exam.getId(), exam.getQuestions(), conn);
                }
            }
            
            conn.commit();
            return exam;
            
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

    private void saveExamQuestions(int examId, List<Question> questions, Connection conn) throws SQLException {
        // Implementierung beibehalten
    }

    private void deleteExamQuestions(int examId, Connection conn) throws SQLException {
        // Implementierung beibehalten
    }

    public Optional<Exam> findById(int id) throws SQLException {
        String sql = "SELECT e.id, e.name, e.description, e.module_id, e.date_created, " +
                   "m.name as module_name, m.description as module_description " +
                   "FROM exams e " +
                   "LEFT JOIN modules m ON e.module_id = m.id " +
                   "WHERE e.id = ?";
                   
        try (Connection conn = DatabaseConnection.getSharedConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Create the module object if present
                    Module module = null;
                    if (rs.getObject("module_id") != null) {
                        module = new Module(
                            rs.getInt("module_id"),
                            rs.getString("module_name"),
                            rs.getString("module_description")
                        );
                    }
                    
                    // Create the exam object
                    Exam exam = new Exam(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        module,
                        rs.getTimestamp("date_created").toLocalDateTime()
                    );
                    
                    // Load questions
                    loadExamQuestions(exam);
                    
                    return Optional.of(exam);
                }
            }
        }
        return Optional.empty();
    }

    private void loadExamQuestions(Exam exam) throws SQLException {
        String sql = "SELECT eq.question_id, eq.question_order FROM exam_questions eq " +
                   "WHERE eq.exam_id = ? ORDER BY eq.question_order";
                   
        try (Connection conn = DatabaseConnection.getSharedConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, exam.getId());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int questionId = rs.getInt("question_id");
                    questionDAO.findById(questionId).ifPresent(exam::addQuestion);
                }
            }
        }
    }

    public List<Exam> findAll() throws SQLException {
        List<Exam> exams = new ArrayList<>();
        String sql = "SELECT e.id, e.name, e.description, e.module_id, e.date_created, " +
                   "m.name as module_name, m.description as module_description " +
                   "FROM exams e " +
                   "LEFT JOIN modules m ON e.module_id = m.id " +
                   "ORDER BY e.date_created DESC";
                   
        try (Connection conn = DatabaseConnection.getSharedConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                // Create the module object if present
                Module module = null;
                if (rs.getObject("module_id") != null) {
                    module = new Module(
                        rs.getInt("module_id"),
                        rs.getString("module_name"),
                        rs.getString("module_description")
                    );
                }
                
                // Create the exam object
                Exam exam = new Exam(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    module,
                    rs.getTimestamp("date_created").toLocalDateTime()
                );
                
                exams.add(exam);
            }
        }
        
        // Load questions for each exam
        for (Exam exam : exams) {
            loadExamQuestions(exam);
        }
        
        return exams;
    }

    public boolean delete(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnection.getSharedConnection();
            conn.setAutoCommit(false);
            
            // First delete exam questions
            deleteExamQuestions(id, conn);
            
            // Then delete the exam
            String sql = "DELETE FROM exams WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            
            int deleted = stmt.executeUpdate();
            conn.commit();
            
            logger.info("Deleted exam, affected rows: {}", deleted);
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
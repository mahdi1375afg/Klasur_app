package com.klasurapp.service;

import com.klasurapp.dao.QuestionDAO;
import com.klasurapp.dao.AnswerOptionDAO;
import com.klasurapp.model.Question;
import com.klasurapp.model.AnswerOption;
import com.klasurapp.model.Module;
import com.klasurapp.model.enums.BloomLevel;
import com.klasurapp.model.enums.ClosedQuestionType;
import com.klasurapp.model.enums.QuestionFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuestionService {
    private static final Logger logger = LoggerFactory.getLogger(QuestionService.class);
    private final QuestionDAO questionDAO;
    private final AnswerOptionDAO answerOptionDAO;

    public QuestionService() {
        this.questionDAO = new QuestionDAO();
        this.answerOptionDAO = new AnswerOptionDAO();
    }

    /**
     * Findet alle Fragen in der Datenbank.
     */
    public List<Question> findAll() throws SQLException {
        logger.info("Finding all questions");
        return questionDAO.findAll();
    }

    /**
     * Findet eine Frage anhand ihrer ID.
     */
    public Question findById(int id) throws SQLException {
        logger.info("Finding question with ID: {}", id);
        return questionDAO.findById(id)
                .orElseThrow(() -> new SQLException("Keine Frage mit ID " + id + " gefunden"));
    }

    /**
     * Findet alle Fragen für ein bestimmtes Modul.
     */
    public List<Question> findByModule(int moduleId) throws SQLException {
        logger.info("Finding questions for module with ID: {}", moduleId);
        return questionDAO.findByModule(moduleId);
    }

    /**
     * Speichert eine Frage in der Datenbank.
     */
    public Question save(Question question) throws SQLException {
        logger.info("Saving question: {}", question.getName());
        return questionDAO.save(question);
    }

    /**
     * Löscht eine Frage aus der Datenbank.
     */
    public boolean delete(int id) throws SQLException {
        logger.info("Deleting question with ID: {}", id);
        return questionDAO.delete(id);
    }

    /**
     * Erstellt eine neue offene Frage.
     */
    public Question createOpenQuestion(String name, String questionText, int estimatedTime, 
                                     Module module, BloomLevel bloomLevel, String solution) throws SQLException {
        logger.info("Creating new open question: {}", name);
        
        Question question = new Question();
        question.setName(name);
        question.setQuestionText(questionText);
        question.setEstimatedTimeMinutes(estimatedTime);
        question.setModule(module);
        question.setBloomLevel(bloomLevel);
        question.setQuestionFormat(QuestionFormat.OPEN);
        question.setSolution(solution);
        
        return save(question);
    }

    /**
     * Erstellt eine neue geschlossene Frage.
     */
    public Question createClosedQuestion(String name, String questionText, int estimatedTime, 
                                       Module module, BloomLevel bloomLevel, 
                                       ClosedQuestionType closedType, List<AnswerOption> options) throws SQLException {
        logger.info("Creating new closed question: {}", name);
        
        Question question = new Question();
        question.setName(name);
        question.setQuestionText(questionText);
        question.setEstimatedTimeMinutes(estimatedTime);
        question.setModule(module);
        question.setBloomLevel(bloomLevel);
        question.setQuestionFormat(QuestionFormat.CLOSED);
        question.setClosedQuestionType(closedType);
        
        // Speichern, damit die Frage eine ID erhält
        questionDAO.save(question);
        
        // Antwortoptionen speichern
        if (options != null && !options.isEmpty()) {
            for (AnswerOption option : options) {
                option.setQuestion(question);
                answerOptionDAO.save(option);
            }
            
            // Antwortoptionen laden und dem Frage-Objekt zuweisen
            List<AnswerOption> savedOptions = answerOptionDAO.findByQuestionId(question.getId());
            question.setAnswerOptions(savedOptions);
        } else {
            question.setAnswerOptions(new ArrayList<>());
        }
        
        return question;
    }

    /**
     * Aktualisiert eine bestehende Frage.
     */
    public Question updateQuestion(Question question) throws SQLException {
        logger.info("Updating question with ID: {}", question.getId());
        
        if (question.getId() == null) {
            throw new IllegalArgumentException("Question ID must not be null for update");
        }
        
        // Bei geschlossenen Fragen auch die Antwortoptionen aktualisieren
        if (question.getQuestionFormat() == QuestionFormat.CLOSED) {
            // Antwortoptionen werden im DAO gelöscht und neu eingefügt
        }
        
        return questionDAO.save(question);
    }

    /**
     * Fügt einer geschlossenen Frage eine Antwortoption hinzu.
     */
    public AnswerOption addAnswerOption(Question question, String optionText, boolean isCorrect, Integer order) throws SQLException {
        logger.info("Adding answer option to question with ID: {}", question.getId());
        
        if (question.getQuestionFormat() != QuestionFormat.CLOSED) {
            throw new IllegalArgumentException("Answer options can only be added to closed questions");
        }
        
        AnswerOption option = new AnswerOption();
        option.setQuestion(question);
        option.setOptionText(optionText);
        option.setCorrect(isCorrect);
        option.setOptionOrder(order);
        
        answerOptionDAO.save(option);
        
        // Zur Liste der Antwortoptionen der Frage hinzufügen
        question.getAnswerOptions().add(option);
        
        return option;
    }

    /**
     * Entfernt eine Antwortoption von einer Frage.
     */
    public boolean removeAnswerOption(Question question, AnswerOption option) throws SQLException {
        logger.info("Removing answer option from question with ID: {}", question.getId());
        
        if (question.getQuestionFormat() != QuestionFormat.CLOSED) {
            throw new IllegalArgumentException("Answer options can only be removed from closed questions");
        }
        
        // Option aus der Liste entfernen und Frage aktualisieren
        question.getAnswerOptions().remove(option);
        questionDAO.save(question);
        
        return true;
    }
}
package com.klasurapp.service;

import com.klasurapp.dao.ModuleDAO;
import com.klasurapp.dao.QuestionDAO;
import com.klasurapp.model.Module;
import com.klasurapp.model.Question;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class ModuleService {
    private static final Logger logger = LoggerFactory.getLogger(ModuleService.class);
    private final ModuleDAO moduleDAO;
    private final QuestionDAO questionDAO;

    public ModuleService() {
        this.moduleDAO = new ModuleDAO();
        this.questionDAO = new QuestionDAO();
    }

    /**
     * Findet alle Module in der Datenbank.
     */
    public List<Module> findAll() throws SQLException {
        logger.info("Finding all modules");
        return moduleDAO.findAll();
    }

    /**
     * Findet ein Modul anhand seiner ID.
     */
    public Module findById(int id) throws SQLException {
        logger.info("Finding module with ID: {}", id);
        return moduleDAO.findById(id)
                .orElseThrow(() -> new SQLException("Kein Modul mit ID " + id + " gefunden"));
    }

    /**
     * Speichert ein Modul in der Datenbank.
     */
    public Module save(Module module) throws SQLException {
        logger.info("Saving module: {}", module.getName());
        return moduleDAO.save(module);
    }

    /**
     * Löscht ein Modul aus der Datenbank.
     * Hinweis: Dies könnte fehlschlagen, wenn das Modul in Fragen oder Klausuren verwendet wird.
     */
    public boolean delete(int id) throws SQLException {
        logger.info("Deleting module with ID: {}", id);
        return moduleDAO.delete(id);
    }

    /**
     * Zählt die Anzahl der Fragen für ein bestimmtes Modul.
     */
    public int countQuestionsForModule(int moduleId) throws SQLException {
        logger.info("Counting questions for module with ID: {}", moduleId);
        List<Question> questions = questionDAO.findByModule(moduleId);
        return questions.size();
    }

    /**
     * Erstellt ein neues Modul mit den angegebenen Informationen.
     */
    public Module createModule(String name, String description) throws SQLException {
        logger.info("Creating new module: {}", name);
        Module module = new Module();
        module.setName(name);
        module.setDescription(description);
        return save(module);
    }

    /**
     * Aktualisiert ein bestehendes Modul.
     */
    public Module updateModule(int id, String name, String description) throws SQLException {
        logger.info("Updating module with ID: {}", id);
        Module module = findById(id);
        module.setName(name);
        module.setDescription(description);
        return save(module);
    }
}
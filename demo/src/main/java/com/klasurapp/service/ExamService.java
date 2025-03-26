package com.klasurapp.service;

import com.klasurapp.dao.ExamDAO;
import com.klasurapp.dao.QuestionDAO;
import com.klasurapp.model.Exam;
import com.klasurapp.model.Module;
import com.klasurapp.model.Question;
import com.klasurapp.model.AnswerOption;
import com.klasurapp.model.enums.BloomLevel;
import com.klasurapp.model.enums.ClosedQuestionType;
import com.klasurapp.model.enums.QuestionFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ExamService {
    private static final Logger logger = LoggerFactory.getLogger(ExamService.class);
    private final ExamDAO examDAO;
    private final QuestionDAO questionDAO;

    public ExamService() {
        this.examDAO = new ExamDAO();
        this.questionDAO = new QuestionDAO();
    }

    /**
     * Findet alle vorhandenen Klausuren in der Datenbank.
     */
    public List<Exam> findAll() throws SQLException {
        logger.info("Finding all exams");
        return examDAO.findAll();
    }

    /**
     * Findet eine Klausur anhand ihrer ID.
     */
    public Exam findById(int id) throws SQLException {
        logger.info("Finding exam with ID: {}", id);
        return examDAO.findById(id)
                .orElseThrow(() -> new SQLException("Keine Klausur mit ID " + id + " gefunden"));
    }

    /**
     * Speichert eine Klausur in der Datenbank.
     */
    public Exam save(Exam exam) throws SQLException {
        logger.info("Saving exam: {}", exam.getName());
        return examDAO.save(exam);
    }

    /**
     * Löscht eine Klausur aus der Datenbank.
     */
    public boolean delete(int id) throws SQLException {
        logger.info("Deleting exam with ID: {}", id);
        return examDAO.delete(id);
    }

    /**
     * Generiert eine neue Klausur basierend auf den angegebenen Kriterien.
     */
    public Exam generateExam(String name, Module module, int questionCount, int estimatedDuration,
                          List<BloomLevel> bloomLevels, List<QuestionFormat> formats,
                          List<ClosedQuestionType> closedTypes) throws SQLException {
        
        logger.info("Generating exam '{}' for module '{}'", name, module.getName());
        
        // 1. Verfügbare Fragen holen, die den Kriterien entsprechen
        List<Question> availableQuestions = questionDAO.findByModule(module.getId());
        
        // 2. Nach Bloom-Level filtern
        if (bloomLevels != null && !bloomLevels.isEmpty()) {
            availableQuestions = availableQuestions.stream()
                .filter(q -> bloomLevels.contains(q.getBloomLevel()))
                .collect(Collectors.toList());
        }
        
        // 3. Nach Format (offen/geschlossen) filtern
        if (formats != null && !formats.isEmpty()) {
            availableQuestions = availableQuestions.stream()
                .filter(q -> formats.contains(q.getQuestionFormat()))
                .collect(Collectors.toList());
        }
        
        // 4. Nach geschlossenem Aufgabentyp filtern
        if (closedTypes != null && !closedTypes.isEmpty()) {
            availableQuestions = availableQuestions.stream()
                .filter(q -> q.getQuestionFormat() != QuestionFormat.CLOSED || 
                           closedTypes.contains(q.getClosedQuestionType()))
                .collect(Collectors.toList());
        }
        
        logger.info("Found {} questions matching criteria", availableQuestions.size());
        
        // 5. Fragen basierend auf Zeit und Anzahl auswählen
        List<Question> selectedQuestions = selectQuestions(availableQuestions, questionCount, estimatedDuration);
        
        // 6. Klausur-Objekt erstellen
        Exam exam = new Exam();
        exam.setName(name);
        exam.setDescription("Automatisch generierte Klausur");
        exam.setModule(module);
        exam.setDateCreated(LocalDateTime.now());
        exam.setQuestions(selectedQuestions);
        
        logger.info("Generated exam with {} questions", selectedQuestions.size());
        
        return exam;
    }

    /**
     * Selektiert Fragen basierend auf Anzahl und Zeit.
     * Versucht, die Fragen optimal zu verteilen, um sowohl die gewünschte Anzahl
     * als auch die geschätzte Gesamtzeit zu erreichen.
     */
    private List<Question> selectQuestions(List<Question> availableQuestions, int targetCount, int targetDurationMinutes) {
        if (availableQuestions.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Wenn weniger Fragen vorhanden sind als gewünscht, alle zurückgeben
        if (availableQuestions.size() <= targetCount) {
            return new ArrayList<>(availableQuestions);
        }
        
        // Fragen nach geschätzter Zeit sortieren
        availableQuestions.sort(Comparator.comparingInt(Question::getEstimatedTimeMinutes));
        
        // Algorithmus zur Auswahl der Fragen unter Berücksichtigung von Zeit und Anzahl
        List<Question> selected = new ArrayList<>();
        int currentDuration = 0;
        
        // Mische die Fragen, um Vielfalt zu gewährleisten, aber behalte die Sortierung nach Zeit bei
        List<Question> shortQuestions = new ArrayList<>();
        List<Question> mediumQuestions = new ArrayList<>();
        List<Question> longQuestions = new ArrayList<>();
        
        for (Question q : availableQuestions) {
            int time = q.getEstimatedTimeMinutes();
            if (time <= 5) {
                shortQuestions.add(q);
            } else if (time <= 15) {
                mediumQuestions.add(q);
            } else {
                longQuestions.add(q);
            }
        }
        
        // Versuche zuerst einige längere Fragen hinzuzufügen
        for (Question q : longQuestions) {
            if (selected.size() < targetCount * 0.3 && currentDuration + q.getEstimatedTimeMinutes() <= targetDurationMinutes * 0.7) {
                selected.add(q);
                currentDuration += q.getEstimatedTimeMinutes();
            }
        }
        
        // Füge mittlere Fragen hinzu
        for (Question q : mediumQuestions) {
            if (selected.size() < targetCount * 0.7 && currentDuration + q.getEstimatedTimeMinutes() <= targetDurationMinutes * 0.9) {
                selected.add(q);
                currentDuration += q.getEstimatedTimeMinutes();
            }
        }
        
        // Fülle mit kurzen Fragen auf
        for (Question q : shortQuestions) {
            if (selected.size() < targetCount && currentDuration + q.getEstimatedTimeMinutes() <= targetDurationMinutes) {
                selected.add(q);
                currentDuration += q.getEstimatedTimeMinutes();
            }
        }
        
        // Wenn wir zu viele Fragen haben oder zu viel Zeit, entferne einige
        while (selected.size() > targetCount || currentDuration > targetDurationMinutes * 1.1) {
            if (!selected.isEmpty()) {
                Question removed = selected.remove(selected.size() - 1);
                currentDuration -= removed.getEstimatedTimeMinutes();
            } else {
                break;
            }
        }
        
        // Wenn wir zu wenige Fragen haben, füge weitere hinzu, auch wenn Zeit überschritten wird
        for (Question q : availableQuestions) {
            if (selected.size() < targetCount && !selected.contains(q)) {
                selected.add(q);
                currentDuration += q.getEstimatedTimeMinutes();
            }
            
            if (selected.size() >= targetCount) {
                break;
            }
        }
        
        logger.info("Selected {} questions with total duration {} minutes", selected.size(), currentDuration);
        return selected;
    }

    /**
     * Exportiert eine Klausur in ein formatiertes Text-Format.
     */
    public String exportExam(Exam exam) {
        logger.info("Exporting exam: {}", exam.getName());
        
        StringBuilder sb = new StringBuilder();
        
        // Kopfdaten
        sb.append("KLAUSUR: ").append(exam.getName()).append("\n");
        sb.append("Modul: ").append(exam.getModule() != null ? exam.getModule().getName() : "-").append("\n");
        sb.append("Datum: ").append(exam.getDateCreated().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))).append("\n");
        sb.append("Anzahl Fragen: ").append(exam.getQuestions().size()).append("\n\n");
        
        // Fragen
        int questionNumber = 1;
        for (Question question : exam.getQuestions()) {
            sb.append(questionNumber++).append(". ").append(question.getName()).append(" (")
              .append(question.getEstimatedTimeMinutes()).append(" Min, ")
              .append(question.getBloomLevel().getName()).append(")\n");
            sb.append(question.getQuestionText()).append("\n\n");
            
            // Bei geschlossenen Fragen auch die Antwortmöglichkeiten anzeigen
            if (question.getQuestionFormat() == QuestionFormat.CLOSED) {
                int optionNumber = 1;
                for (AnswerOption option : question.getAnswerOptions()) {
                    sb.append("   ");
                    
                    // Je nach Typ der geschlossenen Frage andere Darstellung
                    if (question.getClosedQuestionType() == ClosedQuestionType.SINGLE_CHOICE || 
                        question.getClosedQuestionType() == ClosedQuestionType.MULTIPLE_CHOICE) {
                        sb.append(optionNumber).append(") ");
                    } else if (question.getClosedQuestionType() == ClosedQuestionType.TRUE_FALSE) {
                        sb.append(option.isCorrect() ? "Wahr: " : "Falsch: ");
                    } else if (question.getClosedQuestionType() == ClosedQuestionType.RANKING && option.getOptionOrder() != null) {
                        sb.append(option.getOptionOrder()).append(". ");
                    }
                    
                    sb.append(option.getOptionText()).append("\n");
                    optionNumber++;
                }
                sb.append("\n");
            }
            
            // Bei offenen Fragen die Musterlösung anzeigen
            if (question.getQuestionFormat() == QuestionFormat.OPEN && question.getSolution() != null) {
                sb.append("Musterlösung:\n");
                sb.append(question.getSolution()).append("\n\n");
            }
            
            sb.append("----------------------------------------\n\n");
        }
        
        return sb.toString();
    }
}
package com.klasurapp.ui;

import com.klasurapp.model.enums.BloomLevel;
import com.klasurapp.model.enums.ClosedQuestionType;
import com.klasurapp.model.enums.QuestionFormat;
import com.klasurapp.model.Module;
import com.klasurapp.model.Question;
import com.klasurapp.model.Exam;
import com.klasurapp.service.ExamService;
import com.klasurapp.service.ModuleService;
import com.klasurapp.service.QuestionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainFrame extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);
    
    private final QuestionService questionService;
    private final ModuleService moduleService;
    private final ExamService examService;
    
    private JTabbedPane tabbedPane;
    private QuestionPanel questionPanel;
    private ModulePanel modulePanel;
    private ExamGeneratorPanel examGeneratorPanel;
    private ExamViewerPanel examViewerPanel;
    private JLabel statusLabel;

    public MainFrame() {
        // Initialize services
        this.questionService = new QuestionService();
        this.moduleService = new ModuleService();
        this.examService = new ExamService();
        
        setupUI();
        
        // Setup frame properties
        setTitle("Klausur-Generator");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Add window closing handler
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
        
        logger.info("MainFrame initialized");
    }
    
    private void setupUI() {
        // Create main menu
        JMenuBar menuBar = createMenuBar();
        setJMenuBar(menuBar);
        
        // Initialize panels
        questionPanel = new QuestionPanel(questionService, moduleService);
        modulePanel = new ModulePanel(moduleService);
        examGeneratorPanel = new ExamGeneratorPanel(examService, questionService, moduleService);
        examViewerPanel = new ExamViewerPanel(examService);
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Aufgaben", new JScrollPane(questionPanel));
        tabbedPane.addTab("Module", new JScrollPane(modulePanel));
        tabbedPane.addTab("Klausur erstellen", new JScrollPane(examGeneratorPanel));
        tabbedPane.addTab("Klausuren ansehen", new JScrollPane(examViewerPanel));
        
        // Set content pane
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPane.add(tabbedPane, BorderLayout.CENTER);
        
        // Status bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusLabel = new JLabel("Bereit");
        statusBar.add(statusLabel, BorderLayout.WEST);
        contentPane.add(statusBar, BorderLayout.SOUTH);
        
        setContentPane(contentPane);
    }
    
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("Datei");
        
        JMenuItem importItem = new JMenuItem("Importieren...");
        importItem.addActionListener(this::importData);
        
        JMenuItem exportItem = new JMenuItem("Exportieren...");
        exportItem.addActionListener(this::exportData);
        
        JMenuItem printItem = new JMenuItem("Drucken...");
        printItem.addActionListener(this::printContent);
        
        JMenuItem exitItem = new JMenuItem("Beenden");
        exitItem.addActionListener(e -> exitApplication());
        
        fileMenu.add(importItem);
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(printItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Hilfe");
        
        JMenuItem aboutItem = new JMenuItem("Über");
        aboutItem.addActionListener(e -> showAboutDialog());
        
        helpMenu.add(aboutItem);
        
        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    private void importData(ActionEvent e) {
        logger.info("Import action triggered");
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            // Implementation for importing data
            JOptionPane.showMessageDialog(this, 
                "Import-Funktion noch nicht implementiert.", 
                "Information", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void exportData(ActionEvent e) {
        logger.info("Export action triggered");
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            // Implementation for exporting data
            JOptionPane.showMessageDialog(this, 
                "Export-Funktion noch nicht implementiert.", 
                "Information", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void printContent(ActionEvent e) {
        logger.info("Print action triggered");
        int tabIndex = tabbedPane.getSelectedIndex();
        String tabTitle = tabbedPane.getTitleAt(tabIndex);
        
        JOptionPane.showMessageDialog(this, 
            "Drucken von '" + tabTitle + "' noch nicht implementiert.", 
            "Information", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
            "Klausur-Generator\nVersion 1.0\n© 2025",
            "Über Klausur-Generator",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void exitApplication() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Möchten Sie die Anwendung wirklich beenden?",
            "Beenden bestätigen",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            logger.info("Application shutting down");
            dispose();
            System.exit(0);
        }
    }
    
    // Panel für Fragen/Aufgaben
    private class QuestionPanel extends JPanel {
        private final QuestionService questionService;
        private final ModuleService moduleService;
        private JTable questionsTable;
        private DefaultTableModel tableModel;
        private List<Question> questions = new ArrayList<>();
        
        public QuestionPanel(QuestionService questionService, ModuleService moduleService) {
            this.questionService = questionService;
            this.moduleService = moduleService;
            
            setLayout(new BorderLayout(10, 10));
            
            // Toolbar mit Aktionen
            JToolBar toolbar = new JToolBar();
            toolbar.setFloatable(false);
            
            JButton addButton = new JButton("Neue Aufgabe");
            addButton.addActionListener(e -> showAddQuestionDialog());
            
            JButton editButton = new JButton("Bearbeiten");
            editButton.addActionListener(e -> {
                int selectedRow = questionsTable.getSelectedRow();
                if (selectedRow >= 0) {
                    Question question = questions.get(selectedRow);
                    showEditQuestionDialog(question);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Bitte wählen Sie eine Aufgabe aus der Tabelle.",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            });
            
            JButton deleteButton = new JButton("Löschen");
            deleteButton.addActionListener(e -> {
                int selectedRow = questionsTable.getSelectedRow();
                if (selectedRow >= 0) {
                    Question question = questions.get(selectedRow);
                    deleteQuestion(question);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Bitte wählen Sie eine Aufgabe aus der Tabelle.",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            });
            
            toolbar.add(addButton);
            toolbar.add(editButton);
            toolbar.add(deleteButton);
            
            add(toolbar, BorderLayout.NORTH);
            
            // Tabelle für Aufgaben
            String[] columnNames = {"Name", "Modul", "Bloom Level", "Format", "Geschätzte Zeit (Min)"};
            tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Tabellenzellen nicht editierbar
                }
            };
            
            questionsTable = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(questionsTable);
            
            add(scrollPane, BorderLayout.CENTER);
            
            // Such- und Filterleiste
            JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            filterPanel.add(new JLabel("Suche:"));
            JTextField searchField = new JTextField(20);
            filterPanel.add(searchField);
            
            filterPanel.add(new JLabel("Modul:"));
            JComboBox<String> moduleComboBox = new JComboBox<>();
            filterPanel.add(moduleComboBox);
            
            filterPanel.add(new JLabel("Format:"));
            JComboBox<QuestionFormat> formatComboBox = new JComboBox<>(QuestionFormat.values());
            filterPanel.add(formatComboBox);
            
            JButton filterButton = new JButton("Filtern");
            filterButton.addActionListener(e -> loadQuestions());
            filterPanel.add(filterButton);
            
            add(filterPanel, BorderLayout.SOUTH);
            
            // Aufgaben beim Start laden
            loadQuestions();
        }
        
        private void loadQuestions() {
            try {
                questions = questionService.findAll();
                updateQuestionTable();
                statusLabel.setText("Aufgaben geladen: " + questions.size());
            } catch (SQLException ex) {
                logger.error("Error loading questions", ex);
                JOptionPane.showMessageDialog(this,
                    "Fehler beim Laden der Aufgaben: " + ex.getMessage(),
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        
        private void updateQuestionTable() {
            tableModel.setRowCount(0);
            
            for (Question question : questions) {
                Object[] row = new Object[5];
                row[0] = question.getName();
                row[1] = question.getModule() != null ? question.getModule().getName() : "-";
                row[2] = question.getBloomLevel().getName();
                row[3] = question.getQuestionFormat().getDisplayName();
                row[4] = question.getEstimatedTimeMinutes();
                
                tableModel.addRow(row);
            }
        }
        
        private void showAddQuestionDialog() {
            // Implementation für Dialog zum Hinzufügen einer neuen Aufgabe
            JOptionPane.showMessageDialog(this,
                "Dialog zum Hinzufügen einer neuen Aufgabe wird implementiert.",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
        }
        
        private void showEditQuestionDialog(Question question) {
            // Implementation für Dialog zum Bearbeiten einer Aufgabe
            JOptionPane.showMessageDialog(this,
                "Dialog zum Bearbeiten einer Aufgabe wird implementiert.",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
        }
        
        private void deleteQuestion(Question question) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Möchten Sie die Aufgabe '" + question.getName() + "' wirklich löschen?",
                "Löschen bestätigen",
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    questionService.delete(question.getId());
                    loadQuestions();
                    statusLabel.setText("Aufgabe gelöscht: " + question.getName());
                } catch (SQLException ex) {
                    logger.error("Error deleting question", ex);
                    JOptionPane.showMessageDialog(this,
                        "Fehler beim Löschen der Aufgabe: " + ex.getMessage(),
                        "Fehler",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    // Panel für Module
    private class ModulePanel extends JPanel {
        private final ModuleService moduleService;
        private JTable modulesTable;
        private DefaultTableModel tableModel;
        private List<Module> modules = new ArrayList<>();
        
        public ModulePanel(ModuleService moduleService) {
            this.moduleService = moduleService;
            
            setLayout(new BorderLayout(10, 10));
            
            // Toolbar
            JToolBar toolbar = new JToolBar();
            toolbar.setFloatable(false);
            
            JButton addButton = new JButton("Neues Modul");
            addButton.addActionListener(e -> showAddModuleDialog());
            
            JButton editButton = new JButton("Bearbeiten");
            editButton.addActionListener(e -> {
                int selectedRow = modulesTable.getSelectedRow();
                if (selectedRow >= 0) {
                    Module module = modules.get(selectedRow);
                    showEditModuleDialog(module);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Bitte wählen Sie ein Modul aus der Tabelle.",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            });
            
            JButton deleteButton = new JButton("Löschen");
            deleteButton.addActionListener(e -> {
                int selectedRow = modulesTable.getSelectedRow();
                if (selectedRow >= 0) {
                    Module module = modules.get(selectedRow);
                    deleteModule(module);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Bitte wählen Sie ein Modul aus der Tabelle.",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            });
            
            toolbar.add(addButton);
            toolbar.add(editButton);
            toolbar.add(deleteButton);
            
            add(toolbar, BorderLayout.NORTH);
            
            // Tabelle für Module
            String[] columnNames = {"Name", "Beschreibung", "Anzahl Aufgaben"};
            tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            modulesTable = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(modulesTable);
            
            add(scrollPane, BorderLayout.CENTER);
            
            // Module beim Start laden
            loadModules();
        }
        
        private void loadModules() {
            try {
                modules = moduleService.findAll();
                updateModuleTable();
                statusLabel.setText("Module geladen: " + modules.size());
            } catch (SQLException ex) {
                logger.error("Error loading modules", ex);
                JOptionPane.showMessageDialog(this,
                    "Fehler beim Laden der Module: " + ex.getMessage(),
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        
        private void updateModuleTable() {
            tableModel.setRowCount(0);
            
            for (Module module : modules) {
                Object[] row = new Object[3];
                row[0] = module.getName();
                row[1] = module.getDescription();
                row[2] = "-"; // Anzahl der Aufgaben müsste ermittelt werden
                
                tableModel.addRow(row);
            }
        }
        
        private void showAddModuleDialog() {
            // Implementation für Dialog zum Hinzufügen eines neuen Moduls
            JOptionPane.showMessageDialog(this,
                "Dialog zum Hinzufügen eines neuen Moduls wird implementiert.",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
        }
        
        private void showEditModuleDialog(Module module) {
            // Implementation für Dialog zum Bearbeiten eines Moduls
            JOptionPane.showMessageDialog(this,
                "Dialog zum Bearbeiten eines Moduls wird implementiert.",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
        }
        
        private void deleteModule(Module module) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Möchten Sie das Modul '" + module.getName() + "' wirklich löschen?",
                "Löschen bestätigen",
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    moduleService.delete(module.getId());
                    loadModules();
                    statusLabel.setText("Modul gelöscht: " + module.getName());
                } catch (SQLException ex) {
                    logger.error("Error deleting module", ex);
                    JOptionPane.showMessageDialog(this,
                        "Fehler beim Löschen des Moduls: " + ex.getMessage(),
                        "Fehler",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    // Panel für Klausurgenerierung
    private class ExamGeneratorPanel extends JPanel {
        private final ExamService examService;
        private final QuestionService questionService;
        private final ModuleService moduleService;
        
        private JTextField examNameField;
        private JComboBox<Module> moduleComboBox;
        private JSpinner questionCountSpinner;
        private JSpinner timeSpinner;
        private JCheckBox[] bloomLevelCheckboxes;
        private JCheckBox[] formatCheckboxes;
        private JCheckBox[] closedTypeCheckboxes;
        private JTextArea previewArea;
        private Exam generatedExam;
        
        public ExamGeneratorPanel(ExamService examService, QuestionService questionService, ModuleService moduleService) {
            this.examService = examService;
            this.questionService = questionService;
            this.moduleService = moduleService;
            
            setLayout(new BorderLayout(10, 10));
            
            // Formular für Klausurkriterien
            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            
            // Formularfelder hinzufügen
            gbc.gridx = 0;
            gbc.gridy = 0;
            formPanel.add(new JLabel("Klausurname:"), gbc);
            
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            examNameField = new JTextField(20);
            formPanel.add(examNameField, gbc);
            
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            formPanel.add(new JLabel("Modul:"), gbc);
            
            gbc.gridx = 1;
            moduleComboBox = new JComboBox<>();
            formPanel.add(moduleComboBox, gbc);
            
            gbc.gridx = 0;
            gbc.gridy = 2;
            formPanel.add(new JLabel("Anzahl Aufgaben:"), gbc);
            
            gbc.gridx = 1;
            questionCountSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 50, 1));
            formPanel.add(questionCountSpinner, gbc);
            
            gbc.gridx = 0;
            gbc.gridy = 3;
            formPanel.add(new JLabel("Geschätzte Zeit (Min):"), gbc);
            
            gbc.gridx = 1;
            timeSpinner = new JSpinner(new SpinnerNumberModel(90, 15, 240, 15));
            formPanel.add(timeSpinner, gbc);
            
            gbc.gridx = 0;
            gbc.gridy = 4;
            formPanel.add(new JLabel("Bloom Level:"), gbc);
            
            gbc.gridx = 1;
            JPanel bloomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            BloomLevel[] bloomLevels = BloomLevel.values();
            bloomLevelCheckboxes = new JCheckBox[bloomLevels.length];
            
            for (int i = 0; i < bloomLevels.length; i++) {
                BloomLevel level = bloomLevels[i];
                bloomLevelCheckboxes[i] = new JCheckBox(level.getName());
                bloomPanel.add(bloomLevelCheckboxes[i]);
            }
            formPanel.add(bloomPanel, gbc);
            
            gbc.gridx = 0;
            gbc.gridy = 5;
            formPanel.add(new JLabel("Aufgabenformat:"), gbc);
            
            gbc.gridx = 1;
            JPanel formatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            QuestionFormat[] formats = QuestionFormat.values();
            formatCheckboxes = new JCheckBox[formats.length];
            
            for (int i = 0; i < formats.length; i++) {
                QuestionFormat format = formats[i];
                formatCheckboxes[i] = new JCheckBox(format.getDisplayName());
                formatPanel.add(formatCheckboxes[i]);
            }
            formPanel.add(formatPanel, gbc);
            
            gbc.gridx = 0;
            gbc.gridy = 6;
            formPanel.add(new JLabel("Typ geschlossener Aufgaben:"), gbc);
            
            gbc.gridx = 1;
            JPanel closedTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            ClosedQuestionType[] closedTypes = ClosedQuestionType.values();
            closedTypeCheckboxes = new JCheckBox[closedTypes.length];
            
            for (int i = 0; i < closedTypes.length; i++) {
                ClosedQuestionType type = closedTypes[i];
                closedTypeCheckboxes[i] = new JCheckBox(type.getDisplayName());
                closedTypePanel.add(closedTypeCheckboxes[i]);
            }
            formPanel.add(closedTypePanel, gbc);
            
            gbc.gridx = 0;
            gbc.gridy = 7;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            JButton generateButton = new JButton("Klausur generieren");
            generateButton.addActionListener(e -> generateExam());
            formPanel.add(generateButton, gbc);
            
            add(formPanel, BorderLayout.NORTH);
            
            // Vorschau der generierten Klausur
            JPanel previewPanel = new JPanel(new BorderLayout());
            previewPanel.setBorder(BorderFactory.createTitledBorder("Vorschau"));
            
            previewArea = new JTextArea();
            previewArea.setEditable(false);
            previewPanel.add(new JScrollPane(previewArea), BorderLayout.CENTER);
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            
            JButton saveButton = new JButton("Speichern");
            saveButton.addActionListener(e -> saveExam());
            
            JButton printButton = new JButton("Drucken");
            printButton.addActionListener(e -> printExam());
            
            buttonPanel.add(saveButton);
            buttonPanel.add(printButton);
            previewPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            add(previewPanel, BorderLayout.CENTER);
            
            // Module beim Start laden
            loadModules();
        }
        
        private void loadModules() {
            try {
                List<Module> modules = moduleService.findAll();
                moduleComboBox.removeAllItems();
                
                for (Module module : modules) {
                    moduleComboBox.addItem(module);
                }
                
                if (!modules.isEmpty()) {
                    moduleComboBox.setSelectedIndex(0);
                }
            } catch (SQLException ex) {
                logger.error("Error loading modules", ex);
                JOptionPane.showMessageDialog(this,
                    "Fehler beim Laden der Module: " + ex.getMessage(),
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        
        private void generateExam() {
            // Parameter aus dem Formular sammeln
            String examName = examNameField.getText().trim();
            if (examName.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Bitte geben Sie einen Namen für die Klausur ein.",
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Module selectedModule = (Module) moduleComboBox.getSelectedItem();
            if (selectedModule == null) {
                JOptionPane.showMessageDialog(this,
                    "Bitte wählen Sie ein Modul aus.",
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int questionCount = (int) questionCountSpinner.getValue();
            int estimatedDuration = (int) timeSpinner.getValue();
            
            // Ausgewählte Bloom-Level sammeln
            List<BloomLevel> selectedBloomLevels = new ArrayList<>();
            for (int i = 0; i < bloomLevelCheckboxes.length; i++) {
                if (bloomLevelCheckboxes[i].isSelected()) {
                    selectedBloomLevels.add(BloomLevel.values()[i]);
                }
            }
            
            // Ausgewählte Formate sammeln
            List<QuestionFormat> selectedFormats = new ArrayList<>();
            for (int i = 0; i < formatCheckboxes.length; i++) {
                if (formatCheckboxes[i].isSelected()) {
                    selectedFormats.add(QuestionFormat.values()[i]);
                }
            }
            
            // Ausgewählte geschlossene Aufgabentypen sammeln
            List<ClosedQuestionType> selectedClosedTypes = new ArrayList<>();
            for (int i = 0; i < closedTypeCheckboxes.length; i++) {
                if (closedTypeCheckboxes[i].isSelected()) {
                    selectedClosedTypes.add(ClosedQuestionType.values()[i]);
                }
            }
            
            // Überprüfen, ob Auswahlkriterien vorhanden sind
            if (selectedBloomLevels.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Bitte wählen Sie mindestens einen Bloom-Level aus.",
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (selectedFormats.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Bitte wählen Sie mindestens ein Aufgabenformat aus.",
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Klausur generieren
            try {
                Exam exam = examService.generateExam(
                    examName, 
                    selectedModule, 
                    questionCount, 
                    estimatedDuration, 
                    selectedBloomLevels, 
                    selectedFormats, 
                    selectedClosedTypes
                );
                
                // Vorschau anzeigen
                if (exam != null && !exam.getQuestions().isEmpty()) {
                    generatedExam = exam;
                    String preview = examService.exportExam(exam);
                    previewArea.setText(preview);
                    statusLabel.setText("Klausur erfolgreich generiert: " + exam.getQuestions().size() + " Aufgaben");
                } else {
                    previewArea.setText("Keine passenden Aufgaben gefunden. Bitte ändern Sie die Auswahlkriterien.");
                    statusLabel.setText("Keine passenden Aufgaben gefunden");
                }
            } catch (Exception ex) {
                logger.error("Error generating exam", ex);
                JOptionPane.showMessageDialog(this,
                    "Fehler bei der Klausurgenerierung: " + ex.getMessage(),
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        
        private void saveExam() {
            if (generatedExam == null) {
                JOptionPane.showMessageDialog(this,
                    "Bitte generieren Sie zuerst eine Klausur.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            try {
                examService.save(generatedExam);
                JOptionPane.showMessageDialog(this,
                    "Die Klausur wurde erfolgreich gespeichert.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
                statusLabel.setText("Klausur gespeichert: " + generatedExam.getName());
            } catch (SQLException ex) {
                logger.error("Error saving exam", ex);
                JOptionPane.showMessageDialog(this,
                    "Fehler beim Speichern der Klausur: " + ex.getMessage(),
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        
        private void printExam() {
            if (generatedExam == null) {
                JOptionPane.showMessageDialog(this,
                    "Bitte generieren Sie zuerst eine Klausur.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Hier würde die Druckfunktionalität implementiert werden
            JOptionPane.showMessageDialog(this,
                "Druckfunktion noch nicht implementiert.",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    // Panel zum Anzeigen vorhandener Klausuren
    private class ExamViewerPanel extends JPanel {
        private final ExamService examService;
        private JTable examsTable;
        private DefaultTableModel tableModel;
        private List<Exam> exams = new ArrayList<>();
        private JTextArea previewArea;
        
        public ExamViewerPanel(ExamService examService) {
            this.examService = examService;
            
            setLayout(new BorderLayout(10, 10));
            
            // Toolbar
            JToolBar toolbar = new JToolBar();
            toolbar.setFloatable(false);
            
            JButton refreshButton = new JButton("Aktualisieren");
            refreshButton.addActionListener(e -> loadExams());
            
            JButton deleteButton = new JButton("Löschen");
            deleteButton.addActionListener(e -> {
                int selectedRow = examsTable.getSelectedRow();
                if (selectedRow >= 0) {
                    Exam exam = exams.get(selectedRow);
                    deleteExam(exam);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Bitte wählen Sie eine Klausur aus der Tabelle.",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            });
            
            JButton printButton = new JButton("Drucken");
            printButton.addActionListener(e -> {
                int selectedRow = examsTable.getSelectedRow();
                if (selectedRow >= 0) {
                    Exam exam = exams.get(selectedRow);
                    printExam(exam);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Bitte wählen Sie eine Klausur aus der Tabelle.",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            });
            
            JButton exportButton = new JButton("Exportieren");
            exportButton.addActionListener(e -> {
                int selectedRow = examsTable.getSelectedRow();
                if (selectedRow >= 0) {
                    Exam exam = exams.get(selectedRow);
                    exportExam(exam);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Bitte wählen Sie eine Klausur aus der Tabelle.",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            });
            
            toolbar.add(refreshButton);
            toolbar.add(deleteButton);
            toolbar.add(printButton);
            toolbar.add(exportButton);
            
            add(toolbar, BorderLayout.NORTH);
            
            // Tabelle für Klausuren
            String[] columnNames = {"Name", "Modul", "Datum erstellt", "Anzahl Aufgaben"};
            tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            examsTable = new JTable(tableModel);
            examsTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    updateExamPreview();
                }
            });
            
            JScrollPane scrollPane = new JScrollPane(examsTable);
            scrollPane.setPreferredSize(new Dimension(500, 200));
            
            add(scrollPane, BorderLayout.CENTER);
            
            // Vorschau-Panel
            JPanel previewPanel = new JPanel(new BorderLayout());
            previewPanel.setBorder(BorderFactory.createTitledBorder("Vorschau"));
            
            previewArea = new JTextArea();
            previewArea.setEditable(false);
            previewPanel.add(new JScrollPane(previewArea), BorderLayout.CENTER);
            
            add(previewPanel, BorderLayout.SOUTH);
            
            // Klausuren beim Start laden
            loadExams();
        }
        
        private void loadExams() {
            try {
                exams = examService.findAll();
                updateExamTable();
                statusLabel.setText("Klausuren geladen: " + exams.size());
            } catch (SQLException ex) {
                logger.error("Error loading exams", ex);
                JOptionPane.showMessageDialog(this,
                    "Fehler beim Laden der Klausuren: " + ex.getMessage(),
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        
        private void updateExamTable() {
            tableModel.setRowCount(0);
            
            for (Exam exam : exams) {
                Object[] row = new Object[4];
                row[0] = exam.getName();
                row[1] = exam.getModule() != null ? exam.getModule().getName() : "-";
                row[2] = exam.getDateCreated().toString();
                row[3] = exam.getQuestions().size();
                
                tableModel.addRow(row);
            }
            
            // Vorschau zurücksetzen
            previewArea.setText("");
        }
        
        private void updateExamPreview() {
            int selectedRow = examsTable.getSelectedRow();
            if (selectedRow >= 0 && selectedRow < exams.size()) {
                Exam exam = exams.get(selectedRow);
                String preview = examService.exportExam(exam);
                previewArea.setText(preview);
            } else {
                previewArea.setText("");
            }
        }
        
        private void deleteExam(Exam exam) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Möchten Sie die Klausur '" + exam.getName() + "' wirklich löschen?",
                "Löschen bestätigen",
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    examService.delete(exam.getId());
                    loadExams();
                    statusLabel.setText("Klausur gelöscht: " + exam.getName());
                } catch (SQLException ex) {
                    logger.error("Error deleting exam", ex);
                    JOptionPane.showMessageDialog(this,
                        "Fehler beim Löschen der Klausur: " + ex.getMessage(),
                        "Fehler",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        
        private void printExam(Exam exam) {
            // Hier würde die Druckfunktionalität implementiert werden
            JOptionPane.showMessageDialog(this,
                "Druckfunktion noch nicht implementiert.",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
        }
        
        private void exportExam(Exam exam) {
            // Hier würde die Exportfunktionalität implementiert werden
            JOptionPane.showMessageDialog(this,
                "Exportfunktion noch nicht implementiert.",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
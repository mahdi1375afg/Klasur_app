package com.klasurapp.model.enums;

public enum ClosedQuestionType {
    SINGLE_CHOICE("Single-Choice Fragen", "Der Prüfling wählt eine einzige Antwort aus mehreren Optionen aus"),
    MULTIPLE_CHOICE("Multiple-Choice Fragen", "Der Prüfling kann eine oder mehrere Antworten aus mehreren Optionen auswählen"),
    TRUE_FALSE("Wahr/Falsch Fragen", "Der Prüfling gibt an, ob eine Aussage wahr oder falsch ist"),
    GAP_TEXT("Lückentextaufgaben", "Der Prüfling ergänzt einen Text mit vorgegebenen Wörtern (in der Regel eine Option)"),
    MATCHING("Zuordnungsaufgaben", "Der Prüfling ordnet Begriffe einander zu, wobei mehrere Zuordnungen möglich sind"),
    RANKING("Ranking-Aufgaben", "Der Prüfling bringt Elemente in eine festgelegte Reihenfolge");

    private final String displayName;
    private final String description;

    ClosedQuestionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
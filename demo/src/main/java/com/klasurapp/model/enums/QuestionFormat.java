package com.klasurapp.model.enums;

public enum QuestionFormat {
    OPEN("Offene Aufgabe", "Förderung von kritischem Denken und Vertiefung der Analysefähigkeit"),
    CLOSED("Geschlossene Aufgabe", "Überprüfung von Faktenwissen und sofortige Feedbackmöglichkeiten");

    private final String displayName;
    private final String description;

    QuestionFormat(String displayName, String description) {
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
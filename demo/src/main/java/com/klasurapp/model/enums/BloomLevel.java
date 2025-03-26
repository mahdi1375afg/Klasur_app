package com.klasurapp.model.enums;

public enum BloomLevel {
    REMEMBER(1, "Erinnern", "Gelerntes auswendig wiedergeben, Ausführen von Routinen"),
    UNDERSTAND(2, "Verstehen", "Gelerntes erklären, reformulieren oder paraphrasieren"),
    APPLY(3, "Anwenden", "Gelerntes in neuem Kontext / neuer Situation anwenden"),
    ANALYZE(4, "Analysieren", "Gelerntes in Bestandteile zerlegen, Strukturen erläutern"),
    EVALUATE(5, "Bewerten", "Gelerntes nach (meist selbst) gewählten Kriterien kritisch beurteilen"),
    CREATE(6, "Erschaffen", "Gelerntes neu zusammenfügen oder neue Inhalte generieren");

    private final int level;
    private final String name;
    private final String description;

    BloomLevel(int level, String name, String description) {
        this.level = level;
        this.name = name;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static BloomLevel fromLevel(int level) {
        for (BloomLevel bl : BloomLevel.values()) {
            if (bl.getLevel() == level) {
                return bl;
            }
        }
        throw new IllegalArgumentException("Invalid Bloom level: " + level);
    }
}
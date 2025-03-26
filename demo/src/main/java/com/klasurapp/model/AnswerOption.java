package com.klasurapp.model;

import java.util.Objects;

public class AnswerOption {
    private Integer id;
    private Question question;
    private String optionText;
    private boolean correct;
    private Integer optionOrder;

    public AnswerOption() {
    }

    public AnswerOption(Integer id, Question question, String optionText, boolean correct, Integer optionOrder) {
        this.id = id;
        this.question = question;
        this.optionText = optionText;
        this.correct = correct;
        this.optionOrder = optionOrder;
    }

    // Getter und Setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public Integer getOptionOrder() {
        return optionOrder;
    }

    public void setOptionOrder(Integer optionOrder) {
        this.optionOrder = optionOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnswerOption that = (AnswerOption) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AnswerOption{" +
                "id=" + id +
                ", optionText='" + optionText + '\'' +
                ", correct=" + correct +
                '}';
    }
}
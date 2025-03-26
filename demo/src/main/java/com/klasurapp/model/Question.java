package com.klasurapp.model;

import com.klasurapp.model.enums.BloomLevel;
import com.klasurapp.model.enums.ClosedQuestionType;
import com.klasurapp.model.enums.QuestionFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Question {
    private Integer id;
    private String name;
    private String questionText;
    private int estimatedTimeMinutes;
    private Module module;
    private BloomLevel bloomLevel;
    private QuestionFormat questionFormat;
    private ClosedQuestionType closedQuestionType;
    private String solution;
    private List<AnswerOption> answerOptions;

    public Question() {
        this.answerOptions = new ArrayList<>();
    }

    public Question(Integer id, String name, String questionText, int estimatedTimeMinutes, 
                    Module module, BloomLevel bloomLevel, QuestionFormat questionFormat,
                    ClosedQuestionType closedQuestionType, String solution) {
        this.id = id;
        this.name = name;
        this.questionText = questionText;
        this.estimatedTimeMinutes = estimatedTimeMinutes;
        this.module = module;
        this.bloomLevel = bloomLevel;
        this.questionFormat = questionFormat;
        this.closedQuestionType = closedQuestionType;
        this.solution = solution;
        this.answerOptions = new ArrayList<>();
    }

    // Getter und Setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public int getEstimatedTimeMinutes() {
        return estimatedTimeMinutes;
    }

    public void setEstimatedTimeMinutes(int estimatedTimeMinutes) {
        this.estimatedTimeMinutes = estimatedTimeMinutes;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public BloomLevel getBloomLevel() {
        return bloomLevel;
    }

    public void setBloomLevel(BloomLevel bloomLevel) {
        this.bloomLevel = bloomLevel;
    }

    public QuestionFormat getQuestionFormat() {
        return questionFormat;
    }

    public void setQuestionFormat(QuestionFormat questionFormat) {
        this.questionFormat = questionFormat;
    }

    public ClosedQuestionType getClosedQuestionType() {
        return closedQuestionType;
    }

    public void setClosedQuestionType(ClosedQuestionType closedQuestionType) {
        this.closedQuestionType = closedQuestionType;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public List<AnswerOption> getAnswerOptions() {
        return answerOptions;
    }

    public void setAnswerOptions(List<AnswerOption> answerOptions) {
        this.answerOptions = answerOptions;
    }

    public void addAnswerOption(AnswerOption answerOption) {
        this.answerOptions.add(answerOption);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Question question = (Question) o;
        return Objects.equals(id, question.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", questionFormat=" + questionFormat +
                '}';
    }
}
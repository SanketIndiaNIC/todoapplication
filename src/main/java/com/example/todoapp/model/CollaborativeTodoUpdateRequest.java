package com.example.todoapp.model;

import java.util.List;

public class CollaborativeTodoUpdateRequest {
    private String title;
    private String description;
    private List<Number> collaboratorIds;

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Number> getCollaboratorIds() {
        return collaboratorIds;
    }

    public void setCollaboratorIds(List<Number> collaboratorIds) {
        this.collaboratorIds = collaboratorIds;
    }
}

package com.example.todoapp.model;

import java.util.List;

public class CollaborativeTodoRequest {
    private String title;
    private String description;
    private List<Integer> collaboratorIds;

    // Getters and setters
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

    public List<Integer> getCollaboratorIds() {
        return collaboratorIds;
    }

    public void setCollaboratorIds(List<Integer> collaboratorIds) {
        this.collaboratorIds = collaboratorIds;
    }
}

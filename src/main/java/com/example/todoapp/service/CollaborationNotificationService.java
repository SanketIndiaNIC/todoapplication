package com.example.todoapp.service;

import com.example.todoapp.model.CollaborativeTodo;
import com.example.todoapp.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CollaborationNotificationService {

    @Autowired
    private EmailService emailService;

    public void sendCollaborationNotification(CollaborativeTodo todo, User collaborator) {
        emailService.sendCollaborationNotification(todo, collaborator);
    }

    public void notifyCollaborators(CollaborativeTodo todo, String message) {
        emailService.sendCollaborativeTaskUpdate(todo, "Update", message);
    }

    public void notifyCollaboratorsOfDeletion(CollaborativeTodo todo) {
        String deletionMessage = String.format(
            "The task '%s' has been deleted by %s. This task is no longer available.",
            todo.getTitle(),
            todo.getCreator().getUsername()
        );
        emailService.sendCollaborativeTaskUpdate(todo, "Deletion", deletionMessage);
    }

    public void sendDeletionNotification(CollaborativeTodo todo) {
        notifyCollaboratorsOfDeletion(todo);
    }

    public void notifyCollaborators(CollaborativeTodo todo) {
        String updateMessage = String.format(
            "The task '%s' has been updated by %s.",
            todo.getTitle(),
            todo.getCreator().getUsername()
        );
        notifyCollaborators(todo, updateMessage);
    }
}

package com.example.todoapp.service;

import com.example.todoapp.model.CollaborativeTodo;
import com.example.todoapp.model.User;
import com.example.todoapp.repository.CollaborativeTodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CollaborativeReminderScheduler {

    private final CollaborativeTodoRepository collaborativeTodoRepository;
    private final EmailService emailService;
    private final CollaborationNotificationService notificationService;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    @Scheduled(fixedRate = 60000) // Checks every minute
    @Transactional
    public void checkCollaborativeReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<CollaborativeTodo> dueTasks = collaborativeTodoRepository.findByReminderDateTimeBeforeAndReminderSentFalseAndCompletedFalse(now);

        for (CollaborativeTodo task : dueTasks) {
            // Create reminder message
            String reminderMessage = String.format(
                "Task: %s\nDue: %s\nStatus: %s\n%s",
                task.getTitle(),
                task.getReminderDateTime().format(formatter),
                task.isCompleted() ? "Completed" : "Pending",
                task.getReminderMessage() != null ? "\nNote: " + task.getReminderMessage() : ""
            );

            // Notify all participants about the reminder
            notificationService.notifyCollaborators(task, reminderMessage);

            // Mark reminder as sent
            task.setReminderSent(true);
            collaborativeTodoRepository.save(task);
        }
    }
}

package com.example.todoapp.service;

import com.example.todoapp.model.Todo;
import com.example.todoapp.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class ReminderScheduler {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private EmailService emailService;

    @Scheduled(fixedRate = 60000) // Check every minute
    public void checkReminders() {
        LocalDateTime now = LocalDateTime.now();
        todoRepository.findAll().forEach(todo -> {
            if (todo.getReminderDateTime() != null && !todo.isCompleted()) {
                boolean shouldSendReminder = false;
                LocalDateTime lastSent = todo.getLastReminderSent();
                Todo.ReminderFrequency frequency = todo.getReminderFrequency();
                
                // Default to ONCE if frequency is null
                if (frequency == null) {
                    frequency = Todo.ReminderFrequency.ONCE;
                }
                
                switch (frequency) {
                    case ONCE:
                        shouldSendReminder = !todo.isReminderSent() && 
                            todo.getReminderDateTime().isBefore(now);
                        break;
                    case DAILY:
                        shouldSendReminder = (lastSent == null || 
                            ChronoUnit.DAYS.between(lastSent, now) >= 1) &&
                            isTimeToSend(todo.getReminderDateTime(), now);
                        break;
                    case WEEKLY:
                        shouldSendReminder = (lastSent == null || 
                            ChronoUnit.WEEKS.between(lastSent, now) >= 1) &&
                            isTimeToSend(todo.getReminderDateTime(), now);
                        break;
                    case MONTHLY:
                        shouldSendReminder = (lastSent == null || 
                            ChronoUnit.MONTHS.between(lastSent, now) >= 1) &&
                            isTimeToSend(todo.getReminderDateTime(), now);
                        break;
                    case YEARLY:
                        shouldSendReminder = (lastSent == null || 
                            ChronoUnit.YEARS.between(lastSent, now) >= 1) &&
                            isTimeToSend(todo.getReminderDateTime(), now);
                        break;
                }

                if (shouldSendReminder) {
                    emailService.sendReminderEmail(todo);
                    todo.setLastReminderSent(now);
                    if (frequency == Todo.ReminderFrequency.ONCE) {
                        todo.setReminderSent(true);
                    }
                    todoRepository.save(todo);
                }
            }
        });
    }

    private boolean isTimeToSend(LocalDateTime reminderTime, LocalDateTime now) {
        return reminderTime.getHour() == now.getHour() && 
               reminderTime.getMinute() == now.getMinute();
    }
}

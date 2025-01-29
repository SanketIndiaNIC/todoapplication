package com.example.todoapp.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ReminderRequest {
    private LocalDateTime reminderDateTime;
    private Todo.ReminderFrequency frequency = Todo.ReminderFrequency.ONCE;
}

package com.example.todoapp.repository;

import com.example.todoapp.model.Todo;
import com.example.todoapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByUserOrderByCreatedAtDesc(User user);
    
    List<Todo> findByUserId(Long userId);
    
    List<Todo> findByReminderDateTimeBeforeAndReminderSentFalseAndCompletedFalse(LocalDateTime dateTime);
}

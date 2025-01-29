package com.example.todoapp.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Entity
@Table(name = "todos")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Todo {
    public enum ReminderFrequency {
        ONCE,
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "completed")
    private boolean completed = false;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column
    private LocalDateTime completedAt;

    @Column(name = "reminder_date_time")
    private LocalDateTime reminderDateTime;
    
    @Column(name = "reminder_frequency")
    @Enumerated(EnumType.STRING)
    private ReminderFrequency reminderFrequency = ReminderFrequency.ONCE;
    
    @Column(name = "last_reminder_sent")
    private LocalDateTime lastReminderSent;
    
    @Column(name = "reminder_sent", nullable = false)
    private boolean reminderSent = false;

    @Column(name = "user_email")
    private String userEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "todos", "password"})
    private User user;

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}

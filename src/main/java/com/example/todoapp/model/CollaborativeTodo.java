package com.example.todoapp.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "collaborative_todos")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class CollaborativeTodo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "collaborative_todo_users",
        joinColumns = @JoinColumn(name = "collaborative_todo_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> collaborators = new HashSet<>();
    
    @Column(name = "completed")
    private boolean completed = false;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "reminder_date_time")
    private LocalDateTime reminderDateTime;

    @Column(name = "reminder_sent")
    private Boolean reminderSent = false;

    @Column(name = "reminder_message")
    private String reminderMessage;
}

package com.example.todoapp.repository;

import com.example.todoapp.model.CollaborativeTodo;
import com.example.todoapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CollaborativeTodoRepository extends JpaRepository<CollaborativeTodo, Long> {
    
    @Query("SELECT ct FROM CollaborativeTodo ct WHERE ct.creator = :creator OR :collaborator MEMBER OF ct.collaborators ORDER BY ct.createdAt DESC")
    List<CollaborativeTodo> findAllByCreatorOrCollaborators(@Param("creator") User creator, @Param("collaborator") User collaborator);
    
    @Query("SELECT ct FROM CollaborativeTodo ct WHERE (ct.creator = :creator OR :collaborator MEMBER OF ct.collaborators) AND ct.completed = false ORDER BY ct.createdAt DESC")
    List<CollaborativeTodo> findAllByCreatorOrCollaboratorsAndCompletedIsFalse(@Param("creator") User creator, @Param("collaborator") User collaborator);
    
    @Query("SELECT ct FROM CollaborativeTodo ct WHERE ct.creator = :user OR :user MEMBER OF ct.collaborators ORDER BY ct.createdAt DESC")
    List<CollaborativeTodo> findAllUserCollaborativeTodos(@Param("user") User user);
    
    List<CollaborativeTodo> findByCreatorOrderByCreatedAtDesc(User creator);
    
    @Query("SELECT ct FROM CollaborativeTodo ct JOIN ct.collaborators u WHERE u = :user ORDER BY ct.createdAt DESC")
    List<CollaborativeTodo> findByCollaboratorOrderByCreatedAtDesc(@Param("user") User user);
    
    List<CollaborativeTodo> findByCollaboratorsContaining(User user);
    
    @Query("SELECT DISTINCT ct FROM CollaborativeTodo ct LEFT JOIN FETCH ct.collaborators LEFT JOIN FETCH ct.creator WHERE ct.reminderDateTime < :dateTime AND ct.reminderSent = false AND ct.completed = false")
    List<CollaborativeTodo> findByReminderDateTimeBeforeAndReminderSentFalseAndCompletedFalse(@Param("dateTime") LocalDateTime dateTime);
}

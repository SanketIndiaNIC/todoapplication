package com.example.todoapp.controller;

import com.example.todoapp.model.CollaborativeTodo;
import com.example.todoapp.model.CollaborativeTodoRequest;
import com.example.todoapp.model.CollaborativeTodoUpdateRequest;
import com.example.todoapp.model.ErrorResponse;
import com.example.todoapp.model.User;
import com.example.todoapp.repository.CollaborativeTodoRepository;
import com.example.todoapp.repository.UserRepository;
import com.example.todoapp.service.CollaborationNotificationService;
import com.example.todoapp.service.CollaborativeTodoService;
import com.example.todoapp.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/collaborative-todos")
public class CollaborativeTodoController {
    
    @Autowired
    private CollaborativeTodoRepository collaborativeTodoRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CollaborativeTodoService collaborativeTodoService;
    
    @Autowired
    private CollaborationNotificationService notificationService;
    
    @GetMapping
    public ResponseEntity<List<CollaborativeTodo>> getUserCollaborativeTodos(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
        return ResponseEntity.ok(collaborativeTodoService.findAllByCreatorOrCollaborator(user));
    }
    
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAvailableUsers(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("User not found"));
            
        List<User> users = userRepository.findAll().stream()
            .filter(user -> !user.equals(currentUser))
            .collect(Collectors.toList());

        List<Map<String, Object>> userDtos = users.stream()
            .map(user -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", user.getId());
                dto.put("username", user.getUsername());
                dto.put("email", user.getEmail());
                return dto;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(userDtos);
    }
    
    @PostMapping
    public ResponseEntity<CollaborativeTodo> createCollaborativeTodo(@RequestBody Map<String, Object> request) {
        try {
            // Get authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.badRequest().build();
            }
            
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

            String title = (String) request.get("title");
            String description = (String) request.get("description");
            @SuppressWarnings("unchecked")
            List<Number> collaboratorIds = (List<Number>) request.get("collaboratorIds");
            
            if (title == null || description == null || collaboratorIds == null || collaboratorIds.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            CollaborativeTodo todo = new CollaborativeTodo();
            todo.setTitle(title);
            todo.setDescription(description);
            todo.setCreator(currentUser);
            todo.setCreatedAt(LocalDateTime.now());
            
            // Convert collaborator IDs to User objects
            Set<User> collaborators = collaboratorIds.stream()
                .map(id -> userRepository.findById(id.longValue()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
            
            if (collaborators.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            todo.setCollaborators(collaborators);
            
            CollaborativeTodo savedTodo = collaborativeTodoRepository.save(todo);
            
            // Send notifications to collaborators
            collaborators.forEach(collaborator -> 
                notificationService.sendCollaborationNotification(savedTodo, collaborator));
            
            return ResponseEntity.ok(savedTodo);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/toggle")
    public ResponseEntity<CollaborativeTodo> toggleCollaborativeTodo(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("User not found"));
            
        return collaborativeTodoRepository.findById(id)
            .filter(todo -> todo.getCreator().equals(user) || todo.getCollaborators().contains(user))
            .map(todo -> {
                todo.setCompleted(!todo.isCompleted());
                if (todo.isCompleted()) {
                    todo.setCompletedAt(LocalDateTime.now());
                } else {
                    todo.setCompletedAt(null);
                }
                return ResponseEntity.ok(collaborativeTodoRepository.save(todo));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/pending")
    public ResponseEntity<List<CollaborativeTodo>> getPendingCollaborativeTodos(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("User not found"));
            
        return ResponseEntity.ok(collaborativeTodoRepository.findAllByCreatorOrCollaboratorsAndCompletedIsFalse(user, user));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CollaborativeTodo> getCollaborativeTodoById(@PathVariable Long id) {
        return collaborativeTodoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCollaborativeTodo(@PathVariable Long id, @RequestBody CollaborativeTodoUpdateRequest request) {
        try {
            // Get current user
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            
            CollaborativeTodo todo = collaborativeTodoService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found"));
            
            // Check if current user is the creator
            if (!collaborativeTodoService.isCreator(todo, currentUsername)) {
                return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Only the creator can edit this task"));
            }

            // Get current collaborators for comparison
            Set<User> currentCollaborators = new HashSet<>(todo.getCollaborators());
            
            // Get new collaborators set
            Set<User> newCollaborators = collaborativeTodoService.getCollaboratorsFromIds(request.getCollaboratorIds());
            
            // Update the todo
            CollaborativeTodo updatedTodo = collaborativeTodoService.update(
                todo, 
                request.getTitle(), 
                request.getDescription(), 
                newCollaborators
            );
            
            // Notify new collaborators
            newCollaborators.stream()
                .filter(collaborator -> !currentCollaborators.contains(collaborator))
                .forEach(newCollaborator -> 
                    notificationService.sendCollaborationNotification(updatedTodo, newCollaborator));
            
            return ResponseEntity.ok(updatedTodo);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to update todo"));
        }
    }
    
    @PostMapping("/{id}/reminder")
    public ResponseEntity<?> setReminder(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            String reminderDateTime = (String) request.get("reminderDateTime");
            String reminderMessage = (String) request.get("reminderMessage");
            
            CollaborativeTodo todo = collaborativeTodoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found"));
            
            todo.setReminderDateTime(LocalDateTime.parse(reminderDateTime));
            todo.setReminderMessage(reminderMessage);
            todo.setReminderSent(false);
            
            CollaborativeTodo updatedTodo = collaborativeTodoRepository.save(todo);
            
            // Notify all collaborators about the reminder
            String notificationMessage = String.format("Reminder set for task '%s'", todo.getTitle());
            notificationService.notifyCollaborators(todo, notificationMessage);
            
            return ResponseEntity.ok(updatedTodo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to set reminder: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}/reminder")
    public ResponseEntity<?> removeReminder(@PathVariable Long id) {
        try {
            CollaborativeTodo todo = collaborativeTodoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found"));
            
            todo.setReminderDateTime(null);
            todo.setReminderMessage(null);
            todo.setReminderSent(false);
            
            CollaborativeTodo updatedTodo = collaborativeTodoRepository.save(todo);
            
            // Notify all collaborators about the reminder removal
            String notificationMessage = String.format("Reminder removed for task '%s'", todo.getTitle());
            notificationService.notifyCollaborators(todo, notificationMessage);
            
            return ResponseEntity.ok(updatedTodo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to remove reminder: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCollaborativeTodo(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
        CollaborativeTodo todo = collaborativeTodoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Todo not found"));
            
        // Check if the current user is the creator
        if (!todo.getCreator().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the creator can delete this task");
        }
        
        // Send notifications before deleting
        notificationService.notifyCollaboratorsOfDeletion(todo);
        
        // Delete the todo
        collaborativeTodoRepository.delete(todo);
        
        return ResponseEntity.ok().build();
    }
}

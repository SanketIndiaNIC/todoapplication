package com.example.todoapp.controller;

import com.example.todoapp.model.ReminderRequest;
import com.example.todoapp.model.Todo;
import com.example.todoapp.model.User;
import com.example.todoapp.repository.TodoRepository;
import com.example.todoapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class TodoController {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping({"/", "/todo", "/todo/"})
    public String index(Model model) {
        User currentUser = getCurrentUser();
        List<Todo> todos = todoRepository.findByUserOrderByCreatedAtDesc(currentUser);
        model.addAttribute("todos", todos);
        model.addAttribute("fullName", currentUser.getFullName());
        return "index";
    }

    @GetMapping("/todo")
    @ResponseBody
    public List<Map<String, Object>> getTodos(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Todo> todos = todoRepository.findByUserId(user.getId());
        return todos.stream().map(todo -> {
            Map<String, Object> todoMap = new HashMap<>();
            todoMap.put("id", todo.getId());
            todoMap.put("title", todo.getTitle());
            todoMap.put("description", todo.getDescription());
            todoMap.put("completed", todo.isCompleted());
            todoMap.put("createdAt", todo.getCreatedAt());
            todoMap.put("completedAt", todo.getCompletedAt());
            todoMap.put("reminderDateTime", todo.getReminderDateTime());
            todoMap.put("reminderSent", todo.isReminderSent());
            return todoMap;
        }).collect(Collectors.toList());
    }

    @PostMapping("/todo/add")
    @ResponseBody
    public ResponseEntity<?> addTodo(@RequestBody Map<String, String> payload) {
        try {
            String title = payload.get("title");
            String description = payload.get("description");
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Title is required");
            }

            Todo todo = new Todo();
            todo.setTitle(title.trim());
            todo.setDescription(description);
            todo.setUser(getCurrentUser());
            todo.setCompleted(false);
            todo.setCreatedAt(LocalDateTime.now());
            todo.setUpdatedAt(LocalDateTime.now());
            todoRepository.save(todo);

            return ResponseEntity.ok(todo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating todo: " + e.getMessage());
        }
    }

    @PostMapping("/todo/{id}/toggle")
    @ResponseBody
    public ResponseEntity<?> toggleTodo(@PathVariable("id") Long id) {
        try {
            Optional<Todo> todoOptional = todoRepository.findById(id);
            if (todoOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Todo not found");
            }

            Todo todo = todoOptional.get();
            User currentUser = getCurrentUser();

            if (!todo.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
            }

            // Toggle the status
            todo.setCompleted(!todo.isCompleted());
            todo.setUpdatedAt(LocalDateTime.now());
            if (todo.isCompleted()) {
                todo.setCompletedAt(LocalDateTime.now());
            } else {
                todo.setCompletedAt(null);
            }

            Todo savedTodo = todoRepository.save(todo);

            // Return a simple response with just the needed fields
            if(savedTodo.getCompletedAt() != null){
                return ResponseEntity.ok(Map.of(
                        "id", savedTodo.getId(),
                        "title", savedTodo.getTitle(),
                        "description", savedTodo.getDescription(),
                        "completed", savedTodo.isCompleted(),
                        "completedAt", savedTodo.getCompletedAt(),
                        "createdAt", savedTodo.getCreatedAt()
                ));
            }
            else{
                return ResponseEntity.ok(Map.of(
                        "id", savedTodo.getId(),
                        "title", savedTodo.getTitle(),
                        "description", savedTodo.getDescription(),
                        "completed", savedTodo.isCompleted(),
                        "createdAt", savedTodo.getCreatedAt()
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error toggling todo: " + e.getMessage());
        }
    }

    @PostMapping("/todo/{id}/reminder")
    @ResponseBody
    public ResponseEntity<?> setReminder(@PathVariable Long id, @RequestBody ReminderRequest request, Principal principal) {
        try {
            Todo todo = todoRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found"));
            
            User user = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            
            if (!todo.getUser().getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }

            todo.setReminderDateTime(request.getReminderDateTime());
            todo.setReminderFrequency(request.getFrequency());
            todo.setUserEmail(user.getEmail());
            todo.setReminderSent(false);
            todo.setLastReminderSent(null);
            Todo savedTodo = todoRepository.save(todo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedTodo.getId());
            response.put("title", savedTodo.getTitle());
            response.put("description", savedTodo.getDescription());
            response.put("reminderDateTime", savedTodo.getReminderDateTime());
            response.put("reminderFrequency", savedTodo.getReminderFrequency());
            response.put("message", "Reminder set successfully");
            
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to set reminder: " + e.getMessage());
        }
    }

    @PutMapping("/todo/{id}")
    @ResponseBody
    public ResponseEntity<?> editTodo(@PathVariable Long id, @RequestBody Map<String, String> payload, Principal principal) {
        try {
            Todo todo = todoRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found"));
            
            User user = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            
            if (!todo.getUser().getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }

            String newTitle = payload.get("title");
            String newDescription = payload.get("description");
            if (newTitle == null || newTitle.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title cannot be empty");
            }

            todo.setTitle(newTitle.trim());
            todo.setDescription(newDescription);
            Todo savedTodo = todoRepository.save(todo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedTodo.getId());
            response.put("title", savedTodo.getTitle());
            response.put("description", savedTodo.getDescription());
            response.put("completed", savedTodo.isCompleted());
            response.put("createdAt", savedTodo.getCreatedAt());
            response.put("completedAt", savedTodo.getCompletedAt());
            response.put("reminderDateTime", savedTodo.getReminderDateTime());
            response.put("reminderSent", savedTodo.isReminderSent());
            response.put("message", "Todo updated successfully");
            
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update todo: " + e.getMessage());
        }
    }

    @DeleteMapping("/todo/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteTodo(@PathVariable Long id) {
        try {
            Optional<Todo> todoOptional = todoRepository.findById(id);
            if (!todoOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Todo not found");
            }

            Todo todo = todoOptional.get();
            User currentUser = getCurrentUser();

            if (!todo.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
            }

            todoRepository.delete(todo);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting todo: " + e.getMessage());
        }
    }
}

package com.example.todoapp.service;

import com.example.todoapp.model.CollaborativeTodo;
import com.example.todoapp.model.User;
import com.example.todoapp.repository.CollaborativeTodoRepository;
import com.example.todoapp.repository.UserRepository;
import com.example.todoapp.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CollaborativeTodoService {

    @Autowired
    private CollaborativeTodoRepository collaborativeTodoRepository;

    @Autowired
    private UserRepository userRepository;

    public List<CollaborativeTodo> findAllByCreatorOrCollaborator(User user) {
        return collaborativeTodoRepository.findAllUserCollaborativeTodos(user);
    }

    public Optional<CollaborativeTodo> findById(Long id) {
        return collaborativeTodoRepository.findById(id);
    }

    public CollaborativeTodo save(CollaborativeTodo todo) {
        return collaborativeTodoRepository.save(todo);
    }

    public void delete(CollaborativeTodo todo) {
        collaborativeTodoRepository.delete(todo);
    }

    public CollaborativeTodo create(String title, String description, User creator, Set<User> collaborators) {
        CollaborativeTodo todo = new CollaborativeTodo();
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setCreator(creator);
        todo.setCollaborators(collaborators);
        return collaborativeTodoRepository.save(todo);
    }

    public CollaborativeTodo update(CollaborativeTodo todo, String title, String description, Set<User> collaborators) {
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setCollaborators(collaborators);
        return collaborativeTodoRepository.save(todo);
    }

    public boolean isCreator(CollaborativeTodo todo, String username) {
        return todo.getCreator().getUsername().equals(username);
    }

    public Set<User> getCollaboratorsFromIds(List<Number> collaboratorIds) {
        if (collaboratorIds == null) {
            return Set.of();
        }
        return collaboratorIds.stream()
                .map(id -> userRepository.findById(id.longValue())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id)))
                .collect(Collectors.toSet());
    }
}

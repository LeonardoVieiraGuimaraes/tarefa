package com.example.tarefa.controller;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.tarefa.dto.CreateTaskDto;
import com.example.tarefa.dto.TaskDto;
import com.example.tarefa.dto.TaskItemDto;
import com.example.tarefa.model.Role;
import com.example.tarefa.model.Task;
import com.example.tarefa.repository.TaskRepository;
import com.example.tarefa.repository.UserRepository;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskController(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/list")
    public ResponseEntity<TaskDto> feed(@RequestParam(value = "page", defaultValue = "0") int page,
                                        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        var tasks = taskRepository.findAll(
                PageRequest.of(page, pageSize, Sort.Direction.DESC, "creationTimestamp"))
                .map(task -> new TaskItemDto(
                        task.getId(),
                        task.getDescricao(),
                        task.getUser().getUsername(),
                        task.isConcluida())
                );

        return ResponseEntity.ok(new TaskDto(
            tasks.getContent(), page, pageSize, tasks.getTotalPages(), tasks.getTotalElements()));
    }


    @PostMapping
    public ResponseEntity<Void> createTweet(@RequestBody CreateTaskDto dto,
                                            JwtAuthenticationToken token) {
        var user = userRepository.findById(UUID.fromString(token.getName()));

        var task = new Task();
        task.setUser(user.get());
        
        task.setDescricao(dto.descricao());
        // task.setConcluida(dto.concluida());

        taskRepository.save(task);

        return ResponseEntity.ok().build();
        
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTweet(@PathVariable("id") Long taskId,
                                            JwtAuthenticationToken token) {
        var user = userRepository.findById(UUID.fromString(token.getName()));
        var task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var isAdmin = user.get().getRoles()
                .stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        if (isAdmin || task.getUser().getId().equals(UUID.fromString(token.getName()))) {
            taskRepository.deleteById(taskId);

        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }


        return ResponseEntity.ok().build();
    }

}
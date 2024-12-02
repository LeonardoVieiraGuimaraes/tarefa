package com.example.tarefa.dto;

public record TaskItemDto(long id, String descricao, String username, Boolean concluida) {
}
package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewItemRequest {
    @NotNull
    @NotEmpty
    String name;
    @NotNull
    @NotEmpty
    String description;
    @NotNull
    Boolean available;
    Long requestId;
}
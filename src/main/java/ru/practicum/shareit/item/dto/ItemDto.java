package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.validation.Marker;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemDto {

    private Long id;
    @NotBlank(groups = {Marker.CreateValidation.class}, message = "Имя не может быть пустым")
    private String name;
    @NotBlank(groups = {Marker.CreateValidation.class}, message = "Описание не может быть пустым")
    private String description;
    @NotNull(groups = {Marker.CreateValidation.class}, message = "Статус не может быть пустым")
    private Boolean available;

}

package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.validation.Marker;

@Data
@AllArgsConstructor
public class UserDto {

    private Long id;
    @NotBlank(groups = Marker.CreateValidation.class, message = "Имя не может быть пустым")
    String name;
    @Email(groups = {Marker.CreateValidation.class, Marker.UpdateValidation.class},
            message = "Email не может быть пустым")
    @NotBlank(groups = Marker.CreateValidation.class, message = "Некорректный формат email")
    String email;

}

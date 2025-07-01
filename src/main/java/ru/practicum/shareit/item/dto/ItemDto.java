package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.validation.Marker;

import java.util.List;

@Data
@Builder
public class ItemDto {
    private Long id;
    @NotBlank(groups = {Marker.CreateValidation.class}, message = "Имя не может быть пустым")
    private String name;
    @NotBlank(groups = {Marker.CreateValidation.class}, message = "Описание не может быть пустым")
    private String description;
    @NotNull(groups = {Marker.CreateValidation.class}, message = "Статус не может быть пустым")
    private Boolean available;
    private List<CommentDto> comments;
    private BookingDto lastBooking;
    private BookingDto nextBooking;

}

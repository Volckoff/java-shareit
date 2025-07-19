package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingRequestDto {
    @NotNull(message = "Id вещи должно быть указано")
    private Long itemId;

    @NotNull(message = "Дата начала должна быть указана")
    private LocalDateTime start;

    @NotNull(message = "Дата конца должна быть указана")
    private LocalDateTime end;

}

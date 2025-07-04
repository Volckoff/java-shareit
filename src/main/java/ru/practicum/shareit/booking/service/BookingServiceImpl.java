package ru.practicum.shareit.booking.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    private User checkUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", userId);
                    return new NotFoundException(String.format("Пользователь с id = %d не найден", userId));
                });
    }

    private Item checkItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Предмет с id {} не найден", itemId);
                    return new NotFoundException(String.format("Предмет с id = %d не найден", itemId));
                });
    }

    @Override
    public BookingDto addBooking(BookingRequestDto bookingRequest, Long userId) {
        log.info("Попытка бронирования: {} от пользователя с id {}", bookingRequest, userId);
        Long itemId = bookingRequest.getItemId();
        Item item = checkItem(itemId);
        User owner = item.getOwner();
        User booker = checkUser(userId);
        if (owner.equals(booker)) {
            throw new ValidationException("Владелец не может бронировать свою вещь ");
        }
        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }
        LocalDateTime start = bookingRequest.getStart();
        LocalDateTime end = bookingRequest.getEnd();
        if (start.isAfter(end) || start.isEqual(end) || start.isBefore(LocalDateTime.now())
                || end.isBefore(LocalDateTime.now())) {
            throw new ValidationException(String.format("Некорректные даты бронирования. " +
                    "Начало: {} Конец: {}", start, end));
        }
        Booking booking = bookingMapper.toBooking(bookingRequest);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);
        Booking createdBooking = bookingRepository.save(booking);
        return bookingMapper.toBookingDto(createdBooking);
    }

    private Booking checkBooking(long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Бронирование с id {} не найдено", bookingId);
                    return new NotFoundException(String.format("Бронирование с id = %d не найдено", bookingId));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getBookingById(Long bookingId, Long userId) {
        checkUser(userId);
        Booking booking = checkBooking(bookingId);
        User owner = booking.getItem().getOwner();
        User booker = booking.getBooker();

        if (!booker.getId().equals(userId) && !owner.getId().equals(userId)) {
            throw new ValidationException("Просмотр бронирования доступен только автору или владельцу вещи");
        }

        return bookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getUserBookings(Long userId, State state) {
        checkUser(userId);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> userBookings = switch (state) {
            case CURRENT -> bookingRepository.findCurrentBookingsByUser(userId);
            case PAST -> bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE -> bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.WAITING);
            case REJECTED -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED);
            default -> bookingRepository.findByBookerIdOrderByStartDesc(userId);
        };
        return userBookings.stream()
                .map(bookingMapper::toBookingDto)
                .toList();
    }

    @Override
    public BookingDto approveBooking(Long bookingId, Long ownerId, boolean approved) {
        Booking booking = checkBooking(bookingId);
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ValidationException("Подтверждать бронирование может только владелец вещи");
        }
        checkUser(ownerId);
        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        Booking approvedBooking = bookingRepository.save(booking);
        log.info("Статус изменен: {}", approvedBooking);
        return bookingMapper.toBookingDto(approvedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getOwnerBookings(Long ownerId, State state) {
        checkUser(ownerId);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> ownerBookings = switch (state) {
            case CURRENT -> bookingRepository.findCurrentBookingsByOwner(ownerId);
            case PAST -> bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(ownerId, now);
            case FUTURE -> bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(ownerId, now);
            case WAITING -> bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(ownerId, Status.WAITING);
            case REJECTED -> bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(ownerId, Status.REJECTED);
            default -> bookingRepository.findByItemOwnerIdOrderByStartDesc(ownerId);
        };
        return ownerBookings.stream()
                .map(bookingMapper::toBookingDto)
                .toList();
    }
}

package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exceptions.CommentNotValidException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ItemServiceImp implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;
    private final BookingMapper bookingMapper;
    private final ItemRequestRepository itemRequestRepository;


    @Override
    @Transactional(readOnly = true)
    public List<ItemOwnerDto> getOwnerItems(Long userId) {

        log.info("Получение всех вещей владельца");
        userService.getUserById(userId);
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        if (items.isEmpty()) {
            throw new NotFoundException(String.format("Предметы владельца с id = %d не найдены", userId));
        }
        List<Long> itemIds = items.stream().map(Item::getId).toList();
        Map<Long, List<Comment>> commentsByItem = commentRepository.findAllByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));
        List<Booking> lastBookings = bookingRepository.findLastBookingsForItems(itemIds);
        List<Booking> nextBookings = bookingRepository.findNextBookingsForItems(itemIds);
        Map<Long, Booking> lastBookingsByItem = lastBookings.stream()
                .collect(Collectors.toMap(booking -> booking.getItem().getId(), booking -> booking));
        Map<Long, Booking> nextBookingsByItem = nextBookings.stream()
                .collect(Collectors.toMap(booking -> booking.getItem().getId(), booking -> booking));
        return items.stream()
                .map(item -> {
                    ItemOwnerDto itemOwnerDto = itemMapper.toItemOwnerDto(item);
                    Booking lastBooking = lastBookingsByItem.get(item.getId());
                    if (lastBooking != null) {
                        itemOwnerDto.setLastBooking(bookingMapper.toBookingDto(lastBooking));
                    }
                    Booking nextBooking = nextBookingsByItem.get(item.getId());
                    if (nextBooking != null) {
                        itemOwnerDto.setNextBooking(bookingMapper.toBookingDto(nextBooking));
                    }
                    List<Comment> comments = commentsByItem.getOrDefault(item.getId(), Collections.emptyList());
                    itemOwnerDto.setComments(comments.stream()
                            .map(commentMapper::toCommentDto)
                            .collect(Collectors.toList()));
                    return itemOwnerDto;
                }).toList();
    }

    private Item checkItem(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Предмет с id {} не найден", id);
                    return new NotFoundException(String.format("Предмет с id = %d не найден", id));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getItemById(Long id) {
        log.info("Получение вещи по id: {}", id);
        Item item = checkItem(id);
        ItemDto itemDto = itemMapper.toItemDto(item);
        List<Comment> comments = commentRepository.findAllByItemId(id);
        itemDto.setComments(comments.stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList()));

        return itemDto;
    }

    @Override
    public ItemDto addItem(NewItemRequest newItemRequest, Long userId) {

        UserDto ownerDto = userService.getUserById(userId);
        User owner = userMapper.toUser(ownerDto);
        Item item = new Item();
        item.setName(newItemRequest.getName());
        item.setDescription(newItemRequest.getDescription());
        item.setAvailable(newItemRequest.getAvailable());
        item.setOwner(owner);
        if (newItemRequest.getRequestId() != null) {
            ItemRequest request = itemRequestRepository.findById(newItemRequest.getRequestId())
                .orElseThrow(() -> new NotFoundException("Request not found"));
            item.setRequest(request);
        }
        Item createdItem = itemRepository.save(item);
        return itemMapper.toItemDto(createdItem);
    }


    @Override
    public ItemDto updateItem(ItemDto itemDto, Long userId, Long itemId) {
        log.info("Попытка обновления вещи с Id = {}", itemId);
        Item item = checkItem(itemId);
        UserDto ownerDto = userService.getUserById(userId);
        User owner = userMapper.toUser(ownerDto);
        item.setOwner(owner);
        Optional.ofNullable(itemDto.getName()).ifPresent(item::setName);
        Optional.ofNullable(itemDto.getDescription()).ifPresent(item::setDescription);
        Optional.ofNullable(itemDto.getAvailable()).ifPresent(item::setAvailable);
        Item updatedItem = itemRepository.save(item);
        log.info("Вещь с Id = {}, обновлена", itemId);
        return itemMapper.toItemDto(updatedItem);
    }

    @Override
    public void deleteItem(Long id) {
        log.info("Попытка удаления вещи с Id = {}", id);
        if (!itemRepository.existsById(id)) {
            throw new NotFoundException(String.format("Предмет с id = %d не найдена", id));
        }
        itemRepository.deleteById(id);
        log.info("Пользователь с id = {} удалён", id);
    }

    @Override
    public List<ItemDto> getItemsByText(String text) {
        log.info("Получение всех вещей по тексту");
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        return itemRepository.searchItemsByText(text).stream()
                .map(itemMapper::toItemDto)
                .toList();
    }

    @Override
    public CommentDto addComment(Long itemId, CommentDto commentDto, Long userId) {
        Item item = checkItem(itemId);
        UserDto authorDto = userService.getUserById(userId);
        User author = userMapper.toUser(authorDto);
        if (!commentRepository.existsApprovedPastBookingForUserAndItem(userId, itemId)) {
            throw new CommentNotValidException("Пользователь не может оставить отзыв на эту вещь");
        }
        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();
        Comment savedComment = commentRepository.save(comment);
        CommentDto result = commentMapper.toCommentDto(savedComment);
        result.setAuthorName(author.getName());
        return result;
    }

}

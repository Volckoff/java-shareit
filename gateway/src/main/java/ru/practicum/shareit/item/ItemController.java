package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDtoCreate;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;

@RestController
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> getOwnerItems(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Запрос на получение всех предметов владельца с id: {}", userId);
        return itemClient.getOwnerItems(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItemById(@PathVariable Long id, @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Запрос на получение предмета по id: {}", id);
        return itemClient.getItemById(id, userId);
    }

    @PostMapping
    public ResponseEntity<Object> createItem(@Valid @RequestBody NewItemRequest newItemRequest,
                                             @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Создание нового предмета: {}", newItemRequest.getName());
        return itemClient.createItem(newItemRequest, userId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateItem(@RequestBody ItemDto itemDto,
                                             @RequestHeader(USER_ID_HEADER) Long userId,
                                             @PathVariable Long id) {
        log.info("Обновление предмета с id: {}", id);
        return itemClient.updateItem(id, itemDto, userId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteItem(@PathVariable Long id) {
        log.info("Удаление предмета с id: {}", id);
        return itemClient.deleteItem(id);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItemsByText(@RequestParam String text) {
        log.info("Поиск предметов по тексту: '{}'", text);
        return itemClient.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@PathVariable Long itemId, @Valid @RequestBody CommentDtoCreate commentDto,
                                                @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Создание комментария для предмета с id: {}", itemId);
        return itemClient.createComment(itemId, commentDto, userId);
    }
}
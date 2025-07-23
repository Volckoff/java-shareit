package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public List<ItemOwnerDto> getOwnerItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getOwnerItems(userId);
    }

    @GetMapping("/{id}")
    public ItemDto getItemById(@PathVariable Long id) {
        return itemService.getItemById(id);
    }

    @PostMapping
    public ItemDto addItem(@RequestBody NewItemRequest newItemRequest,
                           @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.addItem(newItemRequest, userId);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@RequestBody ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable Long id) {
        return itemService.updateItem(itemDto, userId, id);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItemsByText(@RequestParam String text) {
        return itemService.getItemsByText(text);
    }

    @PostMapping("{itemId}/comment")
    public CommentDto addComment(@PathVariable Long itemId, @RequestBody CommentDto commentDto,
                                 @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.addComment(itemId, commentDto, userId);
    }
}

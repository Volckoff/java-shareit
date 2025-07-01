package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;

import java.util.List;

public interface ItemService {

    List<ItemOwnerDto> getOwnerItems(Long userId);

    ItemDto getItemById(Long id);

    ItemDto addItem(ItemDto item, Long userId);

    ItemDto updateItem(ItemDto item, Long userId, Long id);

    void deleteItem(Long id);

    List<ItemDto> getItemsByText(String text);

    CommentDto addComment(Long itemId, CommentDto comment, Long userId);

}

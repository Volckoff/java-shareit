package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {

    List<Item> getOwnerItems(Long userId);

    Item getItemById(Long id);

    Item addItem(Item item);

    Item updateItem(Item item);

    void deleteItem(Long id);

    List<Item> getItemsByText(String text);

}

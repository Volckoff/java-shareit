package ru.practicum.shareit.item.dao;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class ItemRepositoryImp implements ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();
    private Long id = 0L;

    @Override
    public List<Item> getOwnerItems(Long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .toList();
    }

    private void validateItem(Long id) {
        if (id == null) {
            log.error("Id вещи не указан");
            throw new ValidationException("Id должен быть указан");
        }
        if (!items.containsKey(id)) {
            log.error("Вещь с id = {} не найдена", id);
            throw new NotFoundException(String.format("Пользователь с id = %d не найден", id));
        }
    }

    @Override
    public Item getItemById(Long id) {
        validateItem(id);
        return items.get(id);
    }

    @Override
    public Item addItem(Item item) {
        item.setId(++id);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public void deleteItem(Long id) {
        validateItem(id);
        items.remove(id);
    }

    @Override
    public List<Item> getItemsByText(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return items.values().stream()
                .filter(item -> item.getAvailable() == true)
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .toList();
    }

    @Override
    public Item updateItem(Item updatedItem) {
        validateItem(updatedItem.getId());
        Item item = items.get(updatedItem.getId());
        if (updatedItem.getName() != null) {
            item.setName(updatedItem.getName());
        }
        if (updatedItem.getDescription() != null) {
            item.setDescription(updatedItem.getDescription());
        }
        if (updatedItem.getAvailable() != null) {
            item.setAvailable(updatedItem.getAvailable());
        }
        return item;
    }
}

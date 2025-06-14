package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.AccessDeniedException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;

import java.util.List;

import static ru.practicum.shareit.item.dto.ItemMapper.toItem;
import static ru.practicum.shareit.item.dto.ItemMapper.toItemDto;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImp implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public List<ItemDto> getOwnerItems(Long userId) {
        log.info("Получение всех вещей владельца");
        return itemRepository.getOwnerItems(userId).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public ItemDto getItemById(Long id) {
        log.info("Получение вещи по id: {}", id);
        Item item = itemRepository.getItemById(id);
        return toItemDto(item);
    }

    @Override
    public ItemDto addItem(ItemDto itemDto, Long userId) {
        log.info("Попытка создания вещи");
        User owner = userRepository.getUserById(userId);
        Item item = toItem(itemDto);
        item.setOwner(owner);
        Item createdItem = itemRepository.addItem(item);
        log.info("Вещь успешно создана");
        return toItemDto(createdItem);
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, Long userId, Long itemId) {
        log.info("Попытка обновления вещи с Id = {}", itemId);
        Item existingItem = itemRepository.getItemById(itemId);
        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Вы не можете редактировать чужую вещь");
        }
        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }
        Item updatedItem = itemRepository.updateItem(existingItem);
        log.info("Вещь с Id = {}, обновлена", itemId);
        return toItemDto(updatedItem);
    }

    @Override
    public void deleteItem(Long id) {
        log.info("Попытка удаления вещи с Id = {}", id);
        itemRepository.deleteItem(id);
        log.info("Пользователь с id = {} удалён", id);
    }

    @Override
    public List<ItemDto> getItemsByText(String text) {
        log.info("Получение всех вещей по тексту");
        return itemRepository.getItemsByText(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

}

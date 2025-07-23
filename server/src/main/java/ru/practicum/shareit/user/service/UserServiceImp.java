package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.DuplicateException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserServiceImp implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAll() {
        log.info("Получение всех пользователей");
        return userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .toList();
    }

    private User checkUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", id);
                    return new NotFoundException(String.format("Пользователь с id = %d не найден", id));
                });
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getUserById(Long id) {
        return userMapper.toUserDto(checkUser(id));
    }

    private void validateEmailUniqueness(String email, User currentUser) {
        Optional.ofNullable(email)
                .filter(e -> currentUser == null || !e.equals(currentUser.getEmail()))
                .ifPresent(e -> {
                    if (userRepository.existsByEmail(e)) {
                        log.warn("Дублирование email: {}", e);
                        throw new DuplicateException("Пользователь с такой почтой уже существует");
                    }
                });
    }

    @Override
    public UserDto addUser(UserDto userDto) {
        log.info("Попытка создания пользователя");
        validateEmailUniqueness(userDto.getEmail(), null);
        User user = userRepository.save(userMapper.toUser(userDto));
        log.info("Пользователь с id = {} создан", user.getId());
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(UserDto userDto, Long id) {
        User user = checkUser(id);
        validateEmailUniqueness(userDto.getEmail(), user);
        Optional.ofNullable(userDto.getEmail()).ifPresent(user::setEmail);
        Optional.ofNullable(userDto.getName()).ifPresent(user::setName);
        User updatedUser = userRepository.save(user);
        log.info("Пользователь с id = {} обновлён", updatedUser.getId());
        return userMapper.toUserDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Удаление пользователя с Id = {}", id);
        if (!userRepository.existsById(id)) {
            throw new NotFoundException(String.format("Пользователь с id = %d не найден", id));
        }
        userRepository.deleteById(id);
        log.info("Пользователь с id = {} удалён", id);
    }
}

package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static ru.practicum.shareit.user.dto.UserMapper.toUser;
import static ru.practicum.shareit.user.dto.UserMapper.toUserDto;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImp implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAll() {
        log.info("Получение всех пользователей");
        return userRepository.getAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public UserDto getUserById(Long id) {
        log.info("Получение юзера по с Id {}", id);
        User user = userRepository.getUserById(id);
        return toUserDto(user);
    }

    @Override
    public UserDto addUser(UserDto userDto) {
        log.info("Попытка создания пользователя");
        User user = toUser(userDto);
        User createdUser = userRepository.addUser(user);
        log.info("Пользователь с id = {} создан", user.getId());
        return toUserDto(createdUser);
    }

    @Override
    public UserDto updateUser(UserDto userDto, Long id) {
        log.info("Попытка обновления пользователя с id = {}", id);
        User existingUser = userRepository.getUserById(id);
        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            existingUser.setEmail(userDto.getEmail());
        }
        User updatedUser = userRepository.updateUser(existingUser);
        log.info("Пользователь с id = {} обновлён", updatedUser.getId());
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Удаление пользователя с Id = {}",id);
        userRepository.deleteUser(id);
        log.info("Пользователь с id = {} удалён", id);
    }
}

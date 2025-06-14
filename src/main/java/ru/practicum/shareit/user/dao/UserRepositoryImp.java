package ru.practicum.shareit.user.dao;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.DuplicateException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.User;

import java.util.*;

@Repository
@Slf4j
public class UserRepositoryImp implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private Long id = 0L;

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(Long id) {
        validateUser(id);
        return users.get(id);
    }

    public void validateUser(Long id) {
        if (id == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!users.containsKey(id)) {
            log.error("Пользователь с id = {} не найден", id);
            throw new NotFoundException(String.format("Пользователь с id = %d не найден", id));
        }
    }

    @Override
    public User addUser(User user) {
        validateEmail(user);
        user.setId(++id);
        users.put(user.getId(), user);
        return user;
    }

    public void validateEmail(User user) {
        Optional<User> duplicate = users.values().stream()
                .filter(newUser -> !newUser.getId().equals(user.getId()))
                .filter(newUser -> newUser.getEmail().equalsIgnoreCase(user.getEmail()))
                .findFirst();
        if (duplicate.isPresent()) {
            throw new DuplicateException("Почта должна быть уникальна");
        }
    }

    @Override
    public User updateUser(User updatedUser) {

        validateUser(updatedUser.getId());
        User user = users.get(updatedUser.getId());
        if (updatedUser.getName() != null) {
            user.setName(updatedUser.getName());
        }
        if (updatedUser.getEmail() != null) {
            validateEmail(updatedUser);
            user.setEmail(updatedUser.getEmail());
        }
        users.put(user.getId(), user);

        return user;
    }

    @Override
    public void deleteUser(Long id) {
        validateUser(id);
        users.remove(id);
    }

}

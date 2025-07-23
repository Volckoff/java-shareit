package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.exceptions.DuplicateException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapperImpl;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImp;

import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
@Import({UserServiceImp.class, UserMapperImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserServiceImplTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(new User(null, "Test User", "test@example.com"));
    }

    @Test
    void addUserShouldSaveAndReturnUser() {
        UserDto userDto = new UserDto(null, "New User", "new@example.com");

        UserDto result = userService.addUser(userDto);

        assertNotNull(result.getId());
        assertEquals(userDto.getName(), result.getName());
        assertEquals(userDto.getEmail(), result.getEmail());
        assertTrue(userRepository.existsById(result.getId()));
    }

    @Test
    void addUserShouldThrowWhenEmailExists() {
        UserDto userDto = new UserDto(null, "Duplicate Email", testUser.getEmail());

        assertThrows(DuplicateException.class, () -> userService.addUser(userDto));
    }


    @Test
    void updateUserShouldUpdateFields() {
        UserDto updateDto = new UserDto(null, "Updated Name", null);

        UserDto result = userService.updateUser(updateDto, testUser.getId());

        assertEquals("Updated Name", result.getName());
        assertEquals(testUser.getEmail(), result.getEmail());
    }

    @Test
    void updateUserShouldUpdateOnlyEmailWhenNameIsNull() {
        UserDto updateDto = new UserDto(null, null, "updated@example.com");

        UserDto result = userService.updateUser(updateDto, testUser.getId());

        assertEquals(testUser.getName(), result.getName());
        assertEquals("updated@example.com", result.getEmail());
    }

    @Test
    void updateUserShouldThrowWhenUserNotFound() {
        Long nonExistentUserId = 999L;
        UserDto updateDto = new UserDto(null, "Name", "email@example.com");

        assertThrows(NotFoundException.class, () ->
                userService.updateUser(updateDto, nonExistentUserId));
    }

    @Test
    void updateUserShouldThrowWhenEmailExistsForOtherUser() {
        User anotherUser = userRepository.save(new User(null, "Another", "another@example.com"));
        UserDto updateDto = new UserDto(null, "Name", anotherUser.getEmail());

        assertThrows(DuplicateException.class, () -> userService.updateUser(updateDto, testUser.getId()));
    }

    @Test
    void getUserByIdShouldReturnUser() {
        UserDto result = userService.getUserById(testUser.getId());

        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getEmail(), result.getEmail());
    }

    @Test
    void getUserByIdShouldThrowWhenUserNotFound() {
        Long nonExistentUserId = 999L;

        assertThrows(NotFoundException.class, () -> userService.getUserById(nonExistentUserId));
    }

    @Test
    void deleteUserShouldRemoveFromDb() {
        userService.deleteUser(testUser.getId());

        assertFalse(userRepository.existsById(testUser.getId()));
    }

    @Test
    void deleteUserShouldThrowWhenUserNotFound() {
        Long nonExistentUserId = 999L;

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.deleteUser(nonExistentUserId));

        assertTrue(exception.getMessage().contains(String.valueOf(nonExistentUserId)));
    }

    @Test
    void findAllShouldReturnAllUsers() {
        userRepository.save(new User(null, "User 1", "user1@example.com"));
        userRepository.save(new User(null, "User 2", "user2@example.com"));

        List<UserDto> result = userService.getAll();

        assertEquals(3, result.size()); // Includes the user from @BeforeEach
        assertTrue(result.stream().anyMatch(u -> u.getName().equals("Test User")));
        assertTrue(result.stream().anyMatch(u -> u.getName().equals("User 1")));
        assertTrue(result.stream().anyMatch(u -> u.getName().equals("User 2")));
    }

    @Test
    void findAllShouldReturnEmptyListWhenNoUsers() {
        userRepository.deleteAll();

        List<UserDto> result = userService.getAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void createUserShouldHandleEmptyName() {
        UserDto userDto = new UserDto(null, "", "empty@example.com");

        UserDto result = userService.addUser(userDto);

        assertEquals("", result.getName());
        assertNotNull(result.getId());
    }

    @Test
    void updateUserShouldHandleEmptyName() {
        UserDto updateDto = new UserDto(null, "", null);

        UserDto result = userService.updateUser(updateDto, testUser.getId());

        assertEquals("", result.getName());
        assertEquals(testUser.getEmail(), result.getEmail());
    }
}
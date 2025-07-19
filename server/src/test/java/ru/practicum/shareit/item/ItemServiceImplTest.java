package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingMapperImpl;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exceptions.CommentNotValidException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.ItemServiceImp;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserMapperImpl;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImp;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({ItemServiceImp.class, UserServiceImp.class, ItemMapperImpl.class, UserMapperImpl.class,
        CommentMapperImpl.class, BookingMapperImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemServiceImplTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    private User testUser;
    private Item testItem;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(new User(null, "Test User", "test@example.com"));
        testItem = itemRepository.save(
                Item.builder()
                        .name("Test Item")
                        .description("Test Description")
                        .available(true)
                        .owner(testUser)
                        .build()
        );
    }

    @Test
    void createItemShouldSaveAndReturnItem() {
        NewItemRequest itemDto = NewItemRequest.builder()
                .name("New Item")
                .description("New Description")
                .available(true)
                .build();

        ItemDto result = itemService.addItem(itemDto, testUser.getId());

        assertNotNull(result.getId());
        assertEquals(itemDto.getName(), result.getName());
        assertTrue(itemRepository.existsById(result.getId()));
    }

    @Test
    void createItemShouldThrowWhenRequestIdNotFound() {
        Long nonExistentRequestId = 999L;

        NewItemRequest newItemRequest = NewItemRequest.builder()
                .name("Item with non-existent request")
                .description("Description")
                .available(true)
                .requestId(nonExistentRequestId)
                .build();

        assertThrows(NotFoundException.class, () -> itemService.addItem(newItemRequest, testUser.getId()));
    }

    @Test
    void getItemByIdShouldReturnItem() {
        ItemDto result = itemService.getItemById(testItem.getId());

        assertEquals(testItem.getId(), result.getId());
        assertEquals(testItem.getName(), result.getName());
    }

    @Test
    void updateItemShouldUpdateFields() {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated Name")
                .description(null)
                .available(false)
                .build();

        ItemDto result = itemService.updateItem(updateDto, testUser.getId(), testItem.getId());

        assertEquals("Updated Name", result.getName());
        assertEquals(testItem.getDescription(), result.getDescription());
        assertFalse(result.getAvailable());
    }

    @Test
    void deleteItemShouldRemoveFromDb() {
        itemService.deleteItem(testItem.getId());

        assertFalse(itemRepository.existsById(testItem.getId()));
    }

    @Test
    void deleteItemShouldThrowWhenItemNotFound() {
        Long nonExistentItemId = 999L;

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.deleteItem(nonExistentItemId));

        assertEquals(String.format("Предмет с id = %d не найдена", nonExistentItemId),
                exception.getMessage());
    }

    @Test
    void getOwnerItemsShouldReturnItemsForOwner() {
        List<ItemOwnerDto> result = itemService.getOwnerItems(testUser.getId());

        assertFalse(result.isEmpty());
        assertEquals(testItem.getId(), result.get(0).getId());
    }

    @Test
    void getOwnerItemsShouldThrowWhenNoItemsFound() {
        User userWithoutItems = userRepository.save(new User(null, "No Items", "noitems@example.com"));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.getOwnerItems(userWithoutItems.getId()));

        assertEquals(String.format("Предметы владельца с id = %d не найдены", userWithoutItems.getId()),
                exception.getMessage());
    }

    @Test
    void searchItemsByTextShouldReturnMatchingItems() {
        List<ItemDto> result = itemService.getItemsByText("Test");

        assertFalse(result.isEmpty());
        assertEquals(testItem.getName(), result.get(0).getName());
    }

    @Test
    void searchItemsByTextShouldReturnEmptyListWhenTextIsEmpty() {
        List<ItemDto> resultEmpty = itemService.getItemsByText("");
        List<ItemDto> resultNull = itemService.getItemsByText(null);

        assertTrue(resultEmpty.isEmpty());
        assertTrue(resultNull.isEmpty());
    }

    @Test
    void createCommentShouldSaveAndReturnComment() {
        User booker = userRepository.save(new User(null, "Booker", "booker@example.com"));
        Booking booking = bookingRepository.save(
                Booking.builder()
                        .start(LocalDateTime.now().minusDays(2))
                        .end(LocalDateTime.now().minusDays(1))
                        .item(testItem)
                        .booker(booker)
                        .status(Status.APPROVED)
                        .build()
        );

        CommentDto commentDto = CommentDto.builder()
                .text("Test Comment")
                .build();

        CommentDto result = itemService.addComment(testItem.getId(), commentDto, booker.getId());

        assertNotNull(result.getId());
        assertEquals("Test Comment", result.getText());
        assertEquals("Booker", result.getAuthorName());
    }

    @Test
    void createCommentShouldThrowWhenNoCompletedBookings() {
        CommentDto commentDto = CommentDto.builder()
                .text("Test Comment")
                .build();

        assertThrows(CommentNotValidException.class, () ->
                itemService.addComment(testItem.getId(), commentDto, testUser.getId()));
    }

    @Test
    void getItemByIdShouldIncludeBookingsForOwner() {
        User booker = userRepository.save(new User(null, "Booker", "booker@example.com"));
        bookingRepository.save(
                Booking.builder()
                        .start(LocalDateTime.now().minusDays(2))
                        .end(LocalDateTime.now().minusDays(1))
                        .item(testItem)
                        .booker(booker)
                        .status(Status.APPROVED)
                        .build()
        );
        bookingRepository.save(
                Booking.builder()
                        .start(LocalDateTime.now().plusDays(1))
                        .end(LocalDateTime.now().plusDays(2))
                        .item(testItem)
                        .booker(booker)
                        .status(Status.APPROVED)
                        .build()
        );

        ItemDto result = itemService.getItemById(testItem.getId());

        assertNotNull(result);
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }
}
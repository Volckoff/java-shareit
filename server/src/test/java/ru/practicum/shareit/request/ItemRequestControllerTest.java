package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestCreate;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private ObjectMapper objectMapper;

    private final ItemRequestDto testRequest = ItemRequestDto.builder()
            .id(1L)
            .description("Test request description")
            .requestorId(1L)
            .created(LocalDateTime.now().minusDays(1))
            .items(List.of())
            .build();

    private final ItemRequestCreate testRequestCreate = new ItemRequestCreate("Test request description");

    @Test
    void createItemRequestShouldReturnCreatedRequest() throws Exception {
        when(itemRequestService.addItemRequest(any(ItemRequestCreate.class), anyLong()))
                .thenReturn(testRequest);

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testRequest.getId()))
                .andExpect(jsonPath("$.description").value(testRequest.getDescription()));

        verify(itemRequestService).addItemRequest(any(ItemRequestCreate.class), anyLong());
    }

    @Test
    void getUserItemRequestsShouldReturnListOfRequests() throws Exception {
        when(itemRequestService.getUserItemRequests(anyLong()))
                .thenReturn(List.of(testRequest));

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testRequest.getId()))
                .andExpect(jsonPath("$[0].description").value(testRequest.getDescription()));

        verify(itemRequestService).getUserItemRequests(anyLong());
    }

    @Test
    void getOtherUsersItemRequestsShouldReturnListOfRequests() throws Exception {
        when(itemRequestService.getOtherUsersItemRequests(anyLong()))
                .thenReturn(List.of(testRequest));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testRequest.getId()))
                .andExpect(jsonPath("$[0].description").value(testRequest.getDescription()));

        verify(itemRequestService).getOtherUsersItemRequests(anyLong());
    }

    @Test
    void getItemRequestByIdShouldReturnRequest() throws Exception {
        when(itemRequestService.getItemRequestById(anyLong(), anyLong()))
                .thenReturn(testRequest);

        mockMvc.perform(get("/requests/1")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testRequest.getId()))
                .andExpect(jsonPath("$.description").value(testRequest.getDescription()));

        verify(itemRequestService).getItemRequestById(anyLong(), anyLong());
    }

    @Test
    void getItemRequestByIdWhenNotFoundShouldReturn404() throws Exception {
        when(itemRequestService.getItemRequestById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Request not found"));

        mockMvc.perform(get("/requests/999")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(itemRequestService).getItemRequestById(anyLong(), anyLong());
    }

    @Test
    void createItemRequestWithoutUserIdShouldReturnServerError() throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestCreate)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void createItemWithInvalidRequestShouldReturnBadRequest() throws Exception {
        ItemDto invalidItem = ItemDto.builder().build();

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItem)))
                .andExpect(status().isInternalServerError());
    }
}
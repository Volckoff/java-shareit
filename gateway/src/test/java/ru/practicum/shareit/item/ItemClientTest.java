package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.item.dto.CommentDtoCreate;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;

import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ItemClientTest {

    @Mock
    private RestTemplateBuilder builder;

    @Mock
    private RestTemplate restTemplate;

    private ItemClient itemClient;

    private NewItemRequest newItemRequest;
    private ItemDto itemDto;
    private CommentDtoCreate commentDtoCreate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(builder.build()).thenReturn(restTemplate);
        when(builder.uriTemplateHandler(any())).thenReturn(builder);
        when(builder.requestFactory(any(Supplier.class))).thenReturn(builder);
        itemClient = new ItemClient("http://localhost", builder);

        newItemRequest = new NewItemRequest();
        newItemRequest.setName("Дрель");
        newItemRequest.setDescription("Мощная дрель");
        newItemRequest.setAvailable(true);
        newItemRequest.setRequestId(42L);

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Дрель");
        itemDto.setDescription("Мощная дрель");
        itemDto.setOwnerId(1L);
        itemDto.setRequestId(42L);
        itemDto.setAvailable(true);

        commentDtoCreate = new CommentDtoCreate("Отличная вещь!");
    }

    @Test
    void testGetOwnerItems() {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().body(List.of(itemDto));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = itemClient.getOwnerItems(1L);

        assertThat(result.getBody()).isEqualTo(List.of(itemDto));
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void testGetItemById() {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().body(itemDto);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = itemClient.getItemById(1L, 1L);

        assertThat(result.getBody()).isEqualTo(itemDto);
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void testCreateItem() {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().body(itemDto);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = itemClient.createItem(newItemRequest, 1L);

        assertThat(result.getBody()).isEqualTo(itemDto);
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void testUpdateItem() {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().body(itemDto);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = itemClient.updateItem(1L, itemDto, 1L);

        assertThat(result.getBody()).isEqualTo(itemDto);
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.PATCH),
                any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void testDeleteItem() {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = itemClient.deleteItem(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.DELETE),
                any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void testSearchItems() {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().body(List.of(itemDto));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), anyMap()))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = itemClient.searchItems("дрель");

        assertThat(result.getBody()).isEqualTo(List.of(itemDto));
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Object.class), anyMap());
    }

    @Test
    void testCreateComment() {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().body("Комментарий добавлен");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = itemClient.createComment(1L, commentDtoCreate, 1L);

        assertThat(result.getBody()).isEqualTo("Комментарий добавлен");
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(Object.class));
    }
}
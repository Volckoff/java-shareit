package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ItemRequestClientTest {

    @Mock
    private RestTemplateBuilder builder;

    @Mock
    private RestTemplate restTemplate;

    private ItemRequestClient itemRequestClient;

    private ItemRequestCreateDto requestDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(builder.build()).thenReturn(restTemplate);
        when(builder.uriTemplateHandler(any())).thenReturn(builder);
        when(builder.requestFactory(any(Supplier.class))).thenReturn(builder);
        itemRequestClient = new ItemRequestClient("http://localhost", builder);
        requestDto = new ItemRequestCreateDto("Нужна дрель");
    }

    @Test
    void testCreateItemRequest() {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().body("created");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = itemRequestClient.createItemRequest(1L, requestDto);

        assertThat(result.getBody()).isEqualTo("created");
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void testGetUserItemRequests() {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().body(List.of("request1", "request2"));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = itemRequestClient.getUserItemRequests(1L);

        assertThat(result.getBody()).isEqualTo(List.of("request1", "request2"));
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void testGetOtherUsersItemRequests() {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().body(List.of("other1", "other2"));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = itemRequestClient.getOtherUsersItemRequests(1L);

        assertThat(result.getBody()).isEqualTo(List.of("other1", "other2"));
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void testGetItemRequestById() {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().body("requestById");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = itemRequestClient.getItemRequestById(1L, 99L);

        assertThat(result.getBody()).isEqualTo("requestById");
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Object.class));
    }
}
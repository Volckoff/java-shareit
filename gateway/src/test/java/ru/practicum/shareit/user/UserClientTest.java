package ru.practicum.shareit.user;

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
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserClientTest {

    @Mock
    private RestTemplateBuilder builder;

    @Mock
    private RestTemplate restTemplate;

    private UserClient userClient;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(builder.build()).thenReturn(restTemplate);
        when(builder.uriTemplateHandler(any())).thenReturn(builder);
        when(builder.requestFactory(any(Supplier.class))).thenReturn(builder);
        userClient = new UserClient("http://localhost", builder);
        userDto = new UserDto(1L, "Test User", "test@example.com");
    }

    @Test
    void testFindAll() {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().body(List.of(userDto));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = userClient.findAll();

        assertThat(result.getBody()).isEqualTo(List.of(userDto));
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void testGetUserById() {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().body(userDto);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = userClient.getUserById(1L);

        assertThat(result.getBody()).isEqualTo(userDto);
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void testCreateUser() {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().body(userDto);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = userClient.createUser(userDto);

        assertThat(result.getBody()).isEqualTo(userDto);
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void testUpdateUser() {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().body(userDto);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = userClient.updateUser(1L, userDto);

        assertThat(result.getBody()).isEqualTo(userDto);
        verify(restTemplate, times(1)).exchange(anyString(),
                eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void testDeleteUser() {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = userClient.deleteUser(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.DELETE),
                any(HttpEntity.class), eq(Object.class));
    }
}
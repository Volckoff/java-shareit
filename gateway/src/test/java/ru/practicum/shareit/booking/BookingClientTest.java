package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BookingClientTest {

    @Mock
    private RestTemplateBuilder builder;

    @Mock
    private RestTemplate restTemplate;

    private BookingClient bookingClient;

    private BookItemRequestDto bookItemRequestDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(builder.build()).thenReturn(restTemplate);
        when(builder.uriTemplateHandler(any())).thenReturn(builder);
        when(builder.requestFactory(any(Supplier.class))).thenReturn(builder);
        bookingClient = new BookingClient("http://localhost", builder);
        bookItemRequestDto = new BookItemRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
    }

    @Test
    void testCreateBooking() {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().body("created");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = bookingClient.createBooking(1L, bookItemRequestDto);

        assertThat(result.getBody()).isEqualTo("created");
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void testGetBooking() {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().body("booking");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = bookingClient.getBooking(1L, 99L);

        assertThat(result.getBody()).isEqualTo("booking");
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void testApproveBooking() {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().body("approved");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class),
                anyMap())).thenReturn(responseEntity);

        ResponseEntity<Object> result = bookingClient.approveBooking(1L, 99L, true);

        assertThat(result.getBody()).isEqualTo("approved");
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.PATCH),
                any(HttpEntity.class), eq(Object.class), anyMap());
    }

    @Test
    void testGetUserBookings() {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().body(List.of("booking1", "booking2"));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), anyMap()))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = bookingClient.getUserBookings(1L, BookingState.ALL);

        assertThat(result.getBody()).isEqualTo(List.of("booking1", "booking2"));
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Object.class), anyMap());
    }

    @Test
    void testGetOwnerBookings() {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().body(List.of("ownerBooking1", "ownerBooking2"));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), anyMap()))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = bookingClient.getOwnerBookings(1L, BookingState.ALL);

        assertThat(result.getBody()).isEqualTo(List.of("ownerBooking1", "ownerBooking2"));
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Object.class), anyMap());
    }
}
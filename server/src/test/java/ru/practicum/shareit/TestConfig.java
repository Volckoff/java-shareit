//package ru.practicum.shareit;
//
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.ComponentScan;
//import ru.practicum.shareit.booking.dto.BookingMapper;
//import ru.practicum.shareit.item.dto.ItemMapper;
//import ru.practicum.shareit.user.dto.UserMapper;
//
//@TestConfiguration
//@ComponentScan(basePackages = "ru.practicum.shareit")
//public class TestConfig {
//
//    @Bean
//    public ItemMapper itemMapper() {
//        return org.mapstruct.factory.Mappers.getMapper(ItemMapper.class);
//    }
//
//    @Bean
//    public UserMapper userMapper() {
//        return org.mapstruct.factory.Mappers.getMapper(UserMapper.class);
//    }
//
//    @Bean
//    public BookingMapper bookingMapper() {
//        return org.mapstruct.factory.Mappers.getMapper(BookingMapper.class);
//    }
//}
package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestCreate;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto addItemRequest(@RequestBody ItemRequestCreate itemRequestCreate,
                                            @RequestHeader(USER_ID_HEADER) Long userId) {
        return itemRequestService.addItemRequest(itemRequestCreate, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getUserItemRequests(@RequestHeader(USER_ID_HEADER) Long userId) {
        return itemRequestService.getUserItemRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getOtherUsersItemRequests(@RequestHeader(USER_ID_HEADER) Long userId) {
        return itemRequestService.getOtherUsersItemRequests(userId);
    }

    @GetMapping("{requestId}")
    public ItemRequestDto getItemRequestById(@PathVariable Long requestId,
                                             @RequestHeader(USER_ID_HEADER) Long userId) {
        return itemRequestService.getItemRequestById(requestId, userId);
    }
}

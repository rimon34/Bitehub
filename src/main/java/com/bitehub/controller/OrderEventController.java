package com.bitehub.controller;

import com.bitehub.service.AuthService;
import com.bitehub.service.OrderEventService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/orders")
public class OrderEventController {
    private final OrderEventService orderEventService;
    private final AuthService authService;

    public OrderEventController(OrderEventService orderEventService, AuthService authService) {
        this.orderEventService = orderEventService;
        this.authService = authService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return orderEventService.subscribe(authService.getCurrentUser());
    }
}

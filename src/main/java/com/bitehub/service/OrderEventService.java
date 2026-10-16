package com.bitehub.service;

import com.bitehub.dto.OrderResponseDTO;
import com.bitehub.model.Order;
import com.bitehub.model.User;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Delivers order changes to authenticated browser sessions using server-sent events. */
@Service
public class OrderEventService {
    private static final long TIMEOUT_MS = 10 * 60 * 1000L;
    private static final long HEARTBEAT_SECONDS = 25;

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>> subscribers = new ConcurrentHashMap<>();

    private final ScheduledExecutorService heartbeat = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "sse-heartbeat");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * Streams that die silently (closed tab, dropped proxy connection) never fire
     * a completion callback, so without a heartbeat they would pile up server-side
     * and consume Tomcat threads indefinitely.
     */
    @PostConstruct
    void startHeartbeat() {
        heartbeat.scheduleAtFixedRate(this::pingAll, HEARTBEAT_SECONDS, HEARTBEAT_SECONDS, TimeUnit.SECONDS);
    }

    @PreDestroy
    void stopHeartbeat() {
        heartbeat.shutdownNow();
    }

    public SseEmitter subscribe(User user) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        CopyOnWriteArrayList<SseEmitter> emitters = subscribers.computeIfAbsent(user.getEmail(), key -> new CopyOnWriteArrayList<>());
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (java.io.IOException ex) {
            emitters.remove(emitter);
            emitter.completeWithError(ex);
        }
        return emitter;
    }

    public void publish(Order order) {
        Set<String> recipients = ConcurrentHashMap.newKeySet();
        if (order.getCustomer() != null) recipients.add(order.getCustomer().getEmail());
        if (order.getRestaurant() != null && order.getRestaurant().getUser() != null) recipients.add(order.getRestaurant().getUser().getEmail());
        if (order.getDeliveryPersonnel() != null && order.getDeliveryPersonnel().getUser() != null) recipients.add(order.getDeliveryPersonnel().getUser().getEmail());
        OrderResponseDTO payload = new OrderResponseDTO(order);
        recipients.forEach(email -> send(email, payload));
    }

    private void send(String email, OrderResponseDTO payload) {
        for (SseEmitter emitter : subscribers.getOrDefault(email, new CopyOnWriteArrayList<>())) {
            try {
                emitter.send(SseEmitter.event().name("order-updated").data(payload));
            } catch (java.io.IOException ex) {
                subscribers.getOrDefault(email, new CopyOnWriteArrayList<>()).remove(emitter);
                emitter.completeWithError(ex);
            }
        }
    }

    /** Detects and discards streams whose client has gone away. */
    private void pingAll() {
        subscribers.forEach((email, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
                } catch (Exception ex) {
                    emitters.remove(emitter);
                    try {
                        emitter.complete();
                    } catch (Exception ignored) {
                        // Nothing further to do; the emitter is already unusable.
                    }
                }
            }
        });
    }
}
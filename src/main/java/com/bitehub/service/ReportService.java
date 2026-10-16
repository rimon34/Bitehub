package com.bitehub.service;

import com.bitehub.dto.*;
import com.bitehub.exception.BadRequestException;
import com.bitehub.model.*;
import com.bitehub.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final DeliveryPersonnelRepository deliveryPersonnelRepository;
    private final ComplaintRepository complaintRepository;
    private final AuthService authService;

    public ReportService(OrderRepository orderRepository,
                         OrderItemRepository orderItemRepository,
                         UserRepository userRepository,
                         RestaurantRepository restaurantRepository,
                         DeliveryPersonnelRepository deliveryPersonnelRepository,
                         ComplaintRepository complaintRepository,
                         AuthService authService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.deliveryPersonnelRepository = deliveryPersonnelRepository;
        this.complaintRepository = complaintRepository;
        this.authService = authService;
    }

    /**
     * Admins see platform-wide data. A restaurant user may only ever see its own
     * restaurant's data, so their scope is resolved to their restaurant id.
     */
    private Long currentRestaurantId() {
        User user = authService.getCurrentUser();
        if (user.getRole() != Role.ROLE_RESTAURANT) {
            return null;
        }
        return restaurantRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("No restaurant profile associated with this account"))
                .getId();
    }

    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.setTotalUsers(userRepository.count());
        stats.setTotalCustomers(userRepository.countByRole(Role.ROLE_CUSTOMER));
        stats.setTotalRestaurants(restaurantRepository.count());
        stats.setPendingRestaurants(restaurantRepository.findByIsVerifiedFalse().size());
        stats.setTotalDelivery(deliveryPersonnelRepository.count());
        stats.setTotalOrders(orderRepository.count());
        stats.setPendingOrders(orderRepository.countByOrderStatus(OrderStatus.PLACED));
        stats.setActiveOrders(orderRepository.countByOrderStatus(OrderStatus.CONFIRMED)
                + orderRepository.countByOrderStatus(OrderStatus.PREPARING)
                + orderRepository.countByOrderStatus(OrderStatus.READY_FOR_PICKUP)
                + orderRepository.countByOrderStatus(OrderStatus.OUT_FOR_DELIVERY));
        stats.setCompletedOrders(orderRepository.countByOrderStatus(OrderStatus.DELIVERED));
        stats.setCancelledOrders(orderRepository.countByOrderStatus(OrderStatus.CANCELLED));

        Double revenue = orderRepository.calculateTotalSystemRevenue();
        stats.setTotalRevenue(revenue != null ? Math.round(revenue * 100.0) / 100.0 : 0.0);

        stats.setOpenComplaints(complaintRepository.countByStatus(ComplaintStatus.OPEN) +
                complaintRepository.countByStatus(ComplaintStatus.UNDER_INVESTIGATION));
        return stats;
    }

    public List<SalesReportDTO> getDailyOrdersReport() {
        List<SalesReportDTO> reports = new ArrayList<>();
        Long restaurantId = currentRestaurantId();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.atTime(LocalTime.MAX);

            List<Order> orders = restaurantId == null
                    ? orderRepository.findOrdersBetweenDates(start, end)
                    : orderRepository.findRestaurantOrdersBetweenDates(restaurantId, start, end);
            long count = orders.size();
            double rev = orders.stream()
                    .filter(o -> o.getOrderStatus() == OrderStatus.DELIVERED)
                    .mapToDouble(Order::getTotalAmount)
                    .sum();

            reports.add(new SalesReportDTO(day.format(formatter), count, Math.round(rev * 100.0) / 100.0));
        }
        return reports;
    }

    public List<SalesReportDTO> getWeeklyOrdersReport() {
        List<SalesReportDTO> reports = new ArrayList<>();
        Long restaurantId = currentRestaurantId();
        LocalDate today = LocalDate.now();

        for (int i = 3; i >= 0; i--) {
            LocalDate startDay = today.minusWeeks(i + 1).plusDays(1);
            LocalDate endDay = today.minusWeeks(i);

            LocalDateTime start = startDay.atStartOfDay();
            LocalDateTime end = endDay.atTime(LocalTime.MAX);

            List<Order> orders = restaurantId == null
                    ? orderRepository.findOrdersBetweenDates(start, end)
                    : orderRepository.findRestaurantOrdersBetweenDates(restaurantId, start, end);
            long count = orders.size();
            double rev = orders.stream()
                    .filter(o -> o.getOrderStatus() == OrderStatus.DELIVERED)
                    .mapToDouble(Order::getTotalAmount)
                    .sum();

            reports.add(new SalesReportDTO("Week " + (4 - i) + " (" + startDay.getMonth().name().substring(0, 3) + ")",
                    count, Math.round(rev * 100.0) / 100.0));
        }
        return reports;
    }

    public List<SalesReportDTO> getMonthlyOrdersReport() {
        List<SalesReportDTO> reports = new ArrayList<>();
        Long restaurantId = currentRestaurantId();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

        for (int i = 5; i >= 0; i--) {
            LocalDate targetMonth = today.minusMonths(i);
            LocalDate startDay = targetMonth.withDayOfMonth(1);
            LocalDate endDay = targetMonth.withDayOfMonth(targetMonth.lengthOfMonth());

            LocalDateTime start = startDay.atStartOfDay();
            LocalDateTime end = endDay.atTime(LocalTime.MAX);

            List<Order> orders = restaurantId == null
                    ? orderRepository.findOrdersBetweenDates(start, end)
                    : orderRepository.findRestaurantOrdersBetweenDates(restaurantId, start, end);
            long count = orders.size();
            double rev = orders.stream()
                    .filter(o -> o.getOrderStatus() == OrderStatus.DELIVERED)
                    .mapToDouble(Order::getTotalAmount)
                    .sum();

            reports.add(new SalesReportDTO(targetMonth.format(formatter), count, Math.round(rev * 100.0) / 100.0));
        }
        return reports;
    }

    public List<TopItemDTO> getMostOrderedFoodItems(int limit) {
        Long restaurantId = currentRestaurantId();
        List<Object[]> rawList = restaurantId == null
                ? orderItemRepository.findMostOrderedItems(PageRequest.of(0, limit))
                : orderItemRepository.findMostOrderedItemsByRestaurant(restaurantId, PageRequest.of(0, limit));
        List<TopItemDTO> result = new ArrayList<>();

        for (Object[] row : rawList) {
            String name = (String) row[0];
            long qty = ((Number) row[1]).longValue();
            double revenue = Math.round(((Number) row[2]).doubleValue() * 100.0) / 100.0;
            result.add(new TopItemDTO(name, qty, revenue));
        }
        return result;
    }

    public List<RestaurantPerformanceDTO> getRestaurantPerformance() {
        Long restaurantId = currentRestaurantId();
        List<Restaurant> restaurants = restaurantId == null
                ? restaurantRepository.findAll()
                : restaurantRepository.findById(restaurantId).map(List::of).orElseGet(List::of);
        List<RestaurantPerformanceDTO> list = new ArrayList<>();

        for (Restaurant r : restaurants) {
            List<Order> orders = orderRepository.findByRestaurantIdOrderByPlacedAtDesc(r.getId());
            long totalOrders = orders.size();
            long completed = orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.DELIVERED).count();
            long cancelled = orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.CANCELLED).count();
            double rev = orders.stream()
                    .filter(o -> o.getOrderStatus() == OrderStatus.DELIVERED)
                    .mapToDouble(Order::getTotalAmount)
                    .sum();

            list.add(new RestaurantPerformanceDTO(
                    r.getId(),
                    r.getName(),
                    r.getCuisineType(),
                    totalOrders,
                    completed,
                    cancelled,
                    Math.round(rev * 100.0) / 100.0,
                    r.getRating()
            ));
        }

        list.sort((a, b) -> Double.compare(b.getTotalRevenue(), a.getTotalRevenue()));
        return list;
    }
}

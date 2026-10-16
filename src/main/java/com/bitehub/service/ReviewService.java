package com.bitehub.service;

import com.bitehub.dto.ReviewDTO;
import com.bitehub.dto.ReviewRequest;
import com.bitehub.exception.BadRequestException;
import com.bitehub.exception.ResourceNotFoundException;
import com.bitehub.model.Order;
import com.bitehub.model.OrderStatus;
import com.bitehub.model.Restaurant;
import com.bitehub.model.Review;
import com.bitehub.model.User;
import com.bitehub.repository.OrderRepository;
import com.bitehub.repository.RestaurantRepository;
import com.bitehub.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final AuthService authService;

    public ReviewService(ReviewRepository reviewRepository,
                         OrderRepository orderRepository,
                         RestaurantRepository restaurantRepository,
                         AuthService authService) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.restaurantRepository = restaurantRepository;
        this.authService = authService;
    }

    @Transactional
    public ReviewDTO createReview(ReviewRequest request) {
        User customer = authService.getCurrentUser();
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new BadRequestException("You can only review your own orders");
        }

        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("You can only review an order after it has been delivered");
        }

        if (reviewRepository.existsByOrderId(order.getId())) {
            throw new BadRequestException("You have already reviewed this order");
        }

        Review review = new Review();
        review.setOrder(order);
        review.setCustomer(customer);
        review.setRestaurant(order.getRestaurant());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        // Update restaurant rating
        Restaurant restaurant = order.getRestaurant();
        Double avgRating = reviewRepository.calculateAverageRatingForRestaurant(restaurant.getId());
        long totalReviews = reviewRepository.countByRestaurantId(restaurant.getId());
        restaurant.setRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 5.0);
        restaurant.setTotalReviews((int) totalReviews);
        restaurantRepository.save(restaurant);

        return new ReviewDTO(savedReview);
    }

    public List<ReviewDTO> getReviewsByRestaurant(Long restaurantId) {
        return reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream()
                .map(ReviewDTO::new)
                .collect(Collectors.toList());
    }

    public List<ReviewDTO> getMyReviews() {
        User user = authService.getCurrentUser();
        return reviewRepository.findByCustomerIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(ReviewDTO::new)
                .collect(Collectors.toList());
    }
}

package com.bitehub.service;

import com.bitehub.dto.MenuItemDTO;
import com.bitehub.dto.MenuItemRequest;
import com.bitehub.exception.BadRequestException;
import com.bitehub.exception.ResourceNotFoundException;
import com.bitehub.model.MenuItem;
import com.bitehub.model.Restaurant;
import com.bitehub.model.Role;
import com.bitehub.model.User;
import com.bitehub.repository.MenuItemRepository;
import com.bitehub.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final AuthService authService;

    public MenuItemService(MenuItemRepository menuItemRepository,
                           RestaurantRepository restaurantRepository,
                           AuthService authService) {
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
        this.authService = authService;
    }

    public List<MenuItemDTO> getMenuItemsByRestaurant(Long restaurantId, boolean onlyAvailable) {
        List<MenuItem> items;
        if (onlyAvailable) {
            items = menuItemRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId);
        } else {
            items = menuItemRepository.findByRestaurantId(restaurantId);
        }
        return items.stream().map(MenuItemDTO::new).collect(Collectors.toList());
    }

    public List<MenuItemDTO> getMenuItemsByCategory(Long restaurantId, String category) {
        return menuItemRepository.findByRestaurantIdAndCategoryIgnoreCase(restaurantId, category).stream()
                .map(MenuItemDTO::new)
                .collect(Collectors.toList());
    }

    public List<String> getCategoriesForRestaurant(Long restaurantId) {
        return menuItemRepository.findCategoriesByRestaurantId(restaurantId);
    }

    public MenuItemDTO getMenuItemById(Long id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));
        return new MenuItemDTO(item);
    }

    @Transactional
    public MenuItemDTO createMenuItem(Long restaurantId, MenuItemRequest request) {
        User user = authService.getCurrentUser();
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

        if (user.getRole() != Role.ROLE_ADMIN && !restaurant.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You are not authorized to add menu items to this restaurant");
        }

        MenuItem item = new MenuItem();
        item.setRestaurant(restaurant);
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setPrice(request.getPrice());
        item.setImageUrl(request.getImageUrl() != null && !request.getImageUrl().isBlank()
                ? request.getImageUrl()
                : "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500&auto=format&fit=crop");
        item.setAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);
        item.setVegetarian(request.getIsVegetarian() != null ? request.getIsVegetarian() : false);
        item.setPreparationTimeMinutes(request.getPreparationTimeMinutes() != null ? request.getPreparationTimeMinutes() : 20);

        return new MenuItemDTO(menuItemRepository.save(item));
    }

    @Transactional
    public MenuItemDTO updateMenuItem(Long id, MenuItemRequest request) {
        User user = authService.getCurrentUser();
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));

        if (user.getRole() != Role.ROLE_ADMIN && !item.getRestaurant().getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You are not authorized to modify this menu item");
        }

        if (request.getName() != null) item.setName(request.getName());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getCategory() != null) item.setCategory(request.getCategory());
        if (request.getPrice() != null) item.setPrice(request.getPrice());
        if (request.getImageUrl() != null) item.setImageUrl(request.getImageUrl());
        if (request.getIsAvailable() != null) item.setAvailable(request.getIsAvailable());
        if (request.getIsVegetarian() != null) item.setVegetarian(request.getIsVegetarian());
        if (request.getPreparationTimeMinutes() != null) item.setPreparationTimeMinutes(request.getPreparationTimeMinutes());

        return new MenuItemDTO(menuItemRepository.save(item));
    }

    @Transactional
    public MenuItemDTO toggleAvailability(Long id) {
        User user = authService.getCurrentUser();
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));

        if (user.getRole() != Role.ROLE_ADMIN && !item.getRestaurant().getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You are not authorized to modify this menu item");
        }

        item.setAvailable(!item.isAvailable());
        return new MenuItemDTO(menuItemRepository.save(item));
    }

    @Transactional
    public void deleteMenuItem(Long id) {
        User user = authService.getCurrentUser();
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));

        if (user.getRole() != Role.ROLE_ADMIN && !item.getRestaurant().getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You are not authorized to delete this menu item");
        }

        menuItemRepository.delete(item);
    }
}

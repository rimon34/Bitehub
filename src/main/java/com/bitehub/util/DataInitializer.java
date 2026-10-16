package com.bitehub.util;

import com.bitehub.model.*;
import com.bitehub.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final DeliveryPersonnelRepository deliveryPersonnelRepository;
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final CouponRepository couponRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Fixed, realistic demo coordinates around the campus base point. Seeded
     * restaurants use fictional addresses that cannot be geocoded reliably, so
     * they are given deterministic coordinates instead.
     */
    private static final Map<String, double[]> DEMO_RESTAURANT_COORDS = Map.of(
            "burger@bitehub.com", new double[]{23.7937, 90.4066},
            "pizza@bitehub.com", new double[]{23.7990, 90.4120},
            "biryani@bitehub.com", new double[]{23.8060, 90.4180},
            "sushi@bitehub.com", new double[]{23.8010, 90.4090},
            "fastfood@bitehub.com", new double[]{23.7965, 90.4145},
            "cafe@bitehub.com", new double[]{23.7910, 90.4110}
    );
    private static final double[] DEMO_DELIVERY_COORDS = {23.8115, 90.4244};
    private static final Set<String> DEMO_ORDER_NUMBERS = Set.of("BH-10024", "BH-10019");

    public DataInitializer(
            UserRepository userRepository,
            RestaurantRepository restaurantRepository,
            MenuItemRepository menuItemRepository,
            DeliveryPersonnelRepository deliveryPersonnelRepository,
            OrderRepository orderRepository,
            AddressRepository addressRepository,
            CouponRepository couponRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.deliveryPersonnelRepository = deliveryPersonnelRepository;
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.couponRepository = couponRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Ensure System Admin
        User admin = userRepository.findByEmail("admin@bitehub.com").orElse(null);
        if (admin == null) {
            admin = new User("System Admin", "admin@bitehub.com", passwordEncoder.encode("admin123"), "+1 555-0100", Role.ROLE_ADMIN);
            admin.setProfileImageUrl("/images/bitehub-logo.jpg");
            userRepository.save(admin);
        }

        // Only seed restaurants and sample data if no restaurants exist
        if (restaurantRepository.count() == 0) {
            System.out.println(">>> BiteHub: Seeding rich demo dataset... <<<");

            // 1. Seed Customer Sarah
            User customer = userRepository.findByEmail("customer@bitehub.com").orElse(null);
            if (customer == null) {
                customer = new User("Sarah Jenkins", "customer@bitehub.com", passwordEncoder.encode("customer123"), "+1 555-0199", Role.ROLE_CUSTOMER);
                customer.setProfileImageUrl("/images/cat_burger.jpg");
                customer = userRepository.save(customer);
            }

            // Customer Address
            Address customerAddr = new Address();
            customerAddr.setUser(customer);
            customerAddr.setLabel("Campus Dorm");
            customerAddr.setStreetAddress("Hall of Residence 3, Room 412");
            customerAddr.setCity("University Campus");
            customerAddr.setPostalCode("1212");
            customerAddr.setContactPhone("+1 555-0199");
            customerAddr.setDefault(true);
            addressRepository.save(customerAddr);

            // 2. Seed Delivery Rider John Swift
            User riderUser = userRepository.findByEmail("rider@bitehub.com").orElse(null);
            if (riderUser == null) {
                riderUser = new User("John Swift", "rider@bitehub.com", passwordEncoder.encode("rider123"), "+1 555-0211", Role.ROLE_DELIVERY);
                riderUser = userRepository.save(riderUser);
            }

            DeliveryPersonnel rider = new DeliveryPersonnel();
            rider.setUser(riderUser);
            rider.setVehicleType("Motorcycle");
            rider.setLicenseNumber("DH-M-8892");
            rider.setAvailable(true);
            rider.setActiveOrdersCount(1);
            rider.setRating(4.9);
            rider = deliveryPersonnelRepository.save(rider);

            // 3. Seed Restaurants
            // 3a. The Burger Hub
            User burgerOwner = userRepository.findByEmail("burger@bitehub.com").orElse(null);
            if (burgerOwner == null) {
                burgerOwner = new User("Marcus Vance", "burger@bitehub.com", passwordEncoder.encode("burger123"), "+1 555-0144", Role.ROLE_RESTAURANT);
                burgerOwner = userRepository.save(burgerOwner);
            }
            Restaurant rBurger = new Restaurant();
            rBurger.setUser(burgerOwner);
            rBurger.setName("The Burger Hub");
            rBurger.setDescription("Handcrafted smashburgers, crispy fries, and premium thick milkshakes.");
            rBurger.setCuisineType("American");
            rBurger.setAddress("Campus North Avenue, Building 4");
            rBurger.setPhone("+1 555-0144");
            rBurger.setImageUrl("/images/cat_burger.jpg");
            rBurger.setBannerUrl("https://images.unsplash.com/photo-1550547660-d9450f859349?w=1200");
            rBurger.setOpen(true);
            rBurger.setVerified(true);
            rBurger.setRating(4.8);
            rBurger.setTotalReviews(142);
            rBurger = restaurantRepository.save(rBurger);

            MenuItem mBurger1 = createMenuItem(rBurger, "Classic Double Smashburger", "Burgers", 9.99, "Two seasoned beef patties, melted cheddar, pickles, house sauce", "/images/cat_burger.jpg", false);
            MenuItem mBurger2 = createMenuItem(rBurger, "Crispy Bacon BBQ Burger", "Burgers", 11.49, "Smoked bacon, aged cheddar, crispy onion rings, BBQ sauce", "/images/cat_burger.jpg", false);
            MenuItem mBurger3 = createMenuItem(rBurger, "Golden Herb Fries", "Fast Food", 3.99, "Crisp potato fries tossed with rosemary garlic seasoning", "/images/cat_fastfood.jpg", true);
            MenuItem mBurger4 = createMenuItem(rBurger, "Chunky Chocolate Shake", "Beverages", 4.99, "Rich creamy chocolate shake topped with whipped cream", "/images/cat_beverages.jpg", true);
            menuItemRepository.save(mBurger1);
            menuItemRepository.save(mBurger2);
            menuItemRepository.save(mBurger3);
            menuItemRepository.save(mBurger4);

            // 3b. Napoli Artisan Pizza
            User pizzaOwner = userRepository.findByEmail("pizza@bitehub.com").orElse(null);
            if (pizzaOwner == null) {
                pizzaOwner = new User("Giovanni Rossi", "pizza@bitehub.com", passwordEncoder.encode("pizza123"), "+1 555-0155", Role.ROLE_RESTAURANT);
                pizzaOwner = userRepository.save(pizzaOwner);
            }
            Restaurant rPizza = new Restaurant();
            rPizza.setUser(pizzaOwner);
            rPizza.setName("Napoli Artisan Pizza");
            rPizza.setDescription("Wood-fired Neapolitan sourdough pizza made with imported Italian ingredients.");
            rPizza.setCuisineType("Italian");
            rPizza.setAddress("Dormitory Plaza, Food Street");
            rPizza.setPhone("+1 555-0155");
            rPizza.setImageUrl("/images/cat_pizza.jpg");
            rPizza.setBannerUrl("https://images.unsplash.com/photo-1513104890138-7c749659a591?w=1200");
            rPizza.setOpen(true);
            rPizza.setVerified(true);
            rPizza.setRating(4.7);
            rPizza.setTotalReviews(98);
            rPizza = restaurantRepository.save(rPizza);

            MenuItem mPizza1 = createMenuItem(rPizza, "Margherita Superiore", "Italian", 12.99, "San Marzano tomatoes, fresh buffalo mozzarella, fragrant basil, extra virgin olive oil", "/images/cat_pizza.jpg", true);
            MenuItem mPizza2 = createMenuItem(rPizza, "Spicy Pepperoni & Hot Honey", "Italian", 14.99, "Artisan pepperoni, hot honey drizzle, crushed red pepper, mozzarella", "/images/cat_pizza.jpg", false);
            MenuItem mPizza3 = createMenuItem(rPizza, "Garlic Butter Breadsticks", "Italian", 5.49, "Warm baked dough with garlic butter, parmesan, and marinara dip", "/images/cat_fastfood.jpg", true);
            menuItemRepository.save(mPizza1);
            menuItemRepository.save(mPizza2);
            menuItemRepository.save(mPizza3);

            // 3c. Royal Biryani House
            User biryaniOwner = userRepository.findByEmail("biryani@bitehub.com").orElse(null);
            if (biryaniOwner == null) {
                biryaniOwner = new User("Tariq Ahmed", "biryani@bitehub.com", passwordEncoder.encode("biryani123"), "+1 555-0166", Role.ROLE_RESTAURANT);
                biryaniOwner = userRepository.save(biryaniOwner);
            }
            Restaurant rBiryani = new Restaurant();
            rBiryani.setUser(biryaniOwner);
            rBiryani.setName("Royal Biryani House");
            rBiryani.setDescription("Traditional slow-dum biryani, kebabs, and aromatic Mughal specialties.");
            rBiryani.setCuisineType("Indian");
            rBiryani.setAddress("University Gate #2 Plaza");
            rBiryani.setPhone("+1 555-0166");
            rBiryani.setImageUrl("/images/cat_biryani.jpg");
            rBiryani.setBannerUrl("https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=1200");
            rBiryani.setOpen(true);
            rBiryani.setVerified(true);
            rBiryani.setRating(4.9);
            rBiryani.setTotalReviews(230);
            rBiryani = restaurantRepository.save(rBiryani);

            MenuItem mBiryani1 = createMenuItem(rBiryani, "Kacchi Mutton Biryani", "Indian", 13.99, "Fragrant aged basmati rice layered with spiced marinated mutton and tender potato", "/images/cat_biryani.jpg", false);
            MenuItem mBiryani2 = createMenuItem(rBiryani, "Chicken Dum Biryani", "Indian", 10.99, "Tender chicken cooked on dum with saffron-infused rice and boiled egg", "/images/cat_biryani.jpg", false);
            MenuItem mBiryani3 = createMenuItem(rBiryani, "Chicken Tikka Kebab (4 pcs)", "Indian", 7.99, "Charcoal grilled chicken skewers marinated in yogurt and tandoori spices", "/images/cat_fastfood.jpg", false);
            menuItemRepository.save(mBiryani1);
            menuItemRepository.save(mBiryani2);
            menuItemRepository.save(mBiryani3);

            // 3d. Tokyo Express Sushi
            User sushiOwner = userRepository.findByEmail("sushi@bitehub.com").orElse(null);
            if (sushiOwner == null) {
                sushiOwner = new User("Kenji Tanaka", "sushi@bitehub.com", passwordEncoder.encode("sushi123"), "+1 555-0177", Role.ROLE_RESTAURANT);
                sushiOwner = userRepository.save(sushiOwner);
            }
            Restaurant rSushi = new Restaurant();
            rSushi.setUser(sushiOwner);
            rSushi.setName("Tokyo Express Sushi");
            rSushi.setDescription("Fresh sushi, maki rolls, sashimi platters, and authentic Japanese appetizers.");
            rSushi.setCuisineType("Japanese");
            rSushi.setAddress("Science Complex, Food Arcade");
            rSushi.setPhone("+1 555-0177");
            rSushi.setImageUrl("/images/cat_sushi.jpg");
            rSushi.setBannerUrl("https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=1200");
            rSushi.setOpen(true);
            rSushi.setVerified(true);
            rSushi.setRating(4.6);
            rSushi.setTotalReviews(75);
            rSushi = restaurantRepository.save(rSushi);

            MenuItem mSushi1 = createMenuItem(rSushi, "Salmon Nigiri & Sashimi Combo", "Japanese", 16.99, "Fresh Norwegian salmon slices served with seasoned sushi rice and pickled ginger", "/images/cat_sushi.jpg", false);
            MenuItem mSushi2 = createMenuItem(rSushi, "California Roll (8 pcs)", "Japanese", 8.99, "Crab, cucumber, avocado rolled with toasted sesame seeds and masago", "/images/cat_sushi.jpg", false);
            MenuItem mSushi3 = createMenuItem(rSushi, "Steamed Salted Edamame", "Japanese", 4.50, "Fresh soybean pods tossed in sea salt flakes", "/images/cat_fastfood.jpg", true);
            menuItemRepository.save(mSushi1);
            menuItemRepository.save(mSushi2);
            menuItemRepository.save(mSushi3);

            // 3e. Crispy Fast Bites
            User fastOwner = userRepository.findByEmail("fastfood@bitehub.com").orElse(null);
            if (fastOwner == null) {
                fastOwner = new User("David Miller", "fastfood@bitehub.com", passwordEncoder.encode("fastfood123"), "+1 555-0188", Role.ROLE_RESTAURANT);
                fastOwner = userRepository.save(fastOwner);
            }
            Restaurant rFast = new Restaurant();
            rFast.setUser(fastOwner);
            rFast.setName("Crispy Fast Bites");
            rFast.setDescription("Crunchy fried chicken, hot wings, crispy chicken burgers, and loaded fries.");
            rFast.setCuisineType("Fast Food");
            rFast.setAddress("Student Activity Center");
            rFast.setPhone("+1 555-0188");
            rFast.setImageUrl("/images/cat_fastfood.jpg");
            rFast.setBannerUrl("https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=1200");
            rFast.setOpen(true);
            rFast.setVerified(true);
            rFast.setRating(4.4);
            rFast.setTotalReviews(112);
            rFast = restaurantRepository.save(rFast);

            MenuItem mFast1 = createMenuItem(rFast, "Crispy Golden Chicken (4 pcs)", "Fast Food", 8.99, "Deep fried crunchy chicken marinated in secret herbs & spices", "/images/cat_fastfood.jpg", false);
            MenuItem mFast2 = createMenuItem(rFast, "Spicy Chicken Tenders Basket", "Fast Food", 7.49, "Four crispy tenders with honey mustard and tangy chili dips", "/images/cat_fastfood.jpg", false);
            menuItemRepository.save(mFast1);
            menuItemRepository.save(mFast2);

            // 3f. Campus Brew & Shakes
            User cafeOwner = userRepository.findByEmail("cafe@bitehub.com").orElse(null);
            if (cafeOwner == null) {
                cafeOwner = new User("Chloe Bennett", "cafe@bitehub.com", passwordEncoder.encode("cafe123"), "+1 555-0190", Role.ROLE_RESTAURANT);
                cafeOwner = userRepository.save(cafeOwner);
            }
            Restaurant rCafe = new Restaurant();
            rCafe.setUser(cafeOwner);
            rCafe.setName("Campus Brew & Shakes");
            rCafe.setDescription("Freshly brewed artisan coffee, ice blends, fruit smoothies, and pastries.");
            rCafe.setCuisineType("Beverages");
            rCafe.setAddress("Central Library Promenade");
            rCafe.setPhone("+1 555-0190");
            rCafe.setImageUrl("/images/cat_beverages.jpg");
            rCafe.setBannerUrl("https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=1200");
            rCafe.setOpen(true);
            rCafe.setVerified(true);
            rCafe.setRating(4.5);
            rCafe.setTotalReviews(62);
            rCafe = restaurantRepository.save(rCafe);

            MenuItem mCafe1 = createMenuItem(rCafe, "Iced Caramel Macchiato", "Beverages", 4.75, "Espresso layered with chilled milk, vanilla syrup, and sweet caramel drizzle", "/images/cat_beverages.jpg", true);
            MenuItem mCafe2 = createMenuItem(rCafe, "Berry Blast Smoothie", "Beverages", 5.25, "Real blueberries, raspberries, strawberries blended with Greek yogurt", "/images/cat_beverages.jpg", true);
            menuItemRepository.save(mCafe1);
            menuItemRepository.save(mCafe2);

            // 4. Seed Coupons
            Coupon c1 = new Coupon();
            c1.setCode("BITE20");
            c1.setDiscountType(DiscountType.PERCENTAGE);
            c1.setDiscountValue(20.0);
            c1.setMinOrderAmount(15.0);
            c1.setMaxDiscountAmount(10.0);
            c1.setExpiryDate(LocalDate.now().plusMonths(6));
            c1.setActive(true);
            couponRepository.save(c1);

            Coupon c2 = new Coupon();
            c2.setCode("CAMPUS5");
            c2.setDiscountType(DiscountType.FIXED_AMOUNT);
            c2.setDiscountValue(5.0);
            c2.setMinOrderAmount(20.0);
            c2.setMaxDiscountAmount(5.0);
            c2.setExpiryDate(LocalDate.now().plusMonths(6));
            c2.setActive(true);
            couponRepository.save(c2);

            // 5. Seed Live Orders for Customer Sarah
            // Order 1: OUT_FOR_DELIVERY (Live tracking order on map!)
            Order order1 = new Order();
            order1.setOrderNumber("BH-10024");
            order1.setCustomer(customer);
            order1.setRestaurant(rBurger);
            order1.setDeliveryPersonnel(rider);
            order1.setOrderStatus(OrderStatus.OUT_FOR_DELIVERY);
            order1.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
            order1.setPaymentStatus(PaymentStatus.PENDING);
            order1.setDeliveryAddressText("Hall of Residence 3, Room 412, University Campus");
            order1.setCustomerPhone("+1 555-0199");
            order1.setSpecialInstructions("Please ring the doorbell or call when at the gate.");
            order1.setSubtotal(23.97);
            order1.setDeliveryFee(2.50);
            order1.setTotalAmount(26.47);
            order1.setPlacedAt(LocalDateTime.now().minusMinutes(22));

            OrderItem item1 = new OrderItem(mBurger1, mBurger1.getName(), mBurger1.getPrice(), 2, 19.98);
            item1.setOrder(order1);
            OrderItem item2 = new OrderItem(mBurger3, mBurger3.getName(), mBurger3.getPrice(), 1, 3.99);
            item2.setOrder(order1);

            order1.getItems().add(item1);
            order1.getItems().add(item2);
            orderRepository.save(order1);

            // Order 2: DELIVERED (Past order for history)
            Order order2 = new Order();
            order2.setOrderNumber("BH-10019");
            order2.setCustomer(customer);
            order2.setRestaurant(rPizza);
            order2.setDeliveryPersonnel(rider);
            order2.setOrderStatus(OrderStatus.DELIVERED);
            order2.setPaymentMethod(PaymentMethod.CREDIT_CARD);
            order2.setPaymentStatus(PaymentStatus.COMPLETED);
            order2.setDeliveryAddressText("Hall of Residence 3, Room 412, University Campus");
            order2.setCustomerPhone("+1 555-0199");
            order2.setSubtotal(12.99);
            order2.setDeliveryFee(2.50);
            order2.setTotalAmount(15.49);
            order2.setPlacedAt(LocalDateTime.now().minusDays(1));

            OrderItem item3 = new OrderItem(mPizza1, mPizza1.getName(), mPizza1.getPrice(), 1, 12.99);
            item3.setOrder(order2);
            order2.getItems().add(item3);
            orderRepository.save(order2);

            System.out.println(">>> BiteHub: Demo dataset seeded successfully! <<<");
        }

        // Idempotently assign coordinates to demo rows (also heals databases that
        // were seeded before coordinates existed).
        seedDemoCoordinates();
    }

    /**
     * Gives demo restaurants/orders deterministic coordinates without touching
     * real user data (matched by owner email / known order number).
     */
    private void seedDemoCoordinates() {
        for (Restaurant restaurant : restaurantRepository.findAll()) {
            if (restaurant.getLatitude() != null && restaurant.getLongitude() != null) continue;
            if (restaurant.getUser() == null) continue;
            double[] coords = DEMO_RESTAURANT_COORDS.get(restaurant.getUser().getEmail());
            if (coords != null) {
                restaurant.setLatitude(coords[0]);
                restaurant.setLongitude(coords[1]);
                restaurantRepository.save(restaurant);
            }
        }

        for (Order order : orderRepository.findAll()) {
            if (order.getDeliveryLatitude() != null && order.getDeliveryLongitude() != null) continue;
            if (DEMO_ORDER_NUMBERS.contains(order.getOrderNumber())) {
                order.setDeliveryLatitude(DEMO_DELIVERY_COORDS[0]);
                order.setDeliveryLongitude(DEMO_DELIVERY_COORDS[1]);
                orderRepository.save(order);
            }
        }
    }

    private MenuItem createMenuItem(Restaurant r, String name, String category, Double price, String desc, String img, boolean veg) {
        MenuItem item = new MenuItem();
        item.setRestaurant(r);
        item.setName(name);
        item.setCategory(category);
        item.setPrice(price);
        item.setDescription(desc);
        item.setImageUrl(img);
        item.setVegetarian(veg);
        item.setAvailable(true);
        item.setPreparationTimeMinutes(20);
        return item;
    }
}
# Bitehub
A full-stack, real-time **food delivery platform** driven by **Spring Boot, MySQL, and Vanilla JS**. Users can order food, manage restaurants, track deliveries, and monitor the entire ecosystem — while the system handles strict security protocols, dynamic cart calculations, and role-based access.

---

## 👥 Team Members

| Name | Role |
| :--- | :--- |
| **Shams Mohammad** | Team leader and project manager, debugger✨ |
| **Syed Mahian Morshed** | System developer 💻 |
| **Miftaul Wasif** | System developer 💻 |
| **Md. Rimon Hasan** | System developer 💻,UI/UX and developer |
| **Md. Maruf Hossen** | System developer 💻 |

---

## 🌍 Base Version APIs

| API Module | Purpose |
| :--- | :--- |
| **Authentication API** | JWT-based login, registration, and role assignment for all users |
| **Restaurant API** | Fetches open restaurants, verified menus, cuisines, and price tiers |
| **Order API** | Handles cart checkout, status updates, and historical order retrieval |
| **Delivery API** | Assigns riders to orders and tracks delivery status transitions |

---

## 🍔 Base App Theme

- **Frontend View:** Single-Page Application (SPA) feel with dynamic DOM manipulation and Dark Mode.
- **The Ecosystem:** The platform hosts a **multi-sided marketplace** connecting hungry customers with local restaurants and delivery riders.
- **Core Loop:** **Browse → Order → Prepare → Deliver.** Ordering initiates a chain of real-time state changes handled by different user roles.
- **Strategic Foresight:** **Cart & Cost Management.** The user can view sub-totals, dynamic item quantities, and the fixed delivery fee before finalizing checkout.
- **Market Dynamics:** Restaurants manage their digital storefront for profit:
  - *Active Management:* Fast prep times and high ratings → more customer traffic.
  - *Neglected Stores:* Closing the store or ignoring orders → lost revenue.

### ⏱️ Order Tracking Method
The customer opens the live tracking panel to see the exact state of their food. Status data is pulled securely from the **Order API** and ensures the user knows what is happening:
- **Pending:** Waiting for the restaurant to accept.
- **Preparing:** The kitchen is currently cooking the food.
- **Ready for Pickup:** Awaiting a delivery rider.
- **Out for Delivery:** The rider has picked up the food and is en route.
- **Delivered:** The food has safely arrived.

---

## 🛒 Cart & Checkout Mechanics

- The cart is the user's tool to compile orders across the menu.
- **Cross-Restaurant Rules:** A single order must consist of items from **only one restaurant**. The system strictly blocks mixed-restaurant carts.
- **Financial Engine:** The system computes `(Price * Quantity) + Delivery Fee`.
- **Visuals:** The cart features real-time DOM updates, preventing the need for page reloads when modifying quantities.

---

## 📡 Live Notifications & Role Alerts

Data is displayed via customized dashboards tailored specifically to the logged-in user's role:

* **🧑‍🍳 Restaurant:** *"New Order #4812 — Accept & Start Preparing?"*
* **🛵 Rider:** *"Order #4812 is Ready for Pickup — Accept Delivery?"*
* **🛍️ Customer:** *"Your order is Out for Delivery!"*
* **👨‍💼 Admin:** *"New Restaurant Registration — Approve or Reject?"*

---

## 📋 User Role Guidelines

| Role | Likes | Hates | Core Responsibility | Dashboard Focus |
| :--- | :--- | :--- | :--- | :--- |
| **Customer** | Fast delivery, hot food | High fees, missing items | Placing orders & paying | Menus & Tracking |
| **Restaurant** | High volume, good reviews | Unassigned riders, cancellations | Cooking & dispatching | Order Queue & Menu |
| **Delivery Rider** | Short routes, quick pickups | Long waits at restaurants | Transporting food safely | Active Deliveries |
| **Administrator** | System stability, growth | Security breaches, exploits | Overseeing the platform | Metrics & Approvals |

---

## 🚀 Order Event Matrix

| Order State | Customer Action | Restaurant Action | Rider Action | Admin Action |
| :--- | :--- | :--- | :--- | :--- |
| **Pending** | Can cancel order | Must Accept/Reject | None | Can monitor |
| **Preparing** | Waits patiently | Cooks the food | None | Can monitor |
| **Ready** | Waits patiently | Packages the food | Claims the delivery | Can monitor |
| **Out for Delivery**| Tracks the rider | None | Drives to customer | Can monitor |
| **Delivered** | Eats & leaves review | Earns revenue | Earns delivery cut | Logs revenue |

---

## ⚖️ System Security & Threats

The backend provides balanced progression focusing on strict security choices, threat remediation, and data safety.

* 🔓 **IDOR Exploits:** Blocked. Users cannot fetch or modify orders, restaurants, or deliveries that do not belong to them.
* 💉 **XSS Attacks:** Blocked. All user-input strings (reviews, names) are sanitized via `Utils.escapeHTML`.
* 🔑 **JWT Secrets:** Hardened. Stored strictly in environment variables, never hardcoded.
* 📦 **Data Collisions:** Prevented. Order numbers use cryptographically secure UUID substrings.

---

## 🎨 UI/UX & Frontend Requirements

### 🗺️ Environment
- Seamless Bootstrap 5 layout with a consistent, mobile-responsive grid.
- Custom **Dark Mode** toggle overriding standard CSS variables.

### 🖥️ Main User Interface Elements
- **Navbar:** Dynamic state rendering (Login vs Profile vs Dashboard links).
- **Cards:** Standardized item and restaurant display cards with image fallbacks.
- **Badges:** Color-coded status indicators (e.g., Green for Delivered, Yellow for Preparing).
- **Forms:** Secure auth and checkout forms preventing default submission on error.

---

## 🚀 Future Scope

- Enhanced graphical fidelity with a Live Map (Leaflet.js) integration for real-time rider GPS tracking.
- Expanded payment gateways (Stripe / SSLCommerz / bKash).
- Complex high-efficiency recommendation engines for targeted food suggestions.
- **Supply & Demand Market Simulator:** Dynamically fluctuating delivery fees based on weather, time of day (surge pricing), and rider availability.

---

## 🛠️ Local Environment Deployment

The engine relies on Java 17 and requires a MySQL database alongside standard environment variables:
# 1. Set Secure Environment Variables (PowerShell)
$env:JWT_SECRET="your_long_random_jwt_secret_string"
$env:ADMIN_SECRET="your_admin_secret_key"
# 2. Build the Java Application
mvn clean package -DskipTests
# 3. Execute the Spring Boot Server
java -jar target/bitehub-1.0.0.jar
```bash

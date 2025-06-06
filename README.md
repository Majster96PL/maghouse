# 📦 MagHouse – Full-Scale CRM for Warehouse Management

**MagHouse** is a comprehensive backend CRM system built in **Java 17** using **Spring Boot**. It manages users, items, warehouse spaces, and deliveries. The system uses **JWT-based authentication**, **role-based access control**, and includes **unit/integration tests**. Designed to be production-ready, it also supports future modules like Order and Swagger documentation.

---

## 🧩 Modules

### 👤 User Module
- User registration and login
- JWT-based authentication and token handling
- Role-based access control (RBAC)

### 📦 Item Module
- Create, edit, delete items
- Automatically generate unique item codes
- Item categorization and metadata support

### 🏢 Warehouse Module
- Manage warehouse spaces by `spaceType`
- Support for 3 major city locations
- Scalable for complex warehouse operations

### 🚚 Delivery Module
- Create and manage delivery orders
- Track order status: `CREATED`, `IN_PROGRESS`, `DELIVERED`, `CANCELLED`
- Basic delivery lifecycle support

---

## 🔐 Security

- JWT authentication (stateless sessions)
- Role-based access control (Admin, User, etc.)
- Protected API endpoints using Spring Security
- Token-based login with secure authorization headers

---

## ⚙️ Tech Stack

| Component        | Technology                     |
|------------------|--------------------------------|
| Language         | Java 17                        |
| Framework        | Spring Boot                    |
| Build Tool       | Maven                          |
| Security         | Spring Security + JWT          |
| Persistence      | JPA (H2 (testing) / PostgrSQL) |
| Tests            | JUnit, Mockito                 |
| API              | REST                           |
| Documentation    | Swagger (planned)              |
| Dev Tools        | Lombok, Spring DevTools        |

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+

### Clone and Build

```bash
git clone https://github.com/Majster96PL/maghouse.git
cd maghouse
./mvnw clean install
```
## 🚀 Run the Application

```bash
./mvnw spring-boot:run
```
## Or run via the compiled JAR:
```bash
java -jar target/maghouse-1.0-SNAPSHOT.jar
```
## 🧪 Testing
### The project includes:

- Unit tests (logic and services)

- Integration tests (REST endpoints)

### Run all tests with:

```bash
./mvnw test
```

## 🌐 API Overview
### All modules expose endpoints through a RESTful API. Working on `localhost`

### Endpoints
- `PUT /auth/admin/update` - Update user

- `PUT /auth/admin/change/{id}` - Change role user

- `POST /auth/register` – Register new user

- `POST /auht/login` – Authenticate and get JWT

- `GET /auth/{id}` - Get User by ID

- `POST /auth/refresh` - Refresh JWT token

- `POST /auth/item/create` – Create new item

- `PUT /auth/item//update/{itemId}` – Update item

- `DELETE /auth/item/delete/{itemId}` - Delete item

- `POST /auth/warehouse/create` – Add a new warehouse

- `POST /auth/warehouse/assign-space-type/{itemId}` - Assign space type to item

- `POST /auth/warehouse/assign-location/{itemId}` - Assign warehouse location to item

- `PUT /auth/warehouse/update-location/{itemId}` - Update location

- `POST /auth/delivery/create` – Create a delivery order

- `POST /auth/delivery/update-delivery-status/{id}` - Update delivery status

### 📘 Full API documentation will be available via Swagger in a future release.

## 📌 Roadmap
- ✅ Modular architecture
- ✅ JWT security
- ✅ Unit & integration testing
- 🔜 Order module
- 🔜 Swagger/OpenAPI documentation
- 🔜 Docker & PostgreSQL support
- 🔜 CI/CD with GitHub Actions

## 🧱 Architecture
- Domain-driven modular structure
- Clean DTO-to-entity separation
- Centralized error handling
- Stateless authentication flow
- Prepared for containerization and cloud deployment



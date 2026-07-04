# 📦 MagHouse – Warehouse Management System

![Java](https://img.shields.io/badge/Java-22-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED)
![GitHub Actions](https://img.shields.io/badge/CI-GitHub_Actions-success)

A backend **Warehouse Management System** built with **Java 22**, **Spring Boot**, **Spring Security**, **PostgreSQL**, and **Docker**.

Developed as my engineering thesis, MagHouse demonstrates secure authentication, warehouse inventory management, warehouse space allocation, delivery processing, RESTful API development, automated testing and containerized deployment.

---

# ✨ Features

- JWT Authentication & Authorization
- Role-Based Access Control
- Warehouse Management
- Inventory Management
- Delivery Management
- RESTful API
- Swagger Documentation
- Docker Deployment
- Unit Tests (JUnit + Mockito)

---

# 🏗 Architecture

```text
Client
  │
REST Controllers
  │
Services
  │
Repositories
  │
PostgreSQL
```

---

# 🛠 Tech Stack

| Category | Technology |
|----------|------------|
| Language | Java 22 |
| Framework | Spring Boot |
| Security | Spring Security, JWT |
| ORM | Hibernate, Spring Data JPA |
| Database | PostgreSQL, H2 |
| Build | Maven |
| Containerization | Docker |
| Testing | JUnit 5, Mockito |
| Documentation | Swagger / OpenAPI |
| CI | GitHub Actions |

---

# 🚀 Getting Started

```bash
git clone https://github.com/Majster96PL/maghouse.git
```

Linux/macOS

```bash
DB_USER=your_user DB_PASSWORD=your_password docker compose up --build -d
```

Windows

```powershell
$env:DB_USER="your_user"
$env:DB_PASSWORD="your_password"
docker compose up --build -d
```

---

# 🧪 Running Tests

```bash
mvn clean test
```

---

# 🌐 API Overview

The application exposes REST endpoints for:
- Authentication
- User Management
- Inventory
- Warehouse
- Deliveries
- Administration

## API Reference

##### 
Public endpoints for user registration, login, and token management.

```http request
  POST maghouse/auth/register
```
**REQUEST**

| Parameter   | Type     | Description                       |
|:------------|:---------|:----------------------------------|
| `firstname` | `string` | **Required**. Your firstname      |
| `lastname`  | `string` | **Required**. Your lastname       |
| `email`     | `string` | **Required**. Your email          |
| `password`  | `string` | **Required**. Your password       |
| `role`      | `string` | **Required**. Default role (USER) |

**RESPONSE**

| Parameter       | Type     | Description                           |
|:----------------|:---------|:--------------------------------------|
| `access_token`  | `string` | **Required token for authorization**. |
| `refresh_token` | `string` | Refresh token.                        |

```http request
POST maghouse/auth/login
```
**REQUEST**

| Parameter   | Type       | Description                       |
|:------------|:-----------|:----------------------------------|
| `email`     | `string`   | **Required**. Your email          |
| `password`  | `string`   | **Required**. Your password       |

**RESPONSE**

| Parameter       | Type     | Description                           |
|:----------------|:---------|:--------------------------------------|
| `access_token`  | `string` | **Required token for authorization**. |
| `refresh_token` | `string` | Refresh token                         |  

```http request
POST maghouse/auth/refresh
```

<img width="672" height="234" alt="image" src="https://github.com/user-attachments/assets/06394e02-7892-48ac-8588-c048da521154" />


##### **Item Management (Need to login)**

Retrieve a list of all items.

```http request
GET maghouse/items/
```

Retrieve item details by item code.

```http request
GET /maghouse/items/{itemCode}
```
<img width="524" height="230" alt="image" src="https://github.com/user-attachments/assets/f91a269f-dc77-4b6d-8303-f8cac1e9651e" />


Create new item.

```http request
POST maghouse/items/
```
**REQUEST**

| Parameter  | Type      | Description                  |
|:-----------|:----------|:-----------------------------|
| `name`     | `string`  | **Required** Your item name. |
| `quantity` | `integer` | **Required** Your quantity.  |  

**RESPONSE**

| Parameter      | Type       | Description               |
|:---------------|:-----------|:--------------------------|
| `name`         | `string`   | Your item name.           |
| `itemCode`     | `string`   | Auto-generated item code. |
| `quantity`     | `string`   | Your quantity.            |
| `locationCode` | `string`   | `null`                    |
| `userId`       | `long`     | User id.                  |

Modifies the stock quantity for a specific item.

```http request
PUT maghouse/items/{itemId}
```
<img width="515" height="192" alt="image" src="https://github.com/user-attachments/assets/5f9b1a10-707d-41ae-9348-7d6cca7ad6b2" />


**REQUEST**

| Parameter  | Type      | Description                  |
|:-----------|:----------|:-----------------------------|
| `name`     | `string`  | **Required** Your item name. |
| `quantity` | `integer` | **Required** Your quantity.  |  

**RESPONSE**

| Parameter      | Type       | Description            |
|:---------------|:-----------|:-----------------------|
| `name`         | `string`   | Your item name.        |
| `itemCode`     | `string`   | Your item code.        |
| `quantity`     | `string`   | Your updated quantity. |
| `locationCode` | `string`   | `null`                 |
| `userId`       | `long`     | User id.               |

Removes an item permanently from the inventory.

```http request
DELETE maghouse/items/{item}
```
<img width="477" height="195" alt="image" src="https://github.com/user-attachments/assets/79f2380e-2f53-4926-bddf-569b5ae45790" />


<img width="1290" height="338" alt="image" src="https://github.com/user-attachments/assets/b228a81c-aeb0-4971-bd47-a23a2cece91e" />


##### **Warehouse Management (Need to login)**

Retrieved a list of a defined warehouse structures.

```http request
GET maghouse/warehouses/
```

Retrieves a list of items assigned to a warehouse location (Warsaw, Krakow, Rzeszow).

```http request
GET maghouse/warehouses/items/by-location/{warehouseLocationRequest}
```
**REQUEST**

| Parameter           | Type     | Description                                               |
|:--------------------|:---------|:----------------------------------------------------------|
| `warehouseLocation` | `string` | **Required** Warehouse Location (Warsaw, Krakow, Rzeszow) |

Create a new warehouse.

```http request
POST maghouse/warehouses/
```

**REQUEST**

| Parameter           | Type     | Description                                               |
|:--------------------|:---------|:----------------------------------------------------------|
| `warehouseLocation` | `string` | **Required** Warehouse Location (Warsaw, Krakow, Rzeszow) |

**RESPONSE**

| Parameter           | Type      | Description                                  |
|:--------------------|:----------|:---------------------------------------------|
| `warehouseLocation` | `string`  | Warehouse Location (Warsaw, Krakow, Rzeszow) |
| `userId`            | `integer` | User id.                                     |
| `itemsId`           | `integer` | Items id.                                    |

Assign a space type to an item.

```http request
POST maghouse/warehouses/assign-space-type/{itemId}
```
<img width="492" height="208" alt="image" src="https://github.com/user-attachments/assets/51508fcc-f315-498b-83c8-3e7823c3159b" />


**REQUEST**

| Parameter            | Type     | Description                                                  |
|:---------------------|:---------|:-------------------------------------------------------------|
| `warehouseSpaceType` | `string` | **Required** Warehouse Space Type (SHELF, DRAWER, CONTAINER) |

**RESPONSE**

| Parameter      | Type       | Description                        |
|:---------------|:-----------|:-----------------------------------|
| `name`         | `string`   | Your item name.                    |
| `itemCode`     | `string`   | Your item code.                    |
| `quantity`     | `string`   | Your  quantity.                    |
| `locationCode` | `string`   | Item location (e.g S01A for SHELF) |
| `userId`       | `long`     | User id.                           |

Assigns the item's current stock to a specific physical location in the warehouse.

```http request
POST maghouse/warehouses/assign-location/{itemId}
```

<img width="509" height="208" alt="image" src="https://github.com/user-attachments/assets/158f8b89-5f20-443d-abb3-87cf248227c6" />


**REQUEST**

| Parameter           | Type     | Description                                               |
|:--------------------|:---------|:----------------------------------------------------------|
| `warehouseLocation` | `string` | **Required** Warehouse Location (Warsaw, Krakow, Rzeszow) |

**RESPONSE**

| Parameter      | Type       | Description                          |
|:---------------|:-----------|:-------------------------------------|
| `name`         | `string`   | Your item name.                      |
| `itemCode`     | `string`   | Your item code.                      |
| `quantity`     | `string`   | Your  quantity.                      |
| `locationCode` | `string`   | Item location (e.g WS01A for Warsaw) |
| `userId`       | `long`     | User id.                             |

Moves an item's stock from its current location to a new one.

```http request
PUT maghouse/warehouses/items/{itemId}/location
```
<img width="487" height="189" alt="image" src="https://github.com/user-attachments/assets/53060da3-d28e-44e8-a47f-032afa05e0ae" />


**REQUEST**

| Parameter           | Type     | Description                                               |
|:--------------------|:---------|:----------------------------------------------------------|
| `warehouseLocation` | `string` | **Required** Warehouse Location (Warsaw, Krakow, Rzeszow) |

##### **Delivery Management (Need to login)**

Retrieves a list of all deliveries.

```http request
GET maghouse/deliveries/
```

Retrieves a specific delivery by its ID.

```http request
GET maghouse/deliveries/{id}
```
Retrieves deliveries filtered by supplier name.

```http request
GET maghouse/deliveries/supplier/{supplierName}
```

**REQUEST**

| Parameter      | Type     | Description                 |
|:---------------|:---------|:----------------------------|
| `supplierName` | `string` | **Required** Supplier name. |

Retrieves deliveries filtered by delivery status.

```http request
GET maghouse/deliveries/status/{status}
```

<img width="370" height="231" alt="image" src="https://github.com/user-attachments/assets/a1354eac-edd1-470c-a49d-989814a310e9" />


Retrieves a specific delivery by its unique delivery number.

```http request
GET maghouse/deliveries/number/{deliveryNumber}
```

**REQUEST**

| Parameter        | Type     | Description                                    |
|:-----------------|:---------|:-----------------------------------------------|
| `deliveryNumber` | `string` | **Required** Delivery number (e.g 145/05/2025) |

Retrieves deliveries filtered by warehouse location.

```http request
GET maghouse/deliveries/location/{warehouseLocation}
```

**REQUEST**

| Parameter           | Type     | Description                                               |
|:--------------------|:---------|:----------------------------------------------------------|
| `warehouseLocation` | `string` | **Required** Warehouse Location (Warsaw, Krakow, Rzeszow) |

Retrieves deliveries filtered by item code.

```http request
GET maghouse/deliveries/item/{itemCode}
```

**REQUEST**

| Parameter  | Type     | Description                                   |
|:-----------|:---------|:----------------------------------------------|
| `itemCode` | `string` | **Required** Item code (e.g 1234-05-895-5879) |

Creates a new delivery order based on the provided request data.

```http request
POST maghouse/deliveries/
```

**REQUEST**

| Parameter      | Type      | Description                                   |
|:---------------|:----------|:----------------------------------------------|
| `supplierName` | `string`  | **Required** Supplier name.                   |
| `itemName`     | `string`  | **Required** Item name.                       |
| `itemCode`     | `string`  | **Required** Item code (e.g 1234-05-895-5879) |
| `quantity`     | `integer` | **Required** Item quantity.                   |


**RESPONSE**

```json lines
{
  "supplier": "string",
  "data": "data",
  "numberDelivery": "string",
  "itemName": "string",
  "itemCode": "string",
  "quantity": integer,
  "deliveryStatus": "string",
  "warehouseLocation": "string",
  "userId": integer
}
```

Updates the status of a specific delivery by ID.

```http request
PUT maghouse/deliveries/{id}
```

<img width="478" height="377" alt="image" src="https://github.com/user-attachments/assets/4c03600a-6352-4a25-84b0-8dd1780430c2" />


##### **Admin Management (Need to login by admin account)**

**The admin account is generated during running applications**

**REQUEST FOR LOGIN**

```json lines
{
  "email": "admin@maghouse.pl",
  "password": "admin"
}
```
<img width="493" height="394" alt="image" src="https://github.com/user-attachments/assets/9797a9ce-01d5-41cc-8f9f-f995fa3dedc2" />


# 📖 Swagger

http://localhost:8080/maghouse/swagger-ui/index.html

---

# 🚀 Future Improvements

- Redis
- Kubernetes
- Testcontainers
- Monitoring
- CI/CD improvements

---

# 📄 License

MIT License.


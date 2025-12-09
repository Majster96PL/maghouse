# MagHouse

**Maghouse** is a comprehensive backend CRM solution designed for warehouse management. The Spring Boot appication provides RESTful APIs for user management, inventory tracking with warehouse space allocation, and delivery processing. The system features JWT-based authentication with role-based access control, ensuring secure operations.

---

## Features

- User Management
- Item Module
- Warehouse Management
- Delivery Module

---

## Tech Stack

| Component        | Technology                     |
|------------------|--------------------------------|
| Language         | Java 22                        |
| Framework        | Spring Boot                    |
| Build Tool       | Maven                          |
| Containerization | Docker                         |
| Security         | Spring Security + JWT          |
| Persistence      | JPA (H2 (testing) / PostgrSQL) |
| Tests            | JUnit, Mockito                 |
| API              | REST                           |
| Documentation    | Swagger                        |
| Dev Tools        | Lombok, Spring DevTools        |

---

## Getting Started

#### 1.

```bash
 git clone https://github.com/Majster96PL/maghouse.git
```
#### 2. 

###### Linux/Mac

```bash
 DB_USER=your_user DB_PASSWORD=your_passwod docker compose up --build -d
```
#### or

###### Windows

```shell
 $env:DB_USER="your_user"; $env:DB_PASSWORD="your_password"; docker compose up --build -d
```

#### Run all tests

```bash
 mvn clean test 
```
---

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

![img.png](img.png)

##### **Item Management (Need to login)**

Retrieve a list of all items.

```http request
GET maghouse/items/
```

Retrieve item details by item code.

```http request
GET /maghouse/items/{itemCode}
```
![img_1.png](img_1.png)

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
![img_2.png](img_2.png)

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
![img_3.png](img_3.png)

![img_4.png](img_4.png)

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
![img_5.png](img_5.png)

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

![img_6.png](img_6.png)

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
![img_7.png](img_7.png)

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

![img_8.png](img_8.png)

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

![img_9.png](img_9.png)

##### **Admin Management (Need to login by admin account)**

**The admin account is generated during running applications**

**REQUEST FOR LOGIN**

```json lines
{
  "email": "admin@maghouse.pl",
  "password": "admin"
}
```
![img_10.png](img_10.png)

## Documentation

[Local Documentation](http://localhost:8080/maghouse/swagger-ui/index.html#)


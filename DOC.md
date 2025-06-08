# API Documentation 
# I'll focus on core ENDPOINTS & FEATURES

## Prerequisites
- Java 17
- Docker and Docker Compose
- Gradle

## Running the API

1. **Starting the Database**
   ```bash
   docker compose up
   ```
   This will start PostgreSQL on port 5432.

2. **Running the Application**
   ```bash
   # For Linux/Mac
   ./gradlew bootRun

   # For Windows
   gradlew.bat bootRun
   ```
   The application will start on port 8081 with context path `/api`.

## API Endpoint Testing

### 1. Person Management

#### Create a Customer
```bash
POST /api/person
Content-Type: application/json

{
    "name": "Abdullah Customer",
    "email": "abdullah@example.com",
    "role": "CUSTOMER"
}
```
Sample Response:
```json
{
    "id": 1,
    "name": "Abdullah Customer",
    "email": "abdullah@example.com",
    "role": "CUSTOMER"
}
```

#### Create a Delivery Man
```bash
POST /api/person
Content-Type: application/json

{
    "name": "Zahed Driver",
    "email": "zahed@example.com",
    "registrationNumber": "DRV-001",
    "role": "DELIVERY_MAN"
}
```
Sample Response:
```json
{
    "id": 2,
    "name": "Zahed Driver",
    "email": "zahed@example.com",
    "registrationNumber": "DRV-001",
    "role": "DELIVERY_MAN"
}
```

### 2. Delivery Management

#### Create New Order
```bash
POST /api/delivery/order
Content-Type: application/json

{
    "orderId": "ORD-123",
    "price": 100,
    "customer": {
        "id": 1
    }
}
```
Sample Response:
```json
{
    "id": 1,
    "orderId": "ORD-123",
    "price": 100,
    "customer": {
        "id": 1,
        "name": "Abdullah Customer",
        "email": "abdullah@example.com",
        "role": "CUSTOMER"
    }
}
```

#### Complete a Delivery
```bash
POST /api/delivery/complete
Content-Type: application/json

{
    "orderId": "ORD-123",
    "deliveryMan": {
        "id": 2
    },
    "startTime": "2024-03-08T16:30:00Z",
    "endTime": "2024-03-08T17:00:00Z",
    "distance": 10,
    "price": 100
}
```
Sample Response:
```json
{
    "id": 1,
    "orderId": "ORD-123",
    "startTime": "2024-03-08T16:30:00Z",
    "endTime": "2024-03-08T17:00:00Z",
    "distance": 10,
    "price": 100,
    "commission": 55,
    "deliveryMan": {
        "id": 2,
        "name": "Zahed Driver",
        "email": "zahed@example.com",
        "registrationNumber": "DRV-001",
        "role": "DELIVERY_MAN"
    },
    "customer": {
        "id": 1,
        "name": "Abdullah Customer",
        "email": "abdullah@example.com",
        "role": "CUSTOMER"
    }
}
```

#### Get Delivery by ID
```bash
GET /api/delivery/{deliveryId}
```
Sample Response:
```json
{
    "id": 1,
    "orderId": "ORD-123",
    "startTime": "2025-06-08T16:30:00Z",
    "endTime": "2025-06-08T17:00:00Z",
    "distance": 10,
    "price": 100,
    "commission": 55,
    "deliveryMan": {
        "id": 2,
        "name": "Zahed Driver",
        "email": "zahed@example.com",
        "registrationNumber": "DRV-001",
        "role": "DELIVERY_MAN"
    },
    "customer": {
        "id": 1,
        "name": "Abdullah Customer",
        "email": "abdullah@example.com",
        "role": "CUSTOMER"
    }
}
```

#### Get Top 3 Delivery Men 
```bash
GET /api/delivery/top?startTime=2025-06-01T00:00:00Z&endTime=2025-07-01T23:59:59Z
```
Sample Response:
```json
[
    {
        "deliveryManId": 2,
        "deliveryManName": "Zahed Driver",
        "totalCommission": 255.00,
        "totalDeliveries": 10
    },
    {
        "deliveryManId": 4,
        "deliveryManName": "Redha Driver",
        "totalCommission": 135.00,
        "totalDeliveries": 8
    },
    {
        "deliveryManId": 6,
        "deliveryManName": "Kareem Driver",
        "totalCommission": 50.00,
        "totalDeliveries": 5
    }
]
```

## Features

### 1. Two-Phase Delivery Process
- First create an order using `/delivery/order`
- Then complete the delivery using `/delivery/complete`
- System validates concurrent deliveries
- Prevents overlapping delivery times for same delivery man

### 2. Top Delivery Men Ranking
- Shows highest earning delivery men
- Includes total commission and number of deliveries
- Filtered by time interval
- Returns top 3 performers

### 3. Delayed Delivery Monitoring
- System automatically checks for delayed deliveries every 30 seconds
- Notifies when a delivery exceeds 45 minutes
- Logs detailed information about delayed deliveries
- Runs asynchronously to maintain system performance

## Database Constraints
- Unique order IDs
- Unique email addresses
- Unique registration numbers for delivery men
- No concurrent deliveries for the same delivery man

## Error Handling
The application provides clear error messages for:
- Duplicate order IDs
- Concurrent delivery attempts
- Invalid time ranges
- Role validation errors
- Database constraint violations

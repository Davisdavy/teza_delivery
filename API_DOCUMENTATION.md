# Teza Platform API Documentation Guide

This document describes all REST API endpoints exposed by the Teza system, details authentication controls, request/response models, and role permissions.

---

## Authentication & Headers

Teza uses stateless **JWT (JSON Web Token)** Bearer authentication. 

* **Secured Endpoints Header**:
  ```http
  Authorization: Bearer <your_access_token>
  ```
* **Swagger UI Access**: Available locally at `http://localhost:8080/swagger-ui/index.html`. Use the green **Authorize** lock button in the top right to register your Bearer JWT token (without typing `Bearer` prefix) to test the secured routes.

---

## Table of Contents
1. [Authentication API (`/api/auth`)](#1-authentication-api-apiauth)
2. [User Accounts API (`/api/users`)](#2-user-accounts-api-apiusers)
3. [Merchant Profile API (`/api/merchant`)](#3-merchant-profile-api-apimerchant)
4. [Rider Profile & Location API (`/api/rider`)](#4-rider-profile-location-api-apirider)
5. [Delivery & Dispatch API (`/api/delivery`)](#5-delivery-dispatch-api-apidelivery)
6. [Notifications API (`/api/notifications`)](#6-notifications-api-apinotifications)

---

## 1. Authentication API (`/api/auth`)
*All endpoints under `/api/auth` are publicly accessible.*

### Register User
* **Method & Path**: `POST /api/auth/register`
* **Role/Scope**: Public (can register users with role `CUSTOMER` or `MERCHANT`).
* **Request Body**:
  ```json
  {
    "email": "customer@example.com",
    "password": "securepassword123",
    "role": "CUSTOMER"
  }
  ```
* **Response Body** (201 Created):
  ```json
  {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "d2e46e8f...",
    "expiresIn": 900
  }
  ```

### Login
* **Method & Path**: `POST /api/auth/login`
* **Request Body**:
  ```json
  {
    "email": "customer@example.com",
    "password": "securepassword123"
  }
  ```
* **Response Body** (200 OK): Same as `Register`.

### Refresh Token
* **Method & Path**: `POST /api/auth/refresh`
* **Request Body**:
  ```json
  {
    "refreshToken": "d2e46e8f-..."
  }
  ```
* **Response Body** (200 OK): Same as `Register` (provides rotated access + refresh token).

### Logout
* **Method & Path**: `POST /api/auth/logout`
* **Request Body**:
  ```json
  {
    "refreshToken": "d2e46e8f-..."
  }
  ```
* **Response** (204 No Content): Revokes/invalidates the refresh token in the database.

### Forgot Password
* **Method & Path**: `POST /api/auth/forgot-password`
* **Request Body**:
  ```json
  {
    "email": "customer@example.com"
  }
  ```
* **Response Body** (200 OK):
  ```json
  {
    "email": "customer@example.com",
    "resetToken": "5c92842e-1845-42a1-aa8e-4a69cbfcf156"
  }
  ```

### Reset Password
* **Method & Path**: `POST /api/auth/reset-password`
* **Request Body**:
  ```json
  {
    "token": "5c92842e-1845-42a1-aa8e-4a69cbfcf156",
    "newPassword": "newsecurepassword123"
  }
  ```
* **Response** (204 No Content)

---

## 2. User Accounts API (`/api/users`)
*Requires Bearer Token authentication.*

### Get My Account
* **Method & Path**: `GET /api/users/me`
* **Role/Scope**: Any authenticated user.
* **Response Body** (200 OK):
  ```json
  {
    "id": "a1b2c3d4-...",
    "email": "customer@example.com",
    "role": "CUSTOMER",
    "enabled": true
  }
  ```

### Update My Email
* **Method & Path**: `PUT /api/users/me`
* **Request Body**:
  ```json
  {
    "email": "updated@example.com"
  }
  ```
* **Response Body** (200 OK): Same as `Get My Account`.

### Change My Password
* **Method & Path**: `PUT /api/users/me/password`
* **Request Body**:
  ```json
  {
    "oldPassword": "securepassword123",
    "newPassword": "newsecurepassword123"
  }
  ```
* **Response** (204 No Content)

### List All Users
* **Method & Path**: `GET /api/users`
* **Role/Scope**: `ADMIN` role only.
* **Response Body** (200 OK): `List<UserResponse>`

### Get User Details
* **Method & Path**: `GET /api/users/{id}`
* **Role/Scope**: Account Owner or `ADMIN` role.
* **Response Body** (200 OK): Same as `Get My Account`.

### Update User details
* **Method & Path**: `PUT /api/users/{id}`
* **Role/Scope**: `ADMIN` role only.
* **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "enabled": false,
    "role": "RIDER"
  }
  ```
* **Response Body** (200 OK): Same as `Get My Account`.

### Delete User Account
* **Method & Path**: `DELETE /api/users/{id}`
* **Role/Scope**: Account Owner or `ADMIN` role.
* **Response** (204 No Content)

---

## 3. Merchant Profile API (`/api/merchant`)
*Requires Bearer Token authentication. Access restricted to roles MERCHANT and ADMIN.*

### Initialize Profile
* **Method & Path**: `POST /api/merchant/profile`
* **Request Body**:
  ```json
  {
    "businessName": "Good Eats Cafe",
    "phone": "+254712345678",
    "address": "123 Business St, Nairobi"
  }
  ```
* **Response Body** (201 Created):
  ```json
  {
    "id": "e5f6g7h8-...",
    "userId": "a1b2c3d4-...",
    "businessName": "Good Eats Cafe",
    "phone": "+254712345678",
    "address": "123 Business St, Nairobi",
    "createdAt": "2026-06-10T15:30:00Z",
    "updatedAt": "2026-06-10T15:30:00Z"
  }
  ```

### Get My Merchant Profile
* **Method & Path**: `GET /api/merchant/profile`
* **Response Body** (200 OK): Same as profile initialization.

### Get Profile By ID
* **Method & Path**: `GET /api/merchant/profile/{id}`
* **Role/Scope**: Profile Owner or `ADMIN` role.
* **Response Body** (200 OK): Same as profile initialization.

### Update Profile details
* **Method & Path**: `PUT /api/merchant/profile`
* **Request Body**:
  ```json
  {
    "businessName": "Better Eats Cafe",
    "phone": "+254711111111",
    "address": "456 Market St, Nairobi"
  }
  ```
* **Response Body** (200 OK): Same as profile initialization.

### Delete Profile
* **Method & Path**: `DELETE /api/merchant/profile/{id}`
* **Role/Scope**: Profile Owner or `ADMIN` role.
* **Response** (204 No Content)

---

## 4. Rider Profile & Location API (`/api/rider`)
*Requires Bearer Token authentication. Access restricted to roles RIDER and ADMIN.*

### Initialize Profile
* **Method & Path**: `POST /api/rider/profile`
* **Request Body**:
  ```json
  {
    "licenseNumber": "DL-987654321",
    "vehicleType": "MOTORCYCLE"
  }
  ```
* **Response Body** (201 Created):
  ```json
  {
    "id": "r1i2d3e4-...",
    "userId": "a1b2c3d4-...",
    "licenseNumber": "DL-987654321",
    "vehicleType": "MOTORCYCLE",
    "available": false,
    "onboardingStatus": "PENDING",
    "createdAt": "2026-06-10T15:30:00Z",
    "updatedAt": "2026-06-10T15:30:00Z"
  }
  ```

### Get My Profile
* **Method & Path**: `GET /api/rider/profile`
* **Response Body** (200 OK): Same as profile initialization.

### Get Profile By ID
* **Method & Path**: `GET /api/rider/profile/{id}`
* **Role/Scope**: Profile Owner or `ADMIN` role.
* **Response Body** (200 OK): Same as profile initialization.

### Update Profile
* **Method & Path**: `PUT /api/rider/profile`
* **Role/Scope**: RIDER role.
* **Request Body**:
  ```json
  {
    "vehicleType": "BICYCLE",
    "available": true
  }
  ```
  *(Note: Any attempt to modify `onboardingStatus` here returns `403 Forbidden` — onboarding changes must be made by an Admin).*
* **Response Body** (200 OK): Same as profile initialization.

### Update Rider Onboarding Status (Admin Only)
* **Method & Path**: `PUT /api/rider/profile/{id}/onboarding`
* **Role/Scope**: `ADMIN` role only.
* **Request Body**:
  ```json
  {
    "onboardingStatus": "APPROVED"
  }
  ```
  *(Allowed values: `PENDING`, `APPROVED`, `REJECTED`, `SUSPENDED`)*
* **Response Body** (200 OK): Same as profile initialization.

### Delete Profile
* **Method & Path**: `DELETE /api/rider/profile/{id}`
* **Role/Scope**: Profile Owner or `ADMIN` role.
* **Response** (204 No Content)

### Update Coordinates / Location
* **Method & Path**: `PUT /api/rider/location`
* **Request Body**:
  ```json
  {
    "latitude": -1.2921,
    "longitude": 36.8219
  }
  ```
* **Response Body** (200 OK):
  ```json
  {
    "riderProfileId": "r1i2d3e4-...",
    "latitude": -1.2921,
    "longitude": 36.8219,
    "updatedAt": "2026-06-10T15:31:00Z"
  }
  ```

### Get Current Location Coordinates
* **Method & Path**: `GET /api/rider/location`
* **Response Body** (200 OK): Same as location update response.

---

## 5. Delivery & Dispatch API (`/api/delivery`)
*Requires Bearer Token authentication.*

### Create Delivery
* **Method & Path**: `POST /api/delivery`
* **Role/Scope**: `MERCHANT`, `CUSTOMER` or `ADMIN`.
* **Request Body**:
  ```json
  {
    "pickupAddress": "123 Business St, Nairobi",
    "pickupLatitude": -1.2921,
    "pickupLongitude": 36.8219,
    "dropoffAddress": "789 Residential Ave, Nairobi",
    "dropoffLatitude": -1.3005,
    "dropoffLongitude": 36.8900
  }
  ```
* **Response Body** (201 Created):
  ```json
  {
    "id": "d1e2l3i4-...",
    "merchantId": "e5f6g7h8-...",
    "customerId": null,
    "riderId": null,
    "pickupAddress": "123 Business St, Nairobi",
    "pickupLatitude": -1.2921,
    "pickupLongitude": 36.8219,
    "dropoffAddress": "789 Residential Ave, Nairobi",
    "dropoffLatitude": -1.3005,
    "dropoffLongitude": 36.8900,
    "status": "PENDING",
    "acceptedAt": null,
    "pickedUpAt": null,
    "deliveredAt": null,
    "cancelledAt": null,
    "createdAt": "2026-06-10T15:30:00Z",
    "updatedAt": "2026-06-10T15:30:00Z"
  }
  ```
  *(Note: If a customer requests a delivery, `customerId` is populated and `merchantId` is null).*

### Get Delivery Details
* **Method & Path**: `GET /api/delivery/{id}`
* **Role/Scope**: Order Owner (Merchant or Customer), Assigned Rider, or `ADMIN` role.
* **Response Body** (200 OK): Same as creation response.

### List Merchant Deliveries
* **Method & Path**: `GET /api/delivery/merchant`
* **Role/Scope**: `MERCHANT` role.
* **Response Body** (200 OK): `List<DeliveryResponse>` placed by the logged-in merchant.

### List Customer Deliveries
* **Method & Path**: `GET /api/delivery/customer`
* **Role/Scope**: `CUSTOMER` role.
* **Response Body** (200 OK): `List<DeliveryResponse>` placed by the logged-in customer.

### List Rider Deliveries
* **Method & Path**: `GET /api/delivery/rider`
* **Role/Scope**: `RIDER` role.
* **Response Body** (200 OK): `List<DeliveryResponse>` assigned to the logged-in rider.

### Update Delivery Request
* **Method & Path**: `PUT /api/delivery/{id}`
* **Role/Scope**: Delivery Owner. Valid only in state `PENDING` or `SEARCHING`.
* **Request Body**:
  ```json
  {
    "pickupAddress": "New Pickup Location 123",
    "pickupLatitude": -1.2900,
    "pickupLongitude": 36.8200,
    "dropoffAddress": "New Dropoff Location 456",
    "dropoffLatitude": -1.3010,
    "dropoffLongitude": 36.8910
  }
  ```
* **Response Body** (200 OK): Same as creation response.

### Update Delivery Status (State Transition)
* **Method & Path**: `PUT /api/delivery/{id}/status`
* **Role/Scope**: Owner (Customer/Merchant), Assigned Rider, or `ADMIN` role.
* **Request Body**:
  ```json
  {
    "status": "SEARCHING"
  }
  ```
* **Lifecycle Rules**:
  * `PENDING` -> `SEARCHING` (Owner triggers search)
  * `ASSIGNED` -> `ARRIVED` (Rider arrived at pickup)
  * `ARRIVED` -> `PICKED_UP` (Rider picked up package)
  * `PICKED_UP` -> `IN_TRANSIT` (Rider is transporting package)
  * `IN_TRANSIT` -> `DELIVERED` (Rider delivered package)
  * Any state before completion -> `CANCELLED` (Owner or Admin cancels)
* **Response Body** (200 OK): Same as creation response (includes transit timestamps like `acceptedAt`, `pickedUpAt`, etc.).

### Delete/Cancel Delivery
* **Method & Path**: `DELETE /api/delivery/{id}`
* **Role/Scope**: Owner or `ADMIN` role.
* **Response** (204 No Content)

### Create Delivery Offer
* **Method & Path**: `POST /api/delivery/{id}/offers`
* **Role/Scope**: `ADMIN` role.
* **Request Body**:
  ```json
  {
    "riderProfileId": "r1i2d3e4-...",
    "durationSeconds": 30
  }
  ```
* **Response Body** (201 Created):
  ```json
  {
    "id": "o9p0q1r2-...",
    "deliveryId": "d1e2l3i4-...",
    "riderProfileId": "r1i2d3e4-...",
    "status": "PENDING",
    "expiresAt": "2026-06-10T15:30:30Z",
    "createdAt": "2026-06-10T15:30:00Z"
  }
  ```

### Respond to Offer (Accept/Decline)
* **Method & Path**: `PUT /api/delivery/offers/{offerId}/respond`
* **Role/Scope**: Target Rider.
* **Request Body**:
  ```json
  {
    "status": "ACCEPTED"
  }
  ```
  *(Value can be `ACCEPTED` or `DECLINED`)*
* **Response Body** (200 OK): Same as Offer response payload. (Acceptance automatically binds the rider to the delivery request and transitions delivery to `ASSIGNED`).

### List Offers for a Delivery
* **Method & Path**: `GET /api/delivery/{id}/offers`
* **Role/Scope**: Delivery Owner, Target Rider, or `ADMIN`.
* **Response Body** (200 OK): `List<DeliveryOfferResponse>`

### Get Status History Log
* **Method & Path**: `GET /api/delivery/{id}/history`
* **Role/Scope**: Delivery Owner, Assigned Rider, or `ADMIN`.
* **Response Body** (200 OK):
  ```json
  [
    {
      "id": "h1i2j3k4-...",
      "deliveryId": "d1e2l3i4-...",
      "status": "PENDING",
      "changedBy": "a1b2c3d4-...",
      "changedAt": "2026-06-10T15:30:00Z"
    },
    {
      "id": "h5i6j7k8-...",
      "deliveryId": "d1e2l3i4-...",
      "status": "SEARCHING",
      "changedBy": "a1b2c3d4-...",
      "changedAt": "2026-06-10T15:31:00Z"
    }
  ]
  ```

---

## 6. Notifications API (`/api/notifications`)
*Requires Bearer Token authentication.*

### Get My Notifications
* **Method & Path**: `GET /api/notifications`
* **Response Body** (200 OK):
  ```json
  [
    {
      "id": "n1o2p3q4-...",
      "userId": "a1b2c3d4-...",
      "title": "Rider Assigned",
      "message": "A rider has accepted your delivery order from Pickup 1 to Dropoff 1.",
      "status": "UNREAD",
      "createdAt": "2026-06-10T15:32:00Z"
    }
  ]
  ```

### Mark Notification As Read
* **Method & Path**: `PUT /api/notifications/{id}/read`
* **Role/Scope**: Notification Owner.
* **Response** (204 No Content)

### Mark All My Notifications As Read
* **Method & Path**: `PUT /api/notifications/read-all`
* **Response** (204 No Content)

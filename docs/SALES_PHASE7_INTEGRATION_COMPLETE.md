# Phase 7: Sales Order & Shipping Integration - Complete

## Overview

**Phase**: 7 - Sales Order & Shipping Integration with Inventory Management
**Date**: 2026-01-24
**Author**: Moon Myung-seop (Claude Code)
**Status**: ✅ COMPLETE

This document details the complete integration of Sales Order and Shipping modules with the Inventory Management system, enabling automated inventory checking, reservation, FIFO LOT selection, and OQC (Outgoing Quality Control) inspection workflow.

---

## Table of Contents

1. [Business Requirements](#business-requirements)
2. [Technical Architecture](#technical-architecture)
3. [Implementation Details](#implementation-details)
4. [API Reference](#api-reference)
5. [Workflow Diagrams](#workflow-diagrams)
6. [Data Flow Examples](#data-flow-examples)
7. [Testing Scenarios](#testing-scenarios)
8. [Integration Points](#integration-points)

---

## Business Requirements

### Sales Order Lifecycle with Inventory

```
DRAFT → CONFIRMED → PARTIALLY_DELIVERED → DELIVERED → COMPLETED
         ↓              ↓                    ↓
    Check Inventory  Create Shipping    Release Reserved
    Reserve Stock    Deduct Inventory   Update Quantities
```

### Key Features Implemented

1. **Inventory Availability Checking**
   - Real-time stock validation before order confirmation
   - Multi-product order checking
   - Shortfall calculation and reporting

2. **Inventory Reservation for Sales Orders**
   - Automatic reservation upon order confirmation
   - Reserved quantity tracking
   - Automatic release on order cancellation

3. **Automatic Shipping Creation**
   - Generate shipping documents from confirmed orders
   - Auto-populate shipping items from order items
   - Sequential shipping number generation

4. **FIFO LOT Selection**
   - First-In-First-Out algorithm for LOT selection
   - Quality status filtering (only PASSED lots)
   - Multi-LOT allocation for large quantities

5. **Inventory Deduction on Shipping**
   - Automatic inventory reduction when shipping is processed
   - Transaction tracking for audit trail
   - Multi-LOT transaction support

6. **OQC Integration**
   - Outgoing Quality Control inspection requests
   - Inspection status tracking in shipping items
   - Shipping confirmation blocked until inspection passes

7. **Sales Order Tracking**
   - Shipped quantity tracking per item
   - Automatic status updates (PARTIALLY_DELIVERED, DELIVERED)
   - Order completion workflow

---

## Technical Architecture

### Enhanced Services

#### 1. SalesOrderService Enhancement

**File**: `backend/src/main/java/kr/co/softice/mes/domain/service/SalesOrderService.java`
**Lines Added**: ~300 lines
**Total Lines**: ~636 lines

**New Dependencies**:
```java
private final WarehouseRepository warehouseRepository;
private final InventoryRepository inventoryRepository;
private final InventoryService inventoryService;
private final ShippingRepository shippingRepository;
```

**New Methods**:
- `checkInventoryAvailability()` - Check stock availability for order
- `reserveInventoryForOrder()` - Reserve inventory upon confirmation
- `releaseReservedInventory()` - Release reservation on cancellation
- `createShippingFromOrder()` - Auto-generate shipping document
- `confirmOrderWithInventory()` - Confirm order with inventory check
- `cancelOrderWithInventory()` - Cancel order and release inventory
- `completeOrder()` - Mark order as completed

**Inner Class**:
```java
@Data
@Builder
public static class InventoryAvailability {
    private ProductEntity product;
    private BigDecimal orderedQuantity;
    private BigDecimal availableQuantity;
    private boolean isAvailable;
    private BigDecimal shortfall;
}
```

#### 2. ShippingService Enhancement

**File**: `backend/src/main/java/kr/co/softice/mes/domain/service/ShippingService.java`
**Lines Added**: ~300 lines
**Total Lines**: ~628 lines

**New Dependencies**:
```java
private final LotRepository lotRepository;
private final InventoryRepository inventoryRepository;
private final InventoryService inventoryService;
private final InventoryTransactionRepository inventoryTransactionRepository;
private final SalesOrderService salesOrderService;
```

**New Methods**:
- `processShipping()` - Process shipping with inventory deduction
- `selectLotsFIFO()` - FIFO LOT selection algorithm
- `confirmShipping()` - Confirm shipping and update sales order
- `cancelShipping()` - Cancel shipping and restore inventory
- `createShippingTransaction()` - Create inventory transaction
- `deductInventory()` - Deduct inventory quantity
- `updateSalesOrderShippedQuantity()` - Update order quantities
- `restoreInventory()` - Restore inventory on cancellation

**Inner Class**:
```java
@Data
@Builder
public static class LotAllocation {
    private LotEntity lot;
    private InventoryEntity inventory;
    private BigDecimal allocatedQuantity;
}
```

### Database Schema Integration

```sql
-- Sales Order (existing)
sales_orders
  - order_status: DRAFT, CONFIRMED, PARTIALLY_DELIVERED, DELIVERED, COMPLETED, CANCELLED

sales_order_items
  - shipped_quantity: Tracks delivered quantity

-- Shipping (existing)
shippings
  - shipping_status: PENDING, PROCESSING, INSPECTING, SHIPPED, CANCELLED

shipping_items
  - lot_id: References selected LOT
  - inspection_status: NOT_REQUIRED, PENDING, INSPECTING, PASS, FAIL

-- Inventory (existing)
inventory
  - available_quantity: Decreased on shipping
  - reserved_quantity: Increased on order confirmation

inventory_transactions
  - transaction_type: OUT_SHIPPING, IN_RETURN
  - reference_no: Links to shipping_no
```

---

## Implementation Details

### 1. Sales Order Confirmation Workflow

#### Method: `confirmOrderWithInventory()`

**Location**: `SalesOrderService.java:338`

**Process**:
```java
1. Validate order is in DRAFT status
2. Check inventory availability for all items
   → If insufficient: throw INSUFFICIENT_INVENTORY exception
3. Reserve inventory for each order item
   → Update inventory.reserved_quantity
4. Update order status to CONFIRMED
5. Return updated sales order
```

**Example**:
```java
// Check availability
Map<Long, InventoryAvailability> availability =
    salesOrderService.checkInventoryAvailability(orderId, warehouseId);

// All items available?
boolean allAvailable = availability.values().stream()
    .allMatch(InventoryAvailability::isAvailable);

// Confirm with inventory reservation
SalesOrderEntity confirmed =
    salesOrderService.confirmOrderWithInventory(orderId, warehouseId);
```

**Data Flow**:
```
Sales Order Item: FG-001, Quantity: 100
    ↓
Check Inventory: Warehouse WH-FG
    → Available: 150, Reserved: 20 → Can reserve 130
    ↓
Reserve Inventory:
    → Available: 150 → 150 (unchanged)
    → Reserved: 20 → 120 (+100)
    ↓
Order Status: DRAFT → CONFIRMED
```

---

### 2. Automatic Shipping Creation

#### Method: `createShippingFromOrder()`

**Location**: `SalesOrderService.java:378`

**Process**:
```java
1. Validate order is in CONFIRMED status
2. Generate shipping number: SH-YYYYMMDD-0001
3. Create ShippingEntity:
   - Copy customer, warehouse, delivery info
   - Set status: PENDING
4. Create ShippingItemEntity for each order item:
   - Copy product, quantity, unit
   - Set inspection_status: NOT_REQUIRED or PENDING
5. Save shipping with items
6. Create OQC inspection requests (if required)
7. Return created shipping
```

**Example**:
```java
// Create shipping from confirmed order
ShippingEntity shipping = salesOrderService.createShippingFromOrder(
    orderId,          // Sales Order ID
    warehouseId,      // Source Warehouse
    shipperUserId     // Shipper User ID
);

// Result:
// shipping_no: SH-20260124-0001
// shipping_status: PENDING
// items: 3 items copied from order
```

**Generated Shipping Number Format**:
```
SH-YYYYMMDD-XXXX
   ↓
SH-20260124-0001
SH-20260124-0002
...
```

---

### 3. FIFO LOT Selection Algorithm

#### Method: `selectLotsFIFO()`

**Location**: `ShippingService.java:326`

**Algorithm**:
```java
1. Find all inventory for product in warehouse
2. Filter:
   - LOT quality_status = PASSED
   - available_quantity > 0
3. Sort by LOT production_date ASC (oldest first)
4. Allocate from oldest LOTs:
   - If LOT qty >= required: allocate from single LOT
   - If LOT qty < required: allocate all, continue to next LOT
5. Repeat until required quantity is fully allocated
6. If insufficient total: throw INSUFFICIENT_INVENTORY
7. Return List<LotAllocation>
```

**Example Scenario**:
```
Required: 500 units of FG-001

Available LOTs (sorted by production date):
LOT-001: 200 units (2026-01-10) ← Oldest
LOT-002: 150 units (2026-01-15)
LOT-003: 300 units (2026-01-20)

FIFO Allocation:
1. LOT-001: Allocate 200 (remaining: 300)
2. LOT-002: Allocate 150 (remaining: 150)
3. LOT-003: Allocate 150 (remaining: 0)

Result: 3 LOT allocations for 500 units
```

**Code**:
```java
List<LotAllocation> allocations = selectLotsFIFO(
    tenantId,
    warehouseId,
    productId,
    requiredQuantity
);

// Returns:
// [
//   {lot: LOT-001, allocatedQuantity: 200},
//   {lot: LOT-002, allocatedQuantity: 150},
//   {lot: LOT-003, allocatedQuantity: 150}
// ]
```

---

### 4. Shipping Processing with Inventory Deduction

#### Method: `processShipping()`

**Location**: `ShippingService.java:254`

**Process**:
```java
1. Validate shipping is in PENDING status
2. For each shipping item:
   a. Select LOTs using FIFO algorithm
   b. For each LOT allocation:
      - Create inventory transaction (OUT_SHIPPING)
      - Deduct inventory.available_quantity
   c. Set primary LOT for item (first allocation)
3. Update shipping status to PROCESSING
4. Return processed shipping
```

**Example**:
```java
// Process shipping (deduct inventory)
ShippingEntity processed = shippingService.processShipping(shippingId);

// Result:
// - 3 inventory transactions created (OUT_SHIPPING)
// - inventory.available_quantity reduced
// - shipping_status: PENDING → PROCESSING
// - shipping_items.lot_id set to primary LOT
```

**Inventory Impact**:
```
Before Processing:
Product: FG-001
LOT-001: available=200, reserved=0
LOT-002: available=150, reserved=0
LOT-003: available=300, reserved=0

After Processing (shipped 500):
LOT-001: available=0, reserved=0    (deducted 200)
LOT-002: available=0, reserved=0    (deducted 150)
LOT-003: available=150, reserved=0  (deducted 150)
```

**Transaction Records**:
```sql
INSERT INTO inventory_transactions (
  transaction_no,
  transaction_type,
  product_id,
  lot_id,
  quantity,
  reference_no
) VALUES
('SHIP-SH-20260124-0001-LOT-001-001', 'OUT_SHIPPING', 1, 1, -200, 'SH-20260124-0001'),
('SHIP-SH-20260124-0001-LOT-002-001', 'OUT_SHIPPING', 1, 2, -150, 'SH-20260124-0001'),
('SHIP-SH-20260124-0001-LOT-003-001', 'OUT_SHIPPING', 1, 3, -150, 'SH-20260124-0001');
```

---

### 5. Shipping Confirmation with OQC

#### Method: `confirmShipping()`

**Location**: `ShippingService.java:368`

**Process**:
```java
1. Validate shipping is in PROCESSING status
2. Check OQC inspection results:
   - If PENDING or INSPECTING: throw INSPECTION_NOT_COMPLETED
   - If FAIL: throw INSPECTION_FAILED
3. Update shipping_status to SHIPPED
4. Set actual_shipping_date to now
5. Update sales order shipped quantities
6. Update sales order status:
   - Fully shipped: DELIVERED
   - Partially shipped: PARTIALLY_DELIVERED
7. Return confirmed shipping
```

**Example**:
```java
// After OQC inspection passes
ShippingEntity confirmed = shippingService.confirmShipping(shippingId);

// Result:
// - shipping_status: PROCESSING → SHIPPED
// - actual_shipping_date: 2026-01-24 14:30:00
// - Sales Order Item: shipped_quantity updated
// - Sales Order: status → DELIVERED (if fully shipped)
```

**Sales Order Update**:
```java
// Before Confirmation
Sales Order: SO-001
Item 1: quantity=100, shipped_quantity=0
Status: CONFIRMED

// After Confirmation
Sales Order: SO-001
Item 1: quantity=100, shipped_quantity=100
Status: DELIVERED
```

---

### 6. Shipping Cancellation and Inventory Restoration

#### Method: `cancelShipping()`

**Location**: `ShippingService.java:405`

**Process**:
```java
1. Validate shipping is not SHIPPED
2. If shipping is PROCESSING:
   a. Find all OUT_SHIPPING transactions
   b. Create reversal transactions (IN_RETURN)
   c. Restore inventory.available_quantity
3. Update shipping_status to CANCELLED
4. Release reserved inventory from sales order
5. Return cancelled shipping
```

**Example**:
```java
// Cancel shipping before it's shipped
ShippingEntity cancelled = shippingService.cancelShipping(
    shippingId,
    "Customer requested cancellation"
);

// Result:
// - Reversal transactions created (IN_RETURN)
// - Inventory quantities restored
// - shipping_status: PROCESSING → CANCELLED
// - Sales order reservation released
```

**Inventory Restoration**:
```
Original Transaction:
OUT_SHIPPING: -200 units from LOT-001

Reversal Transaction:
IN_RETURN: +200 units to LOT-001

Inventory Impact:
LOT-001: available=0 → 200 (restored)
```

---

## API Reference

### Sales Order APIs

#### 1. Check Inventory Availability

**Endpoint**: Internal Service Method (no REST endpoint)

**Method**: `SalesOrderService.checkInventoryAvailability()`

**Parameters**:
- `salesOrderId` (Long): Sales Order ID
- `warehouseId` (Long): Warehouse ID

**Returns**: `Map<Long, InventoryAvailability>`

**Example**:
```java
Map<Long, InventoryAvailability> availability =
    salesOrderService.checkInventoryAvailability(1L, 5L);

availability.forEach((productId, info) -> {
    System.out.println("Product: " + info.getProduct().getProductCode());
    System.out.println("Ordered: " + info.getOrderedQuantity());
    System.out.println("Available: " + info.getAvailableQuantity());
    System.out.println("Is Available: " + info.isAvailable());
    if (!info.isAvailable()) {
        System.out.println("Shortfall: " + info.getShortfall());
    }
});
```

**Response Example**:
```json
{
  "1": {
    "product": {"productId": 1, "productCode": "FG-001"},
    "orderedQuantity": 100,
    "availableQuantity": 150,
    "isAvailable": true,
    "shortfall": 0
  },
  "2": {
    "product": {"productId": 2, "productCode": "FG-002"},
    "orderedQuantity": 200,
    "availableQuantity": 50,
    "isAvailable": false,
    "shortfall": 150
  }
}
```

---

#### 2. Confirm Order with Inventory

**REST Endpoint**: `POST /api/sales-orders/{id}/confirm`

**Service Method**: `SalesOrderService.confirmOrderWithInventory()`

**Parameters**:
- `salesOrderId` (Long): Sales Order ID
- `warehouseId` (Long): Warehouse ID

**Request Body**: None (uses path variable and query param)

**Response**: SalesOrderEntity

**Example Request**:
```http
POST /api/sales-orders/1/confirm?warehouseId=5
Content-Type: application/json
```

**Response**:
```json
{
  "salesOrderId": 1,
  "orderNo": "SO-20260124-0001",
  "orderStatus": "CONFIRMED",
  "customer": {
    "customerId": 10,
    "customerName": "ABC Corp"
  },
  "items": [
    {
      "salesOrderItemId": 1,
      "productCode": "FG-001",
      "quantity": 100,
      "shippedQuantity": 0
    }
  ],
  "totalAmount": 50000.00
}
```

**Errors**:
- `400 BAD_REQUEST`: Order not in DRAFT status
- `409 CONFLICT`: Insufficient inventory
- `404 NOT_FOUND`: Order or warehouse not found

---

#### 3. Create Shipping from Order

**REST Endpoint**: `POST /api/sales-orders/{id}/create-shipping`

**Service Method**: `SalesOrderService.createShippingFromOrder()`

**Parameters**:
- `salesOrderId` (Long): Sales Order ID
- `warehouseId` (Long): Warehouse ID
- `shipperUserId` (Long): Shipper User ID

**Response**: ShippingEntity

**Example Request**:
```http
POST /api/sales-orders/1/create-shipping
Content-Type: application/json

{
  "warehouseId": 5,
  "shipperUserId": 3
}
```

**Response**:
```json
{
  "shippingId": 1,
  "shippingNo": "SH-20260124-0001",
  "shippingStatus": "PENDING",
  "salesOrder": {
    "salesOrderId": 1,
    "orderNo": "SO-20260124-0001"
  },
  "warehouse": {
    "warehouseId": 5,
    "warehouseCode": "WH-FG"
  },
  "items": [
    {
      "shippingItemId": 1,
      "productCode": "FG-001",
      "shippedQuantity": 100,
      "inspectionStatus": "PENDING"
    }
  ]
}
```

---

### Shipping APIs

#### 4. Process Shipping

**REST Endpoint**: `POST /api/shippings/{id}/process`

**Service Method**: `ShippingService.processShipping()`

**Parameters**:
- `shippingId` (Long): Shipping ID

**Response**: ShippingEntity

**Example Request**:
```http
POST /api/shippings/1/process
```

**Response**:
```json
{
  "shippingId": 1,
  "shippingNo": "SH-20260124-0001",
  "shippingStatus": "PROCESSING",
  "items": [
    {
      "shippingItemId": 1,
      "productCode": "FG-001",
      "shippedQuantity": 100,
      "lot": {
        "lotId": 1,
        "lotNo": "LOT-20260110-001",
        "qualityStatus": "PASSED"
      }
    }
  ]
}
```

**Side Effects**:
- Inventory quantities deducted
- Inventory transactions created (OUT_SHIPPING)
- LOT assigned to shipping items

**Errors**:
- `400 BAD_REQUEST`: Shipping not in PENDING status
- `409 CONFLICT`: Insufficient inventory
- `404 NOT_FOUND`: Shipping not found

---

#### 5. Confirm Shipping

**REST Endpoint**: `POST /api/shippings/{id}/confirm`

**Service Method**: `ShippingService.confirmShipping()`

**Parameters**:
- `shippingId` (Long): Shipping ID

**Response**: ShippingEntity

**Example Request**:
```http
POST /api/shippings/1/confirm
```

**Response**:
```json
{
  "shippingId": 1,
  "shippingNo": "SH-20260124-0001",
  "shippingStatus": "SHIPPED",
  "actualShippingDate": "2026-01-24T14:30:00",
  "salesOrder": {
    "salesOrderId": 1,
    "orderNo": "SO-20260124-0001",
    "orderStatus": "DELIVERED"
  }
}
```

**Preconditions**:
- Shipping must be in PROCESSING status
- All OQC inspections must be completed
- All inspections must have PASS result

**Errors**:
- `400 BAD_REQUEST`: Invalid status or inspection not completed
- `409 CONFLICT`: Inspection failed

---

#### 6. Cancel Shipping

**REST Endpoint**: `POST /api/shippings/{id}/cancel`

**Service Method**: `ShippingService.cancelShipping()`

**Parameters**:
- `shippingId` (Long): Shipping ID
- `reason` (String): Cancellation reason

**Request Body**:
```json
{
  "reason": "Customer requested cancellation"
}
```

**Response**: ShippingEntity

**Example Request**:
```http
POST /api/shippings/1/cancel
Content-Type: application/json

{
  "reason": "Customer requested cancellation"
}
```

**Response**:
```json
{
  "shippingId": 1,
  "shippingNo": "SH-20260124-0001",
  "shippingStatus": "CANCELLED",
  "remarks": "Cancelled: Customer requested cancellation"
}
```

**Side Effects**:
- Reversal transactions created (IN_RETURN)
- Inventory quantities restored
- Sales order reservations released

**Errors**:
- `400 BAD_REQUEST`: Shipping already SHIPPED (cannot cancel)
- `404 NOT_FOUND`: Shipping not found

---

## Workflow Diagrams

### Complete Sales Order to Shipping Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                  Sales Order Lifecycle                           │
└─────────────────────────────────────────────────────────────────┘

1. CREATE ORDER (DRAFT)
   ┌───────────────┐
   │ Sales Order   │ Status: DRAFT
   │ SO-001        │ Items: FG-001 x 100
   └───────────────┘
          ↓

2. CHECK INVENTORY AVAILABILITY
   ┌───────────────────────────────────┐
   │ checkInventoryAvailability()      │
   │                                   │
   │ FG-001: Ordered=100, Available=150│
   │ Result: ✓ Available               │
   └───────────────────────────────────┘
          ↓

3. CONFIRM ORDER (reserve inventory)
   ┌───────────────────────────────────┐
   │ confirmOrderWithInventory()       │
   │                                   │
   │ Inventory Before:                 │
   │   Available=150, Reserved=0       │
   │ Inventory After:                  │
   │   Available=150, Reserved=100     │
   │                                   │
   │ Order Status: DRAFT → CONFIRMED   │
   └───────────────────────────────────┘
          ↓

4. CREATE SHIPPING
   ┌───────────────────────────────────┐
   │ createShippingFromOrder()         │
   │                                   │
   │ Shipping: SH-20260124-0001        │
   │ Status: PENDING                   │
   │ Items: FG-001 x 100               │
   │ Inspection: PENDING (OQC created) │
   └───────────────────────────────────┘
          ↓

5. PROCESS SHIPPING (deduct inventory)
   ┌───────────────────────────────────┐
   │ processShipping()                 │
   │                                   │
   │ FIFO Selection:                   │
   │   LOT-001: Allocate 100           │
   │                                   │
   │ Inventory Impact:                 │
   │   Available: 150 → 50             │
   │   Reserved: 100 → 100             │
   │                                   │
   │ Transaction: OUT_SHIPPING (-100)  │
   │ Shipping Status: PENDING →        │
   │                  PROCESSING       │
   └───────────────────────────────────┘
          ↓

6. OQC INSPECTION
   ┌───────────────────────────────────┐
   │ Quality Inspector:                │
   │   Inspect LOT-001, Qty: 100       │
   │   Result: PASS                    │
   │                                   │
   │ Update:                           │
   │   shipping_item.inspection_status │
   │   PENDING → PASS                  │
   └───────────────────────────────────┘
          ↓

7. CONFIRM SHIPPING
   ┌───────────────────────────────────┐
   │ confirmShipping()                 │
   │                                   │
   │ Validation: All inspections PASS  │
   │                                   │
   │ Updates:                          │
   │ - Shipping Status: PROCESSING →   │
   │                    SHIPPED        │
   │ - Sales Order Item:               │
   │   shipped_quantity: 0 → 100       │
   │ - Sales Order Status:             │
   │   CONFIRMED → DELIVERED           │
   │ - Inventory Reserved: 100 → 0     │
   └───────────────────────────────────┘
          ↓

8. COMPLETE ORDER (optional)
   ┌───────────────────────────────────┐
   │ completeOrder()                   │
   │                                   │
   │ Order Status: DELIVERED →         │
   │               COMPLETED           │
   └───────────────────────────────────┘
```

---

### FIFO LOT Selection Flow

```
┌─────────────────────────────────────────────────────────────────┐
│              FIFO LOT Selection Algorithm                        │
└─────────────────────────────────────────────────────────────────┘

INPUT: Product FG-001, Quantity: 500

STEP 1: Find Available Inventory
   ┌─────────────────────────────────┐
   │ Query: inventory table          │
   │ Filters:                        │
   │ - product_id = FG-001           │
   │ - warehouse_id = WH-FG          │
   │ - lot.quality_status = PASSED   │
   │ - available_quantity > 0        │
   └─────────────────────────────────┘
          ↓

STEP 2: Sort by Production Date (FIFO)
   ┌─────────────────────────────────────────────────┐
   │ LOT-001: 200 units, production_date=2026-01-10  │
   │ LOT-002: 150 units, production_date=2026-01-15  │
   │ LOT-003: 300 units, production_date=2026-01-20  │
   └─────────────────────────────────────────────────┘
          ↓

STEP 3: Allocate from Oldest LOTs
   ┌─────────────────────────────────────────────────┐
   │ Iteration 1:                                    │
   │   Current LOT: LOT-001 (200 units)              │
   │   Remaining: 500                                │
   │   Allocate: min(200, 500) = 200                 │
   │   Remaining: 500 - 200 = 300                    │
   ├─────────────────────────────────────────────────┤
   │ Iteration 2:                                    │
   │   Current LOT: LOT-002 (150 units)              │
   │   Remaining: 300                                │
   │   Allocate: min(150, 300) = 150                 │
   │   Remaining: 300 - 150 = 150                    │
   ├─────────────────────────────────────────────────┤
   │ Iteration 3:                                    │
   │   Current LOT: LOT-003 (300 units)              │
   │   Remaining: 150                                │
   │   Allocate: min(300, 150) = 150                 │
   │   Remaining: 150 - 150 = 0                      │
   └─────────────────────────────────────────────────┘
          ↓

STEP 4: Return Allocations
   ┌─────────────────────────────────────────────────┐
   │ List<LotAllocation>:                            │
   │ [                                               │
   │   {lot: LOT-001, allocatedQuantity: 200},       │
   │   {lot: LOT-002, allocatedQuantity: 150},       │
   │   {lot: LOT-003, allocatedQuantity: 150}        │
   │ ]                                               │
   │                                                 │
   │ Total Allocated: 500 units ✓                    │
   └─────────────────────────────────────────────────┘
```

---

### Shipping Cancellation Flow

```
┌─────────────────────────────────────────────────────────────────┐
│           Shipping Cancellation and Inventory Restoration        │
└─────────────────────────────────────────────────────────────────┘

SCENARIO: Shipping SH-001 (PROCESSING) needs to be cancelled

STEP 1: Find Original Transactions
   ┌─────────────────────────────────────────────────┐
   │ Query: inventory_transactions                   │
   │ WHERE reference_no = 'SH-001'                   │
   │   AND transaction_type = 'OUT_SHIPPING'         │
   │                                                 │
   │ Results:                                        │
   │ TX-001: LOT-001, quantity=-200                  │
   │ TX-002: LOT-002, quantity=-150                  │
   │ TX-003: LOT-003, quantity=-150                  │
   └─────────────────────────────────────────────────┘
          ↓

STEP 2: Create Reversal Transactions
   ┌─────────────────────────────────────────────────┐
   │ For each original transaction:                  │
   │                                                 │
   │ REV-TX-001:                                     │
   │   transaction_no: REV-SHIP-SH-001-LOT-001-001   │
   │   transaction_type: IN_RETURN                   │
   │   quantity: +200 (negate original -200)         │
   │   reference_no: SH-001                          │
   │   remarks: Reversal for cancelled shipping      │
   │                                                 │
   │ REV-TX-002:                                     │
   │   quantity: +150 (negate original -150)         │
   │                                                 │
   │ REV-TX-003:                                     │
   │   quantity: +150 (negate original -150)         │
   └─────────────────────────────────────────────────┘
          ↓

STEP 3: Restore Inventory Quantities
   ┌─────────────────────────────────────────────────┐
   │ LOT-001:                                        │
   │   Before: available_quantity = 0                │
   │   Add: 200 (from reversal)                      │
   │   After: available_quantity = 200               │
   │                                                 │
   │ LOT-002:                                        │
   │   Before: available_quantity = 0                │
   │   Add: 150                                      │
   │   After: available_quantity = 150               │
   │                                                 │
   │ LOT-003:                                        │
   │   Before: available_quantity = 150              │
   │   Add: 150                                      │
   │   After: available_quantity = 300               │
   └─────────────────────────────────────────────────┘
          ↓

STEP 4: Release Sales Order Reservation
   ┌─────────────────────────────────────────────────┐
   │ salesOrderService.releaseReservedInventory()    │
   │                                                 │
   │ Inventory Update:                               │
   │   reserved_quantity: 100 → 0                    │
   │                                                 │
   │ Sales Order:                                    │
   │   Can now be re-confirmed or cancelled          │
   └─────────────────────────────────────────────────┘
          ↓

STEP 5: Update Shipping Status
   ┌─────────────────────────────────────────────────┐
   │ shipping_status: PROCESSING → CANCELLED         │
   │ remarks: "Cancelled: [reason]"                  │
   └─────────────────────────────────────────────────┘

RESULT: Complete inventory restoration ✓
```

---

## Data Flow Examples

### Example 1: Simple Order with Single LOT

**Scenario**: Small order fulfilled from single LOT

```yaml
Initial State:
  Sales Order:
    - orderNo: SO-001
    - status: DRAFT
    - items:
      - product: FG-001
        quantity: 50

  Inventory (Warehouse WH-FG):
    - LOT-001:
        production_date: 2026-01-10
        quality_status: PASSED
        available_quantity: 200
        reserved_quantity: 0

Workflow:
  1. confirmOrderWithInventory(SO-001, WH-FG):
     → reserved_quantity: 0 → 50
     → order_status: DRAFT → CONFIRMED

  2. createShippingFromOrder(SO-001, WH-FG, shipper=3):
     → shipping_no: SH-20260124-0001
     → shipping_status: PENDING
     → OQC created: QI-OQC-20260124-0001

  3. processShipping(SH-001):
     → FIFO selects LOT-001 (oldest)
     → allocatedQuantity: 50 from LOT-001
     → inventory.available_quantity: 200 → 150
     → shipping_status: PENDING → PROCESSING

  4. OQC Inspection:
     → inspector approves: PASS
     → inspection_status: PENDING → PASS

  5. confirmShipping(SH-001):
     → shipping_status: PROCESSING → SHIPPED
     → sales_order_item.shipped_quantity: 0 → 50
     → sales_order.order_status: CONFIRMED → DELIVERED
     → reserved_quantity: 50 → 0

Final State:
  Sales Order: SO-001, status=DELIVERED
  Inventory LOT-001: available=150, reserved=0
  Shipping: SH-001, status=SHIPPED
```

---

### Example 2: Large Order with Multi-LOT Allocation

**Scenario**: Large order requires multiple LOTs

```yaml
Initial State:
  Sales Order:
    - orderNo: SO-002
    - status: DRAFT
    - items:
      - product: FG-001
        quantity: 500

  Inventory (Warehouse WH-FG):
    - LOT-001: 200 units (2026-01-10) PASSED
    - LOT-002: 150 units (2026-01-15) PASSED
    - LOT-003: 300 units (2026-01-20) PASSED

Workflow:
  1. confirmOrderWithInventory(SO-002, WH-FG):
     → Total available: 200+150+300 = 650 ✓
     → Reserve 500 units
     → reserved_quantity increased across LOTs

  2. createShippingFromOrder(SO-002, WH-FG):
     → shipping_no: SH-20260124-0002

  3. processShipping(SH-002):
     → FIFO Selection:
       • LOT-001: Allocate 200 (all)
       • LOT-002: Allocate 150 (all)
       • LOT-003: Allocate 150 (partial, 150 remains)

     → Create 3 inventory transactions:
       TX-001: OUT_SHIPPING, LOT-001, -200
       TX-002: OUT_SHIPPING, LOT-002, -150
       TX-003: OUT_SHIPPING, LOT-003, -150

     → Inventory Updates:
       LOT-001: 200 → 0
       LOT-002: 150 → 0
       LOT-003: 300 → 150

     → shipping_item.lot_id = LOT-001 (primary)

  4. OQC Inspection (all LOTs pass)

  5. confirmShipping(SH-002):
     → shipped_quantity: 0 → 500
     → order_status: CONFIRMED → DELIVERED

Final State:
  Inventory:
    LOT-001: available=0 (depleted)
    LOT-002: available=0 (depleted)
    LOT-003: available=150 (partial)

  Transaction History: 3 OUT_SHIPPING transactions
```

---

### Example 3: Order Cancellation After Processing

**Scenario**: Shipping processed but needs to be cancelled before shipment

```yaml
Initial State:
  Shipping: SH-003, status=PROCESSING
  Sales Order: SO-003, status=CONFIRMED
  Inventory:
    LOT-001: available=0 (already deducted 100)
  Transactions:
    TX-001: OUT_SHIPPING, LOT-001, -100

Workflow:
  1. cancelShipping(SH-003, "Customer cancelled order"):

     a. Find original transactions:
        → TX-001: OUT_SHIPPING, -100

     b. Create reversal:
        → REV-TX-001: IN_RETURN, +100
        → transaction_no: REV-SHIP-SH-003-LOT-001-001

     c. Restore inventory:
        → LOT-001.available_quantity: 0 → 100

     d. Release sales order reservation:
        → salesOrderService.releaseReservedInventory(SO-003)

     e. Update shipping:
        → shipping_status: PROCESSING → CANCELLED
        → remarks: "Cancelled: Customer cancelled order"

Final State:
  Shipping: SH-003, status=CANCELLED
  Inventory LOT-001: available=100 (restored)
  Transactions:
    TX-001: OUT_SHIPPING, -100 (original)
    REV-TX-001: IN_RETURN, +100 (reversal)
  Net Effect: Inventory fully restored ✓
```

---

### Example 4: Insufficient Inventory Scenario

**Scenario**: Order quantity exceeds available inventory

```yaml
Initial State:
  Sales Order:
    - orderNo: SO-004
    - items:
      - product: FG-001
        quantity: 1000

  Inventory (Warehouse WH-FG):
    - LOT-001: 200 units PASSED
    - LOT-002: 150 units PASSED
    Total Available: 350 units

Workflow:
  1. checkInventoryAvailability(SO-004, WH-FG):
     → Result:
       {
         "1": {
           "product": "FG-001",
           "orderedQuantity": 1000,
           "availableQuantity": 350,
           "isAvailable": false,
           "shortfall": 650
         }
       }

  2. confirmOrderWithInventory(SO-004, WH-FG):
     → ERROR: INSUFFICIENT_INVENTORY
     → Exception: "Insufficient inventory for product FG-001"
     → order_status remains DRAFT

Result:
  Order NOT confirmed
  User notified of shortfall: 650 units
  Suggested actions:
    - Reduce order quantity
    - Wait for new production
    - Source from different warehouse
```

---

### Example 5: Partial Delivery Scenario

**Scenario**: Multi-item order with partial shipments

```yaml
Initial State:
  Sales Order: SO-005
  Items:
    - Item 1: FG-001, quantity=100
    - Item 2: FG-002, quantity=200

  Inventory:
    - FG-001: 150 units available
    - FG-002: 100 units available

Workflow:
  1. confirmOrderWithInventory(SO-005, WH-FG):
     → ERROR: FG-002 has shortfall of 100
     → Order NOT confirmed

  [Alternative: Confirm only available items]

  2. Modified Order (Item 2 reduced to 100):
     → confirmOrderWithInventory(SO-005, WH-FG)
     → Reserved: FG-001=100, FG-002=100
     → order_status: CONFIRMED

  3. createShippingFromOrder(SO-005):
     → Shipping 1: SH-005 (both items)

  4. processShipping(SH-005):
     → Both items processed successfully

  5. confirmShipping(SH-005):
     → Item 1: shipped_quantity = 100 (100% fulfilled)
     → Item 2: shipped_quantity = 100 (50% fulfilled)
     → order_status: PARTIALLY_DELIVERED

  6. [After new production of FG-002]
     → Create second shipping: SH-006
     → Ship remaining 100 units of FG-002
     → order_status: PARTIALLY_DELIVERED → DELIVERED

Final State:
  Sales Order: SO-005, status=DELIVERED
  Shipments: SH-005 (partial), SH-006 (completion)
```

---

## Testing Scenarios

### Test Case 1: Happy Path - Order to Delivery

**Test**: Complete workflow from order creation to delivery

**Steps**:
```java
// 1. Create sales order
SalesOrderEntity order = createTestSalesOrder(
    "SO-TEST-001",
    List.of(new OrderItem("FG-001", 100))
);

// 2. Check availability
Map<Long, InventoryAvailability> availability =
    salesOrderService.checkInventoryAvailability(
        order.getSalesOrderId(),
        finishedGoodsWarehouse.getWarehouseId()
    );

// Verify: All items available
assertTrue(availability.values().stream().allMatch(
    InventoryAvailability::isAvailable
));

// 3. Confirm order
SalesOrderEntity confirmed =
    salesOrderService.confirmOrderWithInventory(
        order.getSalesOrderId(),
        finishedGoodsWarehouse.getWarehouseId()
    );

// Verify: Status changed, inventory reserved
assertEquals("CONFIRMED", confirmed.getOrderStatus());

// 4. Create shipping
ShippingEntity shipping =
    salesOrderService.createShippingFromOrder(
        order.getSalesOrderId(),
        finishedGoodsWarehouse.getWarehouseId(),
        shipper.getUserId()
    );

// Verify: Shipping created with items
assertEquals("PENDING", shipping.getShippingStatus());
assertEquals(1, shipping.getItems().size());

// 5. Process shipping
ShippingEntity processed =
    shippingService.processShipping(shipping.getShippingId());

// Verify: Status changed, LOTs assigned, inventory deducted
assertEquals("PROCESSING", processed.getShippingStatus());
assertNotNull(processed.getItems().get(0).getLot());

// 6. Perform OQC inspection (mock)
performOQCInspection(processed, "PASS");

// 7. Confirm shipping
ShippingEntity confirmed =
    shippingService.confirmShipping(shipping.getShippingId());

// Verify: Shipping confirmed, order updated
assertEquals("SHIPPED", confirmed.getShippingStatus());

SalesOrderEntity updated =
    salesOrderService.findById(order.getSalesOrderId()).get();
assertEquals("DELIVERED", updated.getOrderStatus());
assertEquals(BigDecimal.valueOf(100),
    updated.getItems().get(0).getShippedQuantity());
```

**Expected Result**: ✅ Complete workflow successful

---

### Test Case 2: FIFO LOT Selection

**Test**: Verify LOTs are selected in correct order (oldest first)

**Setup**:
```java
// Create multiple LOTs with different production dates
LotEntity lot1 = createLot("LOT-001", date("2026-01-10"), 200);
LotEntity lot2 = createLot("LOT-002", date("2026-01-15"), 150);
LotEntity lot3 = createLot("LOT-003", date("2026-01-20"), 300);

// Create inventory for each LOT
createInventory(lot1, 200);
createInventory(lot2, 150);
createInventory(lot3, 300);
```

**Test**:
```java
// Request 500 units
List<ShippingService.LotAllocation> allocations =
    shippingService.selectLotsFIFO(
        tenantId,
        warehouseId,
        productId,
        BigDecimal.valueOf(500)
    );

// Verify: 3 allocations in FIFO order
assertEquals(3, allocations.size());

// Verify: First allocation from oldest LOT
assertEquals("LOT-001", allocations.get(0).getLot().getLotNo());
assertEquals(BigDecimal.valueOf(200),
    allocations.get(0).getAllocatedQuantity());

// Verify: Second allocation
assertEquals("LOT-002", allocations.get(1).getLot().getLotNo());
assertEquals(BigDecimal.valueOf(150),
    allocations.get(1).getAllocatedQuantity());

// Verify: Third allocation (partial from LOT-003)
assertEquals("LOT-003", allocations.get(2).getLot().getLotNo());
assertEquals(BigDecimal.valueOf(150),
    allocations.get(2).getAllocatedQuantity());
```

**Expected Result**: ✅ LOTs selected in FIFO order

---

### Test Case 3: Insufficient Inventory

**Test**: Verify proper error handling when inventory is insufficient

**Setup**:
```java
// Create order for 1000 units
SalesOrderEntity order = createTestSalesOrder(
    "SO-TEST-002",
    List.of(new OrderItem("FG-001", 1000))
);

// Available inventory: only 350 units
LotEntity lot1 = createLot("LOT-001", date("2026-01-10"), 200);
LotEntity lot2 = createLot("LOT-002", date("2026-01-15"), 150);
```

**Test**:
```java
// Check availability
Map<Long, InventoryAvailability> availability =
    salesOrderService.checkInventoryAvailability(
        order.getSalesOrderId(),
        warehouseId
    );

// Verify: Not available, shortfall calculated
InventoryAvailability info = availability.get(productId);
assertFalse(info.isAvailable());
assertEquals(BigDecimal.valueOf(1000), info.getOrderedQuantity());
assertEquals(BigDecimal.valueOf(350), info.getAvailableQuantity());
assertEquals(BigDecimal.valueOf(650), info.getShortfall());

// Attempt to confirm
BusinessException exception = assertThrows(
    BusinessException.class,
    () -> salesOrderService.confirmOrderWithInventory(
        order.getSalesOrderId(),
        warehouseId
    )
);

// Verify: Proper error code
assertEquals(ErrorCode.INSUFFICIENT_INVENTORY, exception.getErrorCode());
```

**Expected Result**: ✅ Proper error handling and reporting

---

### Test Case 4: Shipping Cancellation and Restoration

**Test**: Verify inventory is correctly restored on cancellation

**Setup**:
```java
// Create and process shipping
ShippingEntity shipping = createAndProcessShipping(
    "SH-TEST-001",
    List.of(new ShippingItem("FG-001", 100))
);

// Capture inventory state after processing
BigDecimal inventoryAfterShipping = getInventoryQuantity(productId, lotId);
```

**Test**:
```java
// Cancel shipping
ShippingEntity cancelled = shippingService.cancelShipping(
    shipping.getShippingId(),
    "Test cancellation"
);

// Verify: Status changed
assertEquals("CANCELLED", cancelled.getShippingStatus());

// Verify: Reversal transactions created
List<InventoryTransactionEntity> reversals =
    inventoryTransactionRepository.findByReferenceNo(
        shipping.getShippingNo()
    ).stream()
    .filter(tx -> "IN_RETURN".equals(tx.getTransactionType()))
    .collect(Collectors.toList());

assertEquals(1, reversals.size());
assertEquals(BigDecimal.valueOf(100), reversals.get(0).getQuantity());

// Verify: Inventory quantity restored
BigDecimal inventoryAfterCancellation =
    getInventoryQuantity(productId, lotId);

assertEquals(
    inventoryBeforeShipping,
    inventoryAfterCancellation
);
```

**Expected Result**: ✅ Inventory fully restored

---

### Test Case 5: OQC Integration

**Test**: Verify OQC inspection blocks shipping confirmation

**Setup**:
```java
// Create shipping with OQC required
ShippingEntity shipping = createShipping(
    "SH-TEST-002",
    List.of(new ShippingItem("FG-001", 100, "PENDING"))
);

// Process shipping
shippingService.processShipping(shipping.getShippingId());
```

**Test**:
```java
// Attempt to confirm without OQC completion
BusinessException exception = assertThrows(
    BusinessException.class,
    () -> shippingService.confirmShipping(shipping.getShippingId())
);

// Verify: Proper error
assertEquals(ErrorCode.INSPECTION_NOT_COMPLETED, exception.getErrorCode());

// Perform OQC inspection
QualityInspectionEntity inspection =
    performOQCInspection(shipping, "PASS");

// Update shipping item inspection status
updateInspectionStatus(shipping, "PASS");

// Now confirm should succeed
ShippingEntity confirmed =
    shippingService.confirmShipping(shipping.getShippingId());

assertEquals("SHIPPED", confirmed.getShippingStatus());
```

**Expected Result**: ✅ OQC properly enforced

---

## Integration Points

### 1. Quality Management System (QMS)

**Integration Point**: OQC (Outgoing Quality Control)

**Workflow**:
```
Shipping Created (with OQC required)
    ↓
ShippingService.createOQCRequest()
    ↓
QualityInspectionEntity created
    - inspection_type: OUTGOING
    - inspection_status: PENDING
    - linked to shipping_item
    ↓
Quality Inspector performs inspection
    ↓
QualityInspection updated
    - inspection_result: PASS / FAIL
    ↓
ShippingItem.inspection_status updated
    ↓
Shipping can be confirmed (if PASS)
```

**Database Links**:
```sql
shipping_items.quality_inspection_id → quality_inspections.quality_inspection_id
```

---

### 2. Inventory Management System

**Integration Points**:

**a. Inventory Availability**
- `InventoryService.checkAvailability()`
- Query: `inventory.available_quantity`

**b. Inventory Reservation**
- `InventoryService.reserveInventory()`
- Update: `inventory.reserved_quantity`

**c. Inventory Deduction**
- `InventoryService.deductInventory()`
- Update: `inventory.available_quantity`

**d. Inventory Transactions**
- `InventoryTransactionRepository.save()`
- Types: OUT_SHIPPING, IN_RETURN

**Workflow**:
```
Sales Order Confirmation
    ↓
Check Availability → InventoryRepository
    ↓
Reserve Inventory → Update reserved_quantity
    ↓
Create Shipping
    ↓
Process Shipping → FIFO LOT Selection
    ↓
Create Transactions → InventoryTransactionRepository
    ↓
Deduct Inventory → Update available_quantity
```

---

### 3. LOT Management

**Integration Points**:

**a. LOT Selection (FIFO)**
- `LotRepository.findByTenant_TenantIdAndProduct_ProductId()`
- Filter: quality_status = PASSED
- Sort: production_date ASC

**b. LOT Tracking**
- `shipping_items.lot_id` references selected LOT
- `inventory_transactions.lot_id` tracks LOT movements

**Workflow**:
```
processShipping()
    ↓
selectLotsFIFO()
    ↓
Query LOTs:
    - quality_status = PASSED
    - available_quantity > 0
    - Order by production_date ASC
    ↓
Allocate from oldest LOTs first
    ↓
Create transaction for each LOT allocation
    ↓
Link shipping_item to primary LOT
```

---

### 4. Purchase Order System

**Integration Point**: Finished goods from production

**Workflow**:
```
Production Completes
    ↓
WorkResultService.recordProductionWithInventory()
    ↓
Create Finished Goods LOT
    ↓
Update Inventory (finished goods warehouse)
    ↓
LOT available for sales orders
    ↓
Sales Order can be fulfilled
```

**Database Flow**:
```
work_orders → work_results → lots (finished goods)
    ↓
inventory (finished goods warehouse)
    ↓
sales_orders → shippings
```

---

## Summary

### Phase 7 Achievements

✅ **Sales Order Service Enhanced** (~300 lines)
- Inventory availability checking
- Inventory reservation on confirmation
- Automatic shipping creation
- Order lifecycle management

✅ **Shipping Service Enhanced** (~300 lines)
- FIFO LOT selection algorithm
- Inventory deduction on shipping
- Multi-LOT allocation support
- OQC inspection integration
- Shipping cancellation with inventory restoration

✅ **Complete Integration**
- Sales Order ↔ Inventory
- Shipping ↔ Inventory
- Shipping ↔ QMS (OQC)
- Shipping ↔ LOT Management

✅ **Robust Error Handling**
- Insufficient inventory detection
- Status validation
- OQC enforcement
- Transaction rollback on cancellation

### Key Technical Features

1. **FIFO Algorithm**: Ensures oldest inventory is shipped first
2. **Multi-LOT Support**: Handles large orders spanning multiple LOTs
3. **Transaction Tracking**: Complete audit trail for all inventory movements
4. **Inventory Restoration**: Automatic reversal on shipping cancellation
5. **Quality Integration**: OQC inspection blocks shipping until passed

### Files Modified

1. `SalesOrderService.java` - Enhanced with inventory integration
2. `ShippingService.java` - Enhanced with FIFO and inventory deduction
3. `SALES_PHASE7_INTEGRATION_COMPLETE.md` - This documentation

### Next Steps

**Potential Phase 8 Enhancements**:
1. Return/RMA (Return Merchandise Authorization) handling
2. Advanced FIFO/FEFO/LIFO strategies
3. Cross-warehouse fulfillment
4. Partial shipping optimization
5. Real-time inventory alerts

---

**Document Version**: 1.0
**Last Updated**: 2026-01-24
**Status**: Phase 7 Complete ✅

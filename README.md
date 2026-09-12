# IFG-Kogito Checkout Marketplace

Proyek ini mengimplementasikan **workflow checkout marketplace** menggunakan **Kogito** (BPMN workflow engine berbasis Quarkus). Tugasnya adalah membuat sistem checkout yang memvalidasi cart, mengecek inventory, memproses payment, dan membuat order — dengan audit trail untuk setiap proses.

---

## Daftar Isi

1. [Arsitektur](#arsitektur)
2. [BPMN Process](#bpmn-process)
3. [Services](#services)
4. [Endpoints](#endpoints)
5. [Test Scenarios](#test-scenarios)
6. [Cara Menjalankan](#cara-menjalankan)

---

## Arsitektur

```
┌─────────────────────────────────────────────────────────────┐
│                    Quarkus Application                       │
│                                                             │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐  │
│  │  Kogito BPMN │───▶│   Services   │───▶│  In-Memory   │  │
│  │   Engine     │    │  (CDI Beans) │    │    Stores    │  │
│  └──────────────┘    └──────────────┘    └──────────────┘  │
│         │                   │                   │           │
│         ▼                   ▼                   ▼           │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐  │
│  │  checkout    │    │CartService   │    │InventoryStore│  │
│  │  .bpmn       │    │InventorySvc  │    │ (Concurrent  │  │
│  │              │    │PaymentSvc    │    │   HashMap)   │  │
│  │              │    │OrderSvc      │    └──────────────┘  │
│  │              │    │Notification  │                        │
│  │              │    │CheckoutSvc   │    ┌──────────────┐  │
│  └──────────────┘    └──────────────┘    │ AuditStore   │  │
│                                          │ (ArrayList)  │  │
│                                          └──────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

**Tech Stack:**
- **Quarkus 3.27.2** — Java framework
- **Kogito 10.2.0** — BPMN workflow engine (jbpm-with-drools-quarkus)
- **MicroProfile Config** — konfigurasi inventory via properties
- **RestAssured + JUnit 5** — integration testing

---

## BPMN Process

File: `src/main/resources/checkout.bpmn`

Visualisasi BPMN diagram tersedia di folder `\Diagram Image`.

**Flow proses:**
1. **Validate Cart** → cek apakah cart valid
2. **Gateway CartValid** → jika tidak valid, reject
3. **Check Inventory** → cek stok tersedia (dari `InventoryStore`)
4. **Gateway StockAvailable** → jika habis, end "Out of Stock"
5. **Process Payment** → proses pembayaran
6. **Gateway PaymentSuccessful** → jika gagal, end "Payment Failed"
7. **Create Order** → buat order baru
8. **Update Inventory** → kurangi stok
9. **Send Confirmation** → kirim notifikasi
10. **Complete Checkout** → selesai

---

## Services

### Service Classes

| File | Fungsi |
|------|--------|
| `src/main/java/com/example/checkout/service/CartService.java` | Validasi cart berdasarkan flag `cartValid` |
| `src/main/java/com/example/checkout/service/InventoryService.java` | Cek & update stok inventory (in-memory `ConcurrentHashMap`, initial data dari `application.properties`) |
| `src/main/java/com/example/checkout/service/PaymentService.java` | Proses pembayaran berdasarkan flag `paymentSuccessful` |
| `src/main/java/com/example/checkout/service/OrderService.java` | Generate order ID unik (prefix `ORD-`) |
| `src/main/java/com/example/checkout/service/NotificationService.java` | Kirim konfirmasi order (console output) |
| `src/main/java/com/example/checkout/service/CheckoutService.java` | Handle semua skenario akhir (reject, out of stock, payment failed, complete) |

### Model

| File | Fungsi |
|------|--------|
| `src/main/java/com/example/checkout/model/CheckoutRequest.java` | DTO untuk request checkout (cartId, customerId, amount, cartValid, stockAvailable, paymentSuccessful) |

### Audit

| File | Fungsi |
|------|--------|
| `src/main/java/com/example/checkout/audit/CheckoutAuditRecord.java` | Data class untuk audit record (instanceId, processId, event, timestamp, variables) |
| `src/main/java/com/example/checkout/audit/CheckoutAuditStore.java` | In-memory store untuk audit records (`@ApplicationScoped`, `ArrayList`) |
| `src/main/java/com/example/checkout/audit/CheckoutAuditResource.java` | REST endpoint untuk query audit trail (`GET /checkout-instances`) |
| `src/main/java/com/example/checkout/audit/CheckoutProcessEventListener.java` | Kogito `ProcessEventListener` — auto-record setiap `started` & `completed` event |

### Resource (REST)

| File | Fungsi |
|------|--------|
| `src/main/java/com/example/checkout/service/InventoryResource.java` | REST endpoint untuk cek stok (`GET /inventory/{cartId}`) |
| `src/main/java/com/example/GreetingResource.java` | Default Quarkus greeting endpoint (`GET /hello`) |

### Config

| File | Fungsi |
|------|--------|
| `src/main/resources/application.properties` | Konfigurasi inventory initial stock, Kogito settings, Swagger UI |

---

## Endpoints

### Process Endpoints (Kogito-generated)

| Method | Endpoint | Deskripsi |
|--------|----------|-----------|
| `POST` | `/Process_Checkout` | Membuat instance proses checkout baru |
| `GET` | `/Process_Checkout` | List semua instance aktif |
| `GET` | `/Process_Checkout/{id}` | Detail instance by ID |
| `DELETE` | `/Process_Checkout/{id}` | Hapus instance |

**Contoh request:**
```bash
curl -X POST http://localhost:8080/Process_Checkout \
  -H "Content-Type: application/json" \
  -d '{"request":{"cartId":"CART-001","customerId":"USER-123","amount":150000,"cartValid":true,"stockAvailable":true,"paymentSuccessful":true}}'
```

**Response (sukses):**
```json
{
  "id": "cb874281-f874-449b-8702-0d81fe2f0a8e",
  "orderId": "ORD-51ac3956-c71f-4e57-a06d-7e218e7a38ce",
  "request": { "cartId": "CART-001", "customerId": "USER-123", "amount": 150000.0, "cartValid": true, "stockAvailable": true, "paymentSuccessful": true },
  "cartValid": true,
  "paymentSuccessful": true,
  "stockAvailable": true
}
```

**Response (reject — cart invalid / out of stock / payment failed):**
```json
{
  "id": "47b457ab-e91c-484a-b821-9db4b0c898e7",
  "orderId": null,
  "request": { "cartId": "CART-BAD", "cartValid": false },
  "cartValid": false,
  "paymentSuccessful": null,
  "stockAvailable": null
}
```

---

### Inventory Endpoints

| Method | Endpoint | Deskripsi |
|--------|----------|-----------|
| `GET` | `/inventory/{cartId}` | Cek stok untuk cart tertentu |

**Contoh:**
```bash
curl http://localhost:8080/inventory/CART-001
# Response: {"CART-001":10}
```

---

### Audit Endpoints

| Method | Endpoint | Deskripsi |
|--------|----------|-----------|
| `GET` | `/checkout-instances` | Semua audit records (newest first) |
| `GET` | `/checkout-instances?event=completed` | Filter by event type |
| `GET` | `/checkout-instances/count` | Total record count |
| `DELETE` | `/checkout-instances` | Clear semua records |

**Record structure:**
```json
{
  "instanceId": "cb874281-f874-449b-8702-0d81fe2f0a8e",
  "processId": "Process_Checkout",
  "event": "completed",
  "timestamp": "2026-09-12T08:34:13.205292400Z",
  "variables": { "request": {...}, "orderId": "ORD-...", "cartValid": true }
}
```

---

## Test Scenarios

File: `src/test/java/com/example/CheckoutProcessTest.java`

### Positive Tests

| Test | Deskripsi | Expected |
|------|-----------|----------|
| `testStartCheckoutProcess` | Semua flag true | `201` + `orderId` ORD-* |

### Negative Tests

| Test | Deskripsi | Expected |
|------|-----------|----------|
| `testCheckoutRejectedWhenCartInvalid` | `cartValid=false` | `orderId: null` |
| `testCheckoutRejectedWhenOutOfStock` | `CART-005` (stok=0) | `orderId: null`, `stockAvailable: false` |
| `testCheckoutRejectedWhenPaymentFails` | `paymentSuccessful=false` | `orderId: null` |
| `testCheckoutRejectedWhenAllFlagsFalse` | Semua false | Reject di gerbang pertama |
| `testCheckoutRejectedCartValidButOutOfStock` | Cart valid, stok habis | Reject di gerbang inventory |

### Inventory Tests

| Test | Deskripsi | Expected |
|------|-----------|----------|
| `testInventoryReducesAfterSuccessfulCheckout` | Checkout CART-004 (stok 1) | Stok jadi 0 |
| `testZeroStockRejectsCheckout` | Checkout CART-005 (stok 0) | Reject |

### Audit Tests

| Test | Deskripsi | Expected |
|------|-----------|----------|
| `testAuditRecordsCreatedForSuccessfulCheckout` | Checkout sukses | 2 records (started+completed) |
| `testAuditRecordsCreatedForRejectedCheckout` | Checkout reject | 2 records (started+completed) |
| `testAuditCountEndpoint` | Count setelah 1 proses | `count: 2` |
| `testAuditFilterByEvent` | Filter by event | Hanya event yang diminta |

---


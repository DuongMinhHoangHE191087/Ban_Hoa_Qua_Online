# API Response Envelope Migration Guide

## Overview

All API endpoints are migrating to a consistent JSON response format using `ApiResponse<T>`.

**Old format (per-endpoint):**
```json
{ "success": true, "cartSummary": {...} }
{ "success": false, "error": "Invalid request" }
```

**New format (standardized):**
```json
{ "success": true, "data": {...}, "meta": {...} }
{ "success": false, "error": "Invalid request" }
```

---

## Backend: Java Implementation

### The Envelope Classes

**`ApiResponse<T>`** (`src/java/com/fruitmkt/dto/ApiResponse.java`)
- Immutable record with fields: `success`, `data`, `error`, `meta`
- Null fields are omitted in JSON via `@JsonInclude(NON_NULL)`
- Static factories: `ok(data)`, `ok(data, meta)`, `fail(message)`, `fail(statusCode, message)`

**`PageMeta`** (`src/java/com/fruitmkt/dto/PageMeta.java`)
- Carries pagination metadata: `page`, `pageSize`, `totalCount`, `totalPages`
- Factory: `PageMeta.of(page, pageSize, totalCount)` auto-calculates totalPages

### Conversion Recipe (per endpoint)

**Step 1: Define a typed payload record**
```java
public record CartSyncResponse(String message) {}
```
(Nested inside your servlet as a public record, or in a separate DTO file.)

**Step 2: Replace response writes**
```java
// OLD
JsonUtil.writeJson(resp, Map.of("success", true, "message", "Sync done"));

// NEW
JsonUtil.writeJson(resp, ApiResponse.ok(new CartSyncResponse("Sync done")));
```

**Step 3: Handle errors**
```java
// OLD
JsonUtil.writeJson(resp, Map.of("success", false, "error", "Lỗi: " + e.getMessage()));

// NEW — IMPORTANT: NEVER leak e.getMessage() to clients
LoggerUtil.error(log, "Error details...", e);  // Log server-side
JsonUtil.writeJson(resp, ApiResponse.fail("Lỗi máy chủ. Vui lòng thử lại."));
```

**Step 4: Keep HTTP status separate from response body**
```java
// The fail(statusCode, message) factory stores statusCode in meta, but
// YOU must set the HTTP status explicitly:
resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
JsonUtil.writeJson(resp, ApiResponse.fail(400, "Invalid input"));

// Exception: webhooks that must always return 200:
resp.setStatus(HttpServletResponse.SC_OK);  // Always 200
JsonUtil.writeJson(resp, ApiResponse.ok(null));  // or fail() — SePay only checks HTTP status
```

### Pagination

```java
int page = getIntParameter(req, "page", 1);
int pageSize = getIntParameter(req, "pageSize", 20);
List<Product> items = productDAO.findByCategory(categoryId, page, pageSize);
long totalCount = productDAO.countByCategory(categoryId);

PageMeta meta = PageMeta.of(page, pageSize, totalCount);
JsonUtil.writeJson(resp, ApiResponse.ok(items, meta));
```

Produces:
```json
{
  "success": true,
  "data": [...],
  "meta": {
    "page": 1,
    "pageSize": 20,
    "totalCount": 42,
    "totalPages": 3
  }
}
```

---

## Frontend: JavaScript Handling

### Shared Helper Function

Add this once to your JS utility (e.g., `assets/js/util/api.js`):

```javascript
/**
 * Unwrap an ApiResponse<T> from a fetch response.
 * Throws if !success; returns {data, meta}.
 */
export async function unwrap(response) {
  const body = await response.json();
  if (!body.success) {
    const message = body.error || 'Yêu cầu thất bại';
    throw new Error(message);
  }
  return { data: body.data, meta: body.meta };
}
```

### Per-Endpoint Conversion

**Old pattern (flat response):**
```javascript
// Old: /api/cart/sync returns {"success": true, "message": "..."}
const response = await fetch('/api/cart/sync', { method: 'POST', body: JSON.stringify(items) });
const json = await response.json();
if (!json.success) {
  alert(json.error);
  return;
}
alert(json.message);  // ← direct field access
```

**New pattern (envelope):**
```javascript
// New: /api/cart/sync returns {"success": true, "data": {"message": "..."}}
try {
  const { data } = await unwrap(
    await fetch('/api/cart/sync', { method: 'POST', body: JSON.stringify(items) })
  );
  alert(data.message);  // ← nested under .data
} catch (err) {
  alert(err.message);  // ← error handling is centralized
}
```

---

## Conversion Order (Phase 3b-4)

### Sprint 1: API endpoints (`servlet/api/`)
1. ✅ `PaymentWebhookServlet` (critical: SePay)
2. ✅ `CartSyncServlet` (user-facing)
3. ✅ `NotificationAPIServlet` (user-facing)
4. `CouponValidateServlet` — POST validation endpoint
5. `AddressAPIServlet` — POST address operations
6. `ChatAPI` — WebSocket metadata endpoint
7. `ChatMediaUploadServlet` — multipart upload

### Sprint 2: High-traffic customer servlets
1. `CartServlet` — `/cart?format=json` query endpoint
2. `ProductDetailServlet` — product detail JSON endpoint
3. Order-related servlets with JSON responses

### Sprint 3: Remaining endpoints (admin, shop owner, etc.)
- Convert remaining servlets incrementally
- No deadline — only when touching the code

---

## Backward Compatibility

- **Rule:** Each endpoint is either fully old-format or fully new-format — NEVER mix both.
- **Process:** When converting an endpoint:
  1. Update servlet to use `ApiResponse`
  2. Update its matching JS handler to use `unwrap()`
  3. Merge both in the same PR
  4. Do not flip JS handlers before servlet is ready

---

## Testing

### Servlet-side JUnit

```java
@Test
void cartSync_success_returnsApiResponseOk() {
  // Given: valid user + items
  // When: POST /api/cart/sync
  // Then: response status 200, body is {"success":true,"data":{"message":"..."},"meta":null}
}

@Test
void cartSync_unauthorized_returnsApiResponseFail() {
  // Given: no session
  // When: POST /api/cart/sync
  // Then: response status 401, body is {"success":false,"error":"Người dùng chưa đăng nhập."}
}
```

### Frontend (Playwright / E2E)

```javascript
test('cart sync success', async ({ page }) => {
  // Given: authenticated user
  const response = await page.request.post('/api/cart/sync', {
    data: { items: [...] }
  });
  const body = await response.json();
  expect(response.status()).toBe(200);
  expect(body.success).toBe(true);
  expect(body.data.message).toBeTruthy();
});
```

---

## Security Notes

### Leak Prevention
- **NEVER** return exception details in `ApiResponse.fail(e.getMessage())`
- Always log detailed errors server-side:
  ```java
  LoggerUtil.error(log, "Failed to process", e);  // logs detail
  ApiResponse.fail("Lỗi máy chủ. Vui lòng thử lại.");  // returns generic message
  ```

### CSRF Protection
- CSRF validation logic does NOT change — still uses `sessionToken.equals(requestToken)`
- After validation, return `ApiResponse` normally:
  ```java
  if (!validateCsrf(...)) {
    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
    JsonUtil.writeJson(resp, ApiResponse.fail(403, "CSRF token không hợp lệ."));
    return;
  }
  ```

### Webhook Idempotence
- Webhooks (e.g., PaymentWebhook) MUST always return HTTP 200
- Signal success/failure via `ApiResponse.success` field, never HTTP status
  ```java
  resp.setStatus(HttpServletResponse.SC_OK);  // Always 200
  try {
    process();
    JsonUtil.writeJson(resp, ApiResponse.ok(null));     // {"success":true}
  } catch (Exception e) {
    JsonUtil.writeJson(resp, ApiResponse.fail("error"));  // {"success":false,"error":"..."}
  }
  ```

---

## Reference Implementation

See these committed files for examples:
- `PaymentWebhookServlet` — webhook pattern (always HTTP 200)
- `CartSyncServlet` — standard POST pattern (variable HTTP status)
- `NotificationAPIServlet` — dual GET/POST + pagination

---

## FAQ

**Q: Do I have to convert my endpoint right now?**
A: No. Convert when you touch the code. Old and new endpoints can coexist.

**Q: What if a client still expects the old format?**
A: That's a breaking change. Either keep the endpoint old-format, or coordinate the client update in the same PR.

**Q: Can I put extra fields in `ApiResponse` beyond success/data/error/meta?**
A: No. If you need extra fields, nest them in `data` or `meta`:
```java
// Use this:
record MyPayload(String message, int extraField) {}
ApiResponse.ok(new MyPayload(...));

// NOT this:
record ApiResponseCustom(..., int extraField) {}
```

**Q: How do I return multiple types from one endpoint?**
A: Use a common parent type or a sealed interface:
```java
sealed interface EndpointResponse permits SuccessResponse, ErrorResponse {}
record SuccessResponse(String message) implements EndpointResponse {}
record ErrorResponse(String reason) implements EndpointResponse {}

ApiResponse.ok(new SuccessResponse(...));  // data is EndpointResponse
```

---

## Summary

- **Backend:** Use `ApiResponse<T>` + `PageMeta` for consistency
- **Frontend:** Use `unwrap()` helper for uniform error handling
- **Security:** Never leak `e.getMessage()`; log server-side, return generic messages
- **Order:** Prioritize `servlet/api/` first, then customer-facing, then incrementally

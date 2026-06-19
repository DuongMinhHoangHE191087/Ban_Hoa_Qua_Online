# Chen's ERD Analysis - OnlineFruitShopping (Bản Ý Niệm Tinh Gọn: Chỉ Chứa Thực Thể & Quan Hệ)

Bản phân tích cấu trúc thực thể - mối quan hệ (ERD) theo đúng nguyên lý của Peter Chen: **Chuyển hóa toàn bộ các bảng trung gian, bảng phụ và bảng chi tiết thành các Mối quan hệ (Hình thoi) kèm thuộc tính**, chỉ giữ lại các Thực thể cốt lõi (Hình chữ nhật).

---

## 1. Mối quan hệ kế thừa ISA (Superclass - Subclass)

Thực thể `users` đóng vai trò là thực thể cha (Superclass) chứa thông tin chung. Thuộc tính `role` phân chia thành 4 nhóm vai trò (Subclasses) thông qua liên kết **ISA** (Tam giác).

```
                   [ users ] (Superclass)
                       |
                     /   \
                    | ISA | (Kế thừa)
                   / |   | \
                 /   |   |   \
               /     |   |     \
             /       |   |       \
     [Customer]   [Admin] [Delivery] [Shop Owner]
```

*   **`users`**: Thực thể cha chứa thông tin chung (`user_id` (PK), `full_name`, `email` (Unique), `password_hash`, `phone`, `status`, `user_address`, `avatar_url`, `created_at`, `updated_at`).
*   **Các thực thể con**:
    *   **`Admin`**: Phụ trách cấu hình hệ thống.
    *   **`Delivery`**: Phụ trách các chuyến đi và lượt giao đơn hàng.
    *   **`Customer`**: Thực hiện giỏ hàng và đặt đơn hàng.
    *   **`Shop Owner`**: Chủ cửa hàng, có quan hệ `1:1` với hồ sơ cửa hàng `[shop_owner_profiles]`.

---

## 2. Phân tích Chuyển hóa Bảng Phụ & Bảng Trung gian thành Mối quan hệ

Dưới đây là phân tích chi tiết toàn bộ các bảng trong schema `Schema.sql`. Chúng không được vẽ làm thực thể (Hình chữ nhật) mà được chuyển đổi hoàn toàn thành **Mối quan hệ (Hình thoi) gắn thuộc tính**:

### 2.1. Nhóm Bảng Phụ của Thực thể `users` (Được chuyển thành mối quan hệ 1:N)

1.  **Bảng `user_sessions` (Phiên làm việc)**:
    *   *Bản chất SQL*: Bảng phụ lưu token đăng nhập của user.
    *   *Chuyển đổi Chen ERD*: Chuyển thành mối quan hệ **`<Has_Session>`** (`1:N`) từ `[users]` tới thuộc tính phiên. Do không có thực thể Session tương ứng, ta vẽ mối quan hệ này dạng **Thuộc tính đa trị (Multivalued Attribute)** hoặc mối quan hệ tự thân nối với thuộc tính:
        *   Tên hình thoi: `<Has_Session>`
        *   Thuộc tính gắn trên hình thoi: `session_id` (PK), `token`, `expires_at`.
2.  **Bảng `user_addresses` (Sổ địa chỉ)**:
    *   *Bản chất SQL*: Lưu danh sách địa chỉ nhận hàng của khách.
    *   *Chuyển đổi Chen ERD*: Chuyển thành mối quan hệ **`<Has_Address>`** (`1:N`) gắn từ **`[Customer]`**.
        *   Tên hình thoi: `<Has_Address>`
        *   Thuộc tính gắn trên hình thoi: `address_id` (PK), `recipient_name`, `recipient_phone`, `address_detail`, `is_default`, `created_at`.
3.  **Bảng `notifications` (Thông báo)**:
    *   *Bản chất SQL*: Lưu các thông báo gửi đến từng user.
    *   *Chuyển đổi Chen ERD*: Chuyển thành mối quan hệ **`<Receives_Notification>`** (`1:N`) gắn từ **`[users]`**.
        *   Tên hình thoi: `<Receives_Notification>`
        *   Thuộc tính gắn trên hình thoi: `notification_id` (PK), `type`, `title`, `message`, `action_url`, `is_read`, `created_at`.

### 2.2. Nhóm Bảng Phụ của Thực thể `products` (Được chuyển thành mối quan hệ 1:N)

1.  **Bảng `product_images` (Hình ảnh sản phẩm)**:
    *   *Bản chất SQL*: Lưu các ảnh mô tả của sản phẩm.
    *   *Chuyển đổi Chen ERD*: Chuyển thành mối quan hệ **`<Has_Image>`** (`1:N`) gắn từ **`[products]`**.
        *   Tên hình thoi: `<Has_Image>`
        *   Thuộc tính gắn trên hình thoi: `image_id` (PK), `file_path`, `display_order`, `is_primary`, `uploaded_at`.
2.  **Bảng `product_packaging_options` (Đóng gói kèm theo)**:
    *   *Bản chất SQL*: Lưu các tùy chọn hộp quà, đóng gói kèm của sản phẩm.
    *   *Chuyển đổi Chen ERD*: Chuyển thành mối quan hệ **`<Offers_Packaging>`** (`1:N`) gắn từ **`[products]`**.
        *   Tên hình thoi: `<Offers_Packaging>`
        *   Thuộc tính gắn trên hình thoi: `packaging_id` (PK), `label`, `price_add`, `is_active`.

### 2.3. Nhóm Bảng Trung gian Nhiều-Nhiều (M:N) cốt lõi

1.  **Bảng `cart_items` (Chi tiết giỏ hàng)**:
    *   *Bản chất SQL*: Bảng liên kết Nhiều-Nhiều giữa `cart` và `product_variants`.
    *   *Chuyển đổi Chen ERD*: Vẽ mối quan hệ Nhiều-Nhiều **`<Contains_Item>`** trực tiếp giữa **`[cart]`** và **`[product_variants]`**.
        *   Bản số: `cart` ---(1)--- `<Contains_Item>` ---(N)--- `[product_variants]`.
        *   Thuộc tính gắn trên hình thoi: `cart_item_id` (PK), `quantity`, `added_at`.
2.  **Bảng `order_items` (Chi tiết đơn hàng)**:
    *   *Bản chất SQL*: Bảng liên kết Nhiều-Nhiều giữa `orders` và `product_variants`.
    *   *Chuyển đổi Chen ERD*: Vẽ mối quan hệ Nhiều-Nhiều **`<Contains_Order_Item>`** trực tiếp giữa **`[orders]`** và **`[product_variants]`**.
        *   Bản số: `orders` ---(1)--- `<Contains_Order_Item>` ---(N)--- `[product_variants]`.
        *   Thuộc tính gắn trên hình thoi: `order_item_id` (PK), `product_name_snapshot`, `variant_label_snapshot`, `quantity`, `unit_price`, `subtotal`, `packaging_label_snapshot`, `packaging_price_snapshot`.
3.  **Bảng `order_promotions` (Khuyến mãi áp dụng cho đơn)**:
    *   *Bản chất SQL*: Bảng liên kết Nhiều-Nhiều giữa `orders` và `promotions`.
    *   *Chuyển đổi Chen ERD*: Vẽ mối quan hệ Nhiều-Nhiều **`<Uses_Promotion>`** trực tiếp giữa **`[orders]`** và **`[promotions]`**.
        *   Bản số: `orders` ---(N)--- `<Uses_Promotion>` ---(M)--- `[promotions]`.
        *   Thuộc tính gắn trên hình thoi: `usage_id` (PK), `discount_applied`, `used_at`.
4.  **Bảng `shop_settlement_orders` (Chi tiết quyết toán đơn)**:
    *   *Bản chất SQL*: Bảng liên kết Nhiều-Nhiều giữa `shop_settlements` và `orders`.
    *   *Chuyển đổi Chen ERD*: Vẽ mối quan hệ Nhiều-Nhiều **`<Settles_Order>`** trực tiếp giữa **`[shop_settlements]`** và **`[orders]`**.
        *   Bản số: `shop_settlements` ---(1)--- `<Settles_Order>` ---(1)--- `[orders]`.
        *   Thuộc tính gắn trên hình thoi: `settlement_order_id` (PK), `order_amount`, `platform_fee_amount`, `discount_amount`, `refund_amount`, `net_amount`.
5.  **Bảng `inventory_logs` (Nhật ký kho hàng)**:
    *   *Bản chất SQL*: Lưu vết việc cập nhật kho của biến thể sản phẩm bởi người dùng.
    *   *Chuyển đổi Chen ERD*: Vẽ mối quan hệ Nhiều-Nhiều **`<Updates_Inventory>`** trực tiếp giữa **`[users]`** (Shop Owner/Admin) và **`[product_variants]`**.
        *   Bản số: `users` ---(N)--- `<Updates_Inventory>` ---(M)--- `[product_variants]`.
        *   Thuộc tính gắn trên hình thoi: `log_id` (PK), `change_type`, `quantity_delta`, `quantity_after`, `note`, `expires_at`, `is_expired`, `changed_at`.
6.  **Bảng `chat_messages` (Nội dung tin nhắn)**:
    *   *Bản chất SQL*: Tin nhắn trao đổi giữa khách hàng/chủ shop trong phiên chat.
    *   *Chuyển đổi Chen ERD*: Vẽ mối quan hệ Nhiều-Nhiều **`<Sends_Message>`** trực tiếp giữa **`[users]`** và **`[chat_sessions]`**.
        *   Bản số: `users` ---(1)--- `<Sends_Message>` ---(N)--- `[chat_sessions]`.
        *   Thuộc tính gắn trên hình thoi: `message_id` (PK), `content`, `media_url`, `media_type`, `is_read`, `created_at`.
7.  **Bảng `reviews` (Đánh giá sản phẩm)**:
    *   *Bản chất SQL*: Đánh giá của khách hàng đối với sản phẩm đã mua.
    *   *Chuyển đổi Chen ERD*: Vẽ mối quan hệ Nhiều-Nhiều **`<Writes_Review>`** trực tiếp giữa **`[Customer]`** và **`[product_variants]`**.
        *   Bản số: `Customer` ---(1)--- `<Writes_Review>` ---(N)--- `[product_variants]`.
        *   Thuộc tính gắn trên hình thoi: `review_id` (PK), `rating`, `review_text`, `review_image_url`, `is_hidden`, `created_at`.
8.  **Bảng `return_requests` (Yêu cầu trả hàng)**:
    *   *Bản chất SQL*: Khách hàng yêu cầu hoàn trả cho đơn hàng cụ thể.
    *   *Chuyển đổi Chen ERD*: Vẽ mối quan hệ Nhiều-Nhiều **`<Requests_Return>`** trực tiếp giữa **`[Customer]`** và **`[orders]`**.
        *   Bản số: `Customer` ---(1)--- `<Requests_Return>` ---(N)--- `[orders]`.
        *   Thuộc tính gắn trên hình thoi: `return_request_id` (PK), `request_type`, `reason_code`, `description`, `evidence_url`, `requested_quantity`, `resolution_type`, `refund_amount`, `status`, `decision_reason`, `resolved_at`, `created_at`, `updated_at`.

### 2.4. Nhóm Bảng Giao vận & Thanh toán (Chuyển thành mối quan hệ)

1.  **Bảng `delivery_trips` (Chuyến đi giao hàng)**:
    *   *Bản chất SQL*: Chuyến xe do tài xế giao gom nhiều đơn hàng.
    *   *Chuyển đổi Chen ERD*: Vẽ mối quan hệ Nhiều-Nhiều **`<delivery_trips>`** trực tiếp giữa **`[Delivery]`** (Shipper) và **`[orders]`** (Đơn hàng).
        *   Bản số: `Delivery` ---(1)--- `<delivery_trips>` ---(N)--- `[orders]`.
        *   Thuộc tính gắn trên hình thoi: `trip_id` (PK), `status`, `estimated_start_time`, `estimated_end_time`, `created_at`, `updated_at`.
2.  **Bảng `deliveries` (Chi tiết lượt giao)**:
    *   *Bản chất SQL*: Bảng phụ lưu trạng thái giao hàng của đơn con.
    *   *Chuyển đổi Chen ERD*: Vẽ mối quan hệ **`<Shipped_By>`** (`1:1`) gắn trực tiếp vào thực thể **`[orders]`** (Đơn hàng con).
        *   Tên hình thoi: `<Shipped_By>`
        *   Thuộc tính gắn trên hình thoi: `delivery_id` (PK), `trip_stop_seq`, `status`, `picked_up_at`, `delivered_at`, `failure_reason`, `proof_image_url`, `estimated_delivery_time`, `created_at`, `updated_at`.
3.  **Bảng `payment_transactions` (Giao dịch thanh toán)**:
    *   *Bản chất SQL*: Giao dịch thanh toán của đơn hàng.
    *   *Chuyển đổi Chen ERD*: Vẽ mối quan hệ **`<Paid_Via>`** (`1:1`) gắn trực tiếp vào thực thể **`[orders]`**.
        *   Tên hình thoi: `<Paid_Via>`
        *   Thuộc tính gắn trên hình thoi: `transaction_id` (PK), `payment_method`, `sepay_transaction_id`, `sepay_reference`, `sepay_qr_code`, `amount`, `currency`, `status`, `initiated_at`, `completed_at`, `expires_at`, `provider_response`, `error_code`, `error_message`, `ip_address`.

---

## 3. Danh sách các Thực thể Thực sự (Hình chữ nhật) & Thuộc tính gốc

Chỉ có các thực thể cốt lõi sau đây được vẽ dưới dạng hình chữ nhật:

1.  **`users`**: <u>`user_id`</u>, `full_name`, `email`, `password_hash`, `phone`, `status`, `user_address`, `avatar_url`, `is_email_verified`, `failed_login_count`, `locked_until`, `created_at`, `updated_at`.
2.  **`shop_owner_profiles`**: <u>`profile_id`</u>, `shop_name`, `shop_description`, `approval_status`, `rejection_reason`, `approved_at`, `delivery_address`, `rating`, `preferred_categories`, `doc_paths`, `business_email`, `logo_url`, `cover_url`, `created_at`, `updated_at` (Mối quan hệ `Owns_Shop` 1:1 với subclass `Shop Owner`).
3.  **`categories`**: <u>`category_id`</u>, `name`, `slug`, `display_order`, `is_active`.
4.  **`products`**: <u>`product_id`</u>, `name`, `description`, `origin_country`, `origin_region`, `harvest_date`, `shelf_life_days`, `storage_instruction`, `status`, `view_count`, `rating`, `sold_quantity`, `is_organic`, `is_imported`, `season_start_month`, `season_end_month`, `approval_status`, `verification_doc_path`, `rejection_reason`, `created_at`, `updated_at`.
5.  **`product_variants`**: <u>`variant_id`</u>, `sku`, `variant_label`, `price`, `stock_quantity`, `weight_kg`, `discount_price`, `discount_start`, `discount_end`, `is_active`, `created_at`, `updated_at` (Liên kết `1:N` với `products` qua mối quan hệ `<Has_Variant>`).
6.  **`promotions`**: <u>`promo_id`</u>, `code`, `discount_type`, `discount_scope`, `discount_max`, `discount_value`, `min_order_value`, `scope`, `max_uses`, `used_count`, `can_stack`, `valid_from`, `valid_until`, `created_at`, `updated_at`, `is_deleted`, `is_active`.
7.  **`cart`**: <u>`cart_id`</u>, `created_at`, `updated_at`.
8.  **`orders`**: <u>`order_id`</u>, `order_type`, `delivery_address`, `recipient_name`, `recipient_phone`, `delivery_time_slot`, `notes`, `cancelled_at`, `cancellation_reason`, `status`, `total_amount`, `delivery_fee`, `discount_amount`, `system_discount_amount`, `shop_discount_amount`, `platform_fee`, `final_amount`, `payment_method`, `refund_status`, `shop_acceptance_deadline`, `shop_accepted_at`, `created_at`, `updated_at`.
9.  **`shop_settlements`**: <u>`settlement_id`</u>, `period_start`, `period_end`, `gross_amount`, `platform_fee_amount`, `refund_amount`, `adjustment_amount`, `net_amount`, `status`, `calculated_at`, `confirmed_at`, `paid_at`, `note`.
10. **`chat_sessions`**: <u>`session_id`</u>, `session_type`, `status`, `created_at`, `updated_at`, `closed_at`.
11. **`system_config`**: <u>`config_key`</u>, `config_value`, `description`, `data_type`, `effective_date`, `previous_value`, `changed_at`, `updated_at`.
12. **`sepay_webhook_dedup`**: <u>`dedup_id`</u>, `sepay_transaction_id`, `order_code`, `process_result`, `created_at`.
13. **`audit_logs`**: <u>`log_id`</u>, `action`, `target_type`, `target_id`, `detail`, `ip_address`, `created_at`.

---

## 4. Bảng tra cứu mối nối thực thể dạng Chen tinh gọn

| Thực thể A | Mối quan hệ (Hình thoi) | Thực thể B | Bản số (A:B) | Thuộc tính treo trên hình thoi mối quan hệ |
| :--- | :--- | :--- | :---: | :--- |
| `users` | `<Has_Session>` | *(Không có)* | 1:N | `session_id` (PK), `token`, `expires_at` |
| `Customer` | `<Has_Address>` | *(Không có)* | 1:N | `address_id` (PK), `recipient_name`, `recipient_phone`, `address_detail`, `is_default` |
| `Shop Owner` | `<Owns_Shop>` | `shop_owner_profiles` | 1:1 | *(Không có)* |
| `users` | `<Receives_Notification>` | *(Không có)* | 1:N | `notification_id` (PK), `type`, `title`, `message`, `action_url` |
| `Shop Owner` | `<Manages_Product>` | `products` | 1:N | *(Không có)* |
| `categories` | `<Contains>` | `products` | 1:N | *(Không có)* |
| `products` | `<Has_Image>` | *(Không có)* | 1:N | `image_id` (PK), `file_path`, `display_order`, `is_primary` |
| `products` | `<Has_Variant>` | `product_variants` | 1:N | *(Không có)* |
| `products` | `<Offers_Packaging>` | *(Không có)* | 1:N | `packaging_id` (PK), `label`, `price_add` |
| `users` | `<Updates_Inventory>` | `product_variants` | N:M | `log_id` (PK), `change_type`, `quantity_delta`, `quantity_after`, `changed_at` |
| `users` | `<Creates_Promo>` | `promotions` | 1:N | *(Không có)* |
| `products` | `<Applies_Promo>` | `promotions` | 1:N | *(Không có)* |
| `Customer` | `<Has_Cart>` | `cart` | 1:1 | *(Không có)* |
| `cart` | `<Contains_Item>` | `product_variants` | M:N | `cart_item_id` (PK), `quantity`, `added_at` |
| `Customer` | `<Places_Order>` | `orders` | 1:N | *(Không có)* |
| `Shop Owner` | `<Receives_Order>` | `orders` | 1:N | *(Không có)* |
| `users` | `<Cancels_Order>` | `orders` | 1:N | *(Không có)* |
| `orders` (Cha) | `<Has_Child_Order>` | `orders` (Con) | 1:N | *(Không có)* |
| `orders` | `<Contains_Order_Item>` | `product_variants` | M:N | `order_item_id` (PK), `product_name_snapshot`, `quantity`, `unit_price`, `subtotal` |
| `orders` | `<Uses_Promotion>` | `promotions` | M:N | `usage_id` (PK), `discount_applied`, `used_at` |
| `orders` | `<Paid_Via>` | *(Không có)* | 1:1 | `transaction_id` (PK), `payment_method`, `amount`, `status` |
| `Customer` | `<Requests_Return>` | `orders` | M:N | `return_request_id` (PK), `request_type`, `reason_code`, `refund_amount`, `status` |
| `Shop Owner` | `<Settled_By>` | `shop_settlements` | 1:N | *(Không có)* |
| `shop_settlements` | `<Settles_Order>` | `orders` | M:N | `settlement_order_id` (PK), `order_amount`, `net_amount` |
| `Delivery` | `<delivery_trips>` | `orders` | M:N | `trip_id` (PK), `status`, `estimated_start_time` |
| `orders` | `<Shipped_By>` | *(Không có)* | 1:1 | `delivery_id` (PK), `status`, `picked_up_at`, `delivered_at` |
| `Customer` | `<Writes_Review>` | `product_variants` | M:N | `review_id` (PK), `rating`, `review_text`, `created_at` |
| `Customer` | `<Chats_As_Customer>` | `chat_sessions` | 1:N | *(Không có)* |
| `Shop Owner` | `<Chats_As_Shop>` | `chat_sessions` | 1:N | *(Không có)* |
| `users` | `<Sends_Message>` | `chat_sessions` | M:N | `message_id` (PK), `content`, `media_url`, `is_read` |
| `Admin` | `<Modifies_Config>` | `system_config` | 1:N | *(Không có)* |
| `users` | `<Generates_Audit>` | `audit_logs` | 1:N | *(Không có)* |

# Phân Tích Cấu Trúc Hàm DAO và Chức Năng Các Trang JSP

Báo cáo chi tiết về cấu trúc các lớp DAO, các phương thức hiện có và các trang JSP trong dự án `Ban_hoa_qua_online`. Mục tiêu là xác định sự trùng lặp, tối ưu hóa phân chia nhiệm vụ và đưa ra khuyến nghị chuẩn hóa.

## 1. Danh Sách Lớp và Phương Thức DAO (Data Access Object)
Dưới đây là danh sách các phương thức được định nghĩa trong từng lớp DAO để quản lý truy vấn SQL:

### [BaseDAO](file:///src/java/dao/system/BaseDAO.java)
- **Package**: `dao.system`
- **Đường dẫn**: `src/java/dao/system/BaseDAO.java`
- **Các phương thức chính**:
  * `Product findById(int id)`
  * `Order placeOrder(Order order, List<OrderItem> items)`
  * `Connection getConnection()`
  * `<T> T withTransaction(TransactionalWork<T> work)`
  * `void runInTransaction(TransactionalAction action)`

### [CartDAO](file:///src/java/dao/cart/CartDAO.java)
- **Package**: `dao.cart`
- **Đường dẫn**: `src/java/dao/cart/CartDAO.java`
- **Các phương thức chính**:
  * `List<Cart> findByCustomer(int customerId)`
  * `int createForCustomer(int customerId)`
  * `Cart addItem(int cartId, int variantId, int quantity)`
  * `Cart addItem(int cartId, int variantId, int quantity, Integer packagingId)`
  * `void updateItemQuantity(int cartItemId, int quantity)`
  * `void updateItemVariant(int cartItemId, int newVariantId)`
  * `Cart removeItem(int cartItemId)`
  * `Cart clearCart(int cartId)`
  * `void deleteItemsByCustomer(Connection conn, int customerId, List<Integer> variantIds)`
  * `List<CartItem> findItems(int cartId)`
  * `CartItem findItemById(int cartItemId)`
  * `void replaceCartItems(int cartId, List<CartItem> items)`

### [CategoryDAO](file:///src/java/dao/catalog/CategoryDAO.java)
- **Package**: `dao.catalog`
- **Đường dẫn**: `src/java/dao/catalog/CategoryDAO.java`
- **Các phương thức chính**:
  * `List<Category> findById(int id)`
  * `List<Category> findAll()`
  * `List<Category> findAllActive()`
  * `int save(Category category)`
  * `void update(Category category)`
  * `void delete(int categoryId)`
  * `boolean hasActiveProducts(int categoryId)`

### [ChatDAO](file:///src/java/dao/chat/ChatDAO.java)
- **Package**: `dao.chat`
- **Đường dẫn**: `src/java/dao/chat/ChatDAO.java`
- **Các phương thức chính**:
  * `List<ChatSession> findSessionByParticipants(int customerId, int ownerId)`
  * `ChatSession findSessionById(int sessionId)`
  * `List<ChatSession> findSessionsByCustomer(int customerId)`
  * `List<ChatSession> findSessionsByOwner(int ownerId)`
  * `List<ChatSession> findAllSessions()`
  * `int createSession(int customerId, int ownerId)`
  * `int createSession(int customerId, int ownerId, String sessionType)`
  * `int countUnread(int sessionId, int readerId)`
  * `int countTotalUnread(int userId)`
  * `int saveMessage(ChatMessage msg)`
  * `List<ChatMessage> findMessages(int sessionId)`
  * `void markRead(int sessionId, int readerId)`

### [ConnectionPool](file:///src/java/dao/system/ConnectionPool.java)
- **Package**: `dao.system`
- **Đường dẫn**: `src/java/dao/system/ConnectionPool.java`
- **Các phương thức chính**:
  * `static Connection getConnection()`
  * `static boolean isPoolActive()`
  * `static void logPoolStats()`

### [DeliveryDAO](file:///src/java/dao/order/DeliveryDAO.java)
- **Package**: `dao.order`
- **Đường dẫn**: `src/java/dao/order/DeliveryDAO.java`
- **Các phương thức chính**:
  * `Delivery findById(int deliveryId)`
  * `List<Delivery> findByStaffId(int staffId)`
  * `void updateStatusAndProof(int deliveryId, String status, String failureReason, String proofImageUrl)`
  * `void updateEstimatedTime(int deliveryId, java.time.LocalDateTime estimatedTime)`
  * `void assignShipper(int orderId, int staffId, java.time.LocalDateTime estimatedTime)`
  * `void assignShipper(int orderId, Integer deliveryTripId, Integer tripStopSeq, int staffId, java.time.LocalDateTime estimatedTime)`
  * `void assignShipper(Connection conn, int orderId, Integer deliveryTripId, Integer tripStopSeq, int staffId, java.time.LocalDateTime estimatedTime)`
  * `boolean claimDelivery(int deliveryId, int staffId)`
  * `Delivery findByOrderId(int orderId)`
  * `Map<Integer, Delivery> findByOrderIds(Collection<Integer> orderIds)`
  * `List<Delivery> findAll()`
  * `List<Delivery> findUnassigned()`

### [DeliveryTripDAO](file:///src/java/dao/order/DeliveryTripDAO.java)
- **Package**: `dao.order`
- **Đường dẫn**: `src/java/dao/order/DeliveryTripDAO.java`
- **Các phương thức chính**:
  * `int save(Connection conn, int parentOrderId, Integer shipperId, String status, LocalDateTime estimatedStartTime, LocalDateTime estimatedEndTime)`
  * `DeliveryTrip findById(int tripId)`
  * `List<DeliveryTrip> findByParentOrderId(int parentOrderId)`
  * `void assignShipper(int tripId, int shipperId)`
  * `void updateStatus(int tripId, String status)`

### [InventoryDAO](file:///src/java/dao/catalog/InventoryDAO.java)
- **Package**: `dao.catalog`
- **Đường dẫn**: `src/java/dao/catalog/InventoryDAO.java`
- **Các phương thức chính**:
  * `Connection openConnection()`
  * `int save(Connection conn, InventoryLog log)`
  * `int save(InventoryLog log)`
  * `List<InventoryLog> findByOwner(int ownerId)`
  * `List<InventoryLog> findByVariant(int variantId)`
  * `List<InventoryLog> findExpiredLogs(Connection conn)`
  * `void markLogExpired(Connection conn, int logId)`

### [NotificationDAO](file:///src/java/dao/chat/NotificationDAO.java)
- **Package**: `dao.chat`
- **Đường dẫn**: `src/java/dao/chat/NotificationDAO.java`
- **Các phương thức chính**:
  * `List<Notification> findByUser(int userId, boolean unreadOnly)`
  * `int save(Notification notif)`
  * `boolean hasUnreadChatNotification(int userId, int sessionId)`
  * `void markRead(int notifId)`
  * `List<Notification> findAllSystemNotifications()`
  * `void insertForRole(String title, String message, String role)`
  * `void markAllRead(int userId)`
  * `void insertForAll(String title, String message)`

### [OrderDAO](file:///src/java/dao/order/OrderDAO.java)
- **Package**: `dao.order`
- **Đường dẫn**: `src/java/dao/order/OrderDAO.java`
- **Các phương thức chính**:
  * `List<Order> findById(int id)`
  * `Order findOneById(int id)`
  * `List<Order> findByCustomerId(int customerId)`
  * `List<Order> findByCustomer(int customerId, int page, int pageSize)`
  * `List<Order> findByCustomer(int customerId, String status, int page, int pageSize)`
  * `List<Order> findByOwner(int ownerId, String status, int page, int pageSize)`
  * `int countAll(String status)`
  * `List<Order> findAll(String status, int page, int pageSize)`
  * `int countAll(String status, String paymentMethod, String paymentStatus)`
  * `List<Order> findAll(String status, String paymentMethod, String paymentStatus, int page, int pageSize)`
  * `int save(Order order)`
  * `int save(Connection conn, Order order)`
  * `void updateStatus(int orderId, String status)`
  * `void updateRefundStatus(int orderId, String refundStatus)`
  * `void cancel(int orderId, int cancelledBy, String reason)`
  * `void restoreInventoryStock(int orderId)`
  * `void restoreItemInventoryStock(int orderItemId, int quantity)`
  * `int getOwnerIdByProductId(int productId)`
  * `Connection openConnection()`
  * `void updatePlatformFee(Connection conn, int orderId, java.math.BigDecimal platformFee)`
  * `Order findByIdForCustomer(int orderId, int customerId)`
  * `Order findByIdForOwner(int orderId, int ownerId)`
  * `int countByCustomer(int customerId)`
  * `int countByCustomer(int customerId, String status)`
  * `int countByOwner(int ownerId, String status)`
  * `List<OrderItem> findItemsByOrderId(int orderId)`
  * `Map<Integer, List<OrderItem>> findItemsByOrderIds(Collection<Integer> orderIds)`
  * `List<Map<String, Object>> getRevenueTrend(Integer ownerId, String startDate, String endDate, Integer categoryId)`
  * `List<Map<String, Object>> getOrderStatusStats(Integer ownerId, String startDate, String endDate, Integer categoryId)`
  * `List<Map<String, Object>> getCancellationReasonStats(Integer ownerId, String startDate, String endDate, Integer categoryId)`
  * `List<Map<String, Object>> getFruitUsageReport(Integer ownerId, String startDate, String endDate, Integer categoryId)`
  * `List<Order> findChildrenByParentId(int parentOrderId)`
  * `Map<Integer, Order> findByIds(Collection<Integer> orderIds)`
  * `void updateReceivedStatus(int orderId, String receivedStatus)`

### [OrderItemDAO](file:///src/java/dao/order/OrderItemDAO.java)
- **Package**: `dao.order`
- **Đường dẫn**: `src/java/dao/order/OrderItemDAO.java`
- **Các phương thức chính**:
  * `List<OrderItem> findByOrderId(int orderId)`
  * `void saveBatch(Connection conn, int orderId, List<CartItem> items, Map<Integer, ProductVariant> variantMap)`

### [PaymentDAO](file:///src/java/dao/shop/PaymentDAO.java)
- **Package**: `dao.shop`
- **Đường dẫn**: `src/java/dao/shop/PaymentDAO.java`
- **Các phương thức chính**:
  * `int initTransaction(int orderId, String paymentMethod, BigDecimal amount, String sepayReference, String ipAddress, LocalDateTime expiresAt)`
  * `List<PaymentTransaction> findByOrder(int orderId)`
  * `PaymentTransaction findOneByOrder(int orderId)`
  * `Map<Integer, PaymentTransaction> findByOrderIds(Collection<Integer> orderIds)`
  * `List<PaymentTransaction> findBySepayTxId(String txId)`
  * `void updateStatus(int transactionId, String status, String sepayTransactionId, String providerResponse)`
  * `void updateStatus(int transactionId, String status)`
  * `void updateStatusFailed(int transactionId, String errorCode, String errorMessage)`
  * `boolean isDuplicate(String sepayTxId)`
  * `void insertDedup(String sepayTxId, String orderCode, String processResult)`
  * `PaymentTransaction findByReference(String sepayReference)`
  * `List<PaymentTransaction> findByCustomer(int customerId)`
  * `List<Map<String, Object>> findAdminPayments(String status, String paymentMethod, String keyword, int page, int pageSize)`
  * `int countAdminPayments(String status, String paymentMethod, String keyword)`

### [ProductDAO](file:///src/java/dao/catalog/ProductDAO.java)
- **Package**: `dao.catalog`
- **Đường dẫn**: `src/java/dao/catalog/ProductDAO.java`
- **Các phương thức chính**:
  * `List<Product> findById(int id)`
  * `Product findOneById(int id)`
  * `List<Product> findAll(int page, int pageSize)`
  * `List<Product> findByOwner(int ownerId)`
  * `Map<Integer, Product> findActiveByOwner(int ownerId)`
  * `List<Product> findByCategory(int categoryId, int page, int pageSize)`
  * `List<Product> findFlashSaleProducts()`
  * `List<Product> search(String keyword, Integer categoryId, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, int page, int pageSize)`
  * `int countSearch(String keyword, Integer categoryId, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice)`
  * `int save(Product product)`
  * `void update(Product product)`
  * `void updateStatus(int productId, String status)`
  * `void updateHarvestDateAndStatus(Connection conn, int productId, java.time.LocalDate harvestDate, String status)`
  * `void createRestockNotification(int ownerId, int customerId, int productId, String productName)`
  * `boolean hasRequestedRestockToday(int ownerId, int customerId, int productId)`
  * `void autoDeactivateExpiredProducts()`
  * `void deleteProduct(int productId)`
  * `void incrementViewCount(int productId)`
  * `int getProductIdByOrderItem(int orderItemId)`
  * `int getProductIdByReview(int reviewId)`
  * `void recalculateRating(int productId)`
  * `List<Product> findSimilarProducts(int productId, int categoryId, int limit)`
  * `List<Product> findByOwnerAndActiveStatus(int ownerId, int excludeProductId, int limit)`
  * `int getLowStockCountByOwner(int ownerId, int threshold)`
  * `List<Map<String, Object>> getLowStockVariantsByOwner(int ownerId, int threshold)`
  * `List<Product> findPendingProducts()`
  * `List<Product> findAllAdminProducts(int page, int pageSize, String approvalStatusFilter)`
  * `boolean updateApprovalStatus(int productId, String approvalStatus, String rejectionReason, boolean isOrganic, boolean isImported, int categoryId)`
  * `boolean banProduct(int productId)`
  * `List<Product> findNextProducts(int lastProductId, int limit)`
  * `List<Product> findAllActiveForAI()`
  * `List<Map<String, Object>> findAllActiveBriefForAI()`
  * `List<Map<String, Object>> findBriefProductsByIds(List<Integer> ids)`
  * `Map<Integer, Product> findByIds(Collection<Integer> ids)`
  * `List<Map<String, Object>> findFlashSaleProductsOptimized(String contextPath)`
  * `List<Map<String, Object>> findBestSellersOptimized(int limit, String contextPath)`
  * `List<Map<String, Object>> findSeasonalProductsOptimized(int limit, String contextPath)`
  * `List<Map<String, Object>> findOrganicProductsOptimized(int limit, String contextPath)`
  * `List<Map<String, Object>> findImportedProductsOptimized(int limit, String contextPath)`
  * `List<Map<String, Object>> searchProductsOptimized(String keyword, Integer categoryId, int page, int pageSize, String contextPath)`

### [ProductImageDAO](file:///src/java/dao/catalog/ProductImageDAO.java)
- **Package**: `dao.catalog`
- **Đường dẫn**: `src/java/dao/catalog/ProductImageDAO.java`
- **Các phương thức chính**:
  * `List<ProductImage> findByProduct(int productId)`
  * `ProductImage findById(int imageId)`
  * `ProductImage findPrimary(int productId)`
  * `Map<Integer, ProductImage> findPrimaryByProductIds(Collection<Integer> productIds)`
  * `int save(ProductImage image)`
  * `void delete(int imageId)`
  * `void updateDisplayOrder(int imageId, int order)`
  * `void setPrimary(int imageId, int productId)`

### [ProductPackagingOptionDAO](file:///src/java/dao/catalog/ProductPackagingOptionDAO.java)
- **Package**: `dao.catalog`
- **Đường dẫn**: `src/java/dao/catalog/ProductPackagingOptionDAO.java`
- **Các phương thức chính**:
  * `List<ProductPackagingOption> findByProduct(int productId)`
  * `ProductPackagingOption findById(int packagingId)`
  * `int save(ProductPackagingOption option)`
  * `void deleteByProductExcept(int productId, List<Integer> keepIds)`
  * `void update(ProductPackagingOption option)`

### [ProductVariantDAO](file:///src/java/dao/catalog/ProductVariantDAO.java)
- **Package**: `dao.catalog`
- **Đường dẫn**: `src/java/dao/catalog/ProductVariantDAO.java`
- **Các phương thức chính**:
  * `ProductVariant findById(int id)`
  * `List<ProductVariant> findByProduct(int productId)`
  * `Map<Integer, List<ProductVariant>> findByProductIds(Collection<Integer> productIds)`
  * `ProductVariant findBySku(String sku)`
  * `int save(ProductVariant variant)`
  * `void update(ProductVariant variant)`
  * `void updateStock(int variantId, int delta)`
  * `void updateStock(Connection conn, int variantId, int delta)`
  * `void deactivate(int variantId)`
  * `int getStockQuantity(int variantId)`
  * `int getStockQuantity(Connection conn, int variantId)`
  * `int decrementStockQuantity(Connection conn, int variantId, int qty)`
  * `int getProductId(Connection conn, int variantId)`
  * `int getProductOwnerId(Connection conn, int variantId)`
  * `int getProductOwnerId(int variantId)`
  * `Map<String, Object> getVariantAndProductName(Connection conn, int variantId)`

### [PromotionDAO](file:///src/java/dao/shop/PromotionDAO.java)
- **Package**: `dao.shop`
- **Đường dẫn**: `src/java/dao/shop/PromotionDAO.java`
- **Các phương thức chính**:
  * `Promotion findById(int id)`
  * `Promotion findByCode(String code)`
  * `Promotion findAnyByCode(String code)`
  * `List<Promotion> findByOwner(int ownerId)`
  * `List<Promotion> findGlobalPromotions()`
  * `List<Promotion> findActivePromotionsByProduct(int productId)`
  * `Map<Integer, Promotion> findActivePromotionsByProductIds(Collection<Integer> productIds)`
  * `int save(Promotion promo)`
  * `void update(Promotion promo)`
  * `void incrementUsedCount(int promoId)`
  * `void incrementUsedCount(Connection conn, int promoId)`
  * `List<Promotion> findShopActivePromotions(int ownerId)`
  * `List<Promotion> findActiveSystemPromotions()`
  * `void deactivate(int promoId)`
  * `void softDelete(int promoId)`
  * `Promotion findValidShopCoupon(String code, int ownerId, java.math.BigDecimal subtotal)`
  * `Promotion findValidSystemCoupon(String code, java.math.BigDecimal subtotal)`
  * `void saveOrderPromotion(Connection conn, int orderId, int promoId, int customerId, java.math.BigDecimal discountApplied)`

### [ReturnRequestDAO](file:///src/java/dao/order/ReturnRequestDAO.java)
- **Package**: `dao.order`
- **Đường dẫn**: `src/java/dao/order/ReturnRequestDAO.java`
- **Các phương thức chính**:
  * `ReturnRequest findById(int id)`
  * `List<ReturnRequest> findByOrder(int orderId)`
  * `List<ReturnRequest> findByCustomer(int customerId)`
  * `List<ReturnRequest> findPending()`
  * `List<ReturnRequest> findAll()`
  * `List<ReturnRequest> findAll(String status, int page, int pageSize)`
  * `int countAll(String status)`
  * `List<ReturnRequest> findByOwner(int ownerId)`
  * `int save(ReturnRequest req)`
  * `void updateStatus(int id, String status, String decisionReason, int decidedBy)`

### [ReviewDAO](file:///src/java/dao/order/ReviewDAO.java)
- **Package**: `dao.order`
- **Đường dẫn**: `src/java/dao/order/ReviewDAO.java`
- **Các phương thức chính**:
  * `List<Review> findByOrderItem(int orderItemId)`
  * `List<Review> findByProduct(int productId)`
  * `List<Review> findByProductPaginated(int productId, Integer ratingFilter, int page, int pageSize)`
  * `int countByProductAndRating(int productId, Integer ratingFilter)`
  * `Map<Integer, Integer> getRatingDistribution(int productId)`
  * `boolean existsByCustomerAndItem(int customerId, int orderItemId)`
  * `int save(Review review)`
  * `List<Review> findAllForAdmin()`
  * `void updateVisibility(int reviewId, boolean isHidden)`
  * `void update(Review review)`
  * `void delete(int reviewId)`
  * `Review findById(int reviewId)`
  * `Review findByOrderItemId(int orderItemId)`
  * `void hide(int reviewId)`

### [SettlementDAO](file:///src/java/dao/shop/SettlementDAO.java)
- **Package**: `dao.shop`
- **Đường dẫn**: `src/java/dao/shop/SettlementDAO.java`
- **Các phương thức chính**:
  * `List<ShopSettlement> findAll(String status, int page, int pageSize)`
  * `int countAll(String status)`
  * `void markPaid(int settlementId)`
  * `List<ShopSettlement> findByOwner(int ownerId)`
  * `int runAutoSettlement(int freezeDays)`
  * `ShopSettlement findById(int settlementId)`

### [ShopProfileDAO](file:///src/java/dao/shop/ShopProfileDAO.java)
- **Package**: `dao.shop`
- **Đường dẫn**: `src/java/dao/shop/ShopProfileDAO.java`
- **Các phương thức chính**:
  * `List<ShopProfile> findByUserId(int userId)`
  * `ShopProfile findOneByUserId(int userId)`
  * `ShopProfile findById(int profileId)`
  * `List<ShopProfile> findAll(String approvalStatus)`
  * `List<ShopProfile> findAll()`
  * `List<ShopProfile> findByApprovalStatus(String status)`
  * `Map<Integer, ShopProfile> findByUserIds(Collection<Integer> userIds)`
  * `int save(ShopProfile profile)`
  * `void update(ShopProfile profile)`
  * `void updateApprovalStatus(int profileId, int userId, String status, String rejectionReason)`
  * `void updateDocPaths(int profileId, String jsonDocPaths)`
  * `boolean isBusinessEmailExists(String businessEmail)`

### [SystemConfigDAO](file:///src/java/dao/system/SystemConfigDAO.java)
- **Package**: `dao.system`
- **Đường dẫn**: `src/java/dao/system/SystemConfigDAO.java`
- **Các phương thức chính**:
  * `String getValue(String key)`
  * `double getDouble(String key, double defaultVal)`
  * `int getInt(String key, int defaultVal)`
  * `void updateConfigWithHistory(Connection conn, String key, String newValue, LocalDateTime effectiveDate, int changedBy, String reason)`
  * `List<Map<String, Object>> getHistory(String key, int limit)`
  * `List<Map<String, Object>> findAll()`
  * `Connection openConnection()`

### [UserAddressDAO](file:///src/java/dao/auth/UserAddressDAO.java)
- **Package**: `dao.auth`
- **Đường dẫn**: `src/java/dao/auth/UserAddressDAO.java`
- **Các phương thức chính**:
  * `List<UserAddress> findByUser(int userId)`
  * `UserAddress findById(int addressId)`
  * `boolean save(UserAddress addr)`
  * `boolean update(UserAddress addr)`
  * `boolean delete(int addressId)`
  * `void clearDefault(int userId)`

### [UserDAO](file:///src/java/dao/auth/UserDAO.java)
- **Package**: `dao.auth`
- **Đường dẫn**: `src/java/dao/auth/UserDAO.java`
- **Các phương thức chính**:
  * `List<User> findById(int id)`
  * `User findByEmail(String email)`
  * `User findByPhone(String phone)`
  * `User findByLoginIdentifier(String identifier)`
  * `User registerExternalUser(String email, String fullName)`
  * `List<User> searchUsers(String role, String keyword, int offset, int limit)`
  * `int countUsers(String role, String keyword)`
  * `List<User> findAll()`
  * `int saveNewCustomer(String fullName, String email, String passwordHash, String phone, String role)`
  * `int saveNewCustomer(String fullName, String email, String passwordHash, String phone, String role, String status, boolean emailVerified)`
  * `int saveNewCustomer(String fullName, String email, String passwordHash, String phone, String role, String status, boolean emailVerified, String avatarUrl)`
  * `void update(User user)`
  * `boolean updateUserStatus(int userId, String status)`
  * `void updateRole(int userId, String role)`
  * `void updatePassword(int userId, String newHash)`
  * `void clearForgotPasswordCode(int userId)`
  * `void saveEmailVerificationCode(int userId, String codeHash, Timestamp expiresAt, Timestamp resendAt)`
  * `void activateVerifiedEmail(int userId)`
  * `void incrementFailedLogin(int userId)`
  * `void resetFailedLogin(int userId)`
  * `void lockAccount(int userId, java.time.LocalDateTime until)`
  * `User findUserById(int id)`
  * `User findActiveAdminById(int id)`
  * `User findFirstActiveAdmin()`
  * `Map<Integer, User> findByIds(Collection<Integer> ids)`
  * `void saveUserSession(int userId, String token, java.sql.Timestamp expiresAt)`
  * `Integer findUserIdBySessionToken(String token)`
  * `void deleteSessionToken(String token)`
  * `void deleteUserSession(String token)`
  * `void deleteAllSessions()`
  * `void deleteSessionsByUserId(int userId)`
  * `void deleteUser(int userId)`
  * `List<User> findActiveShopOwners()`
  * `List<Map<String, Object>> getUserRegistrationTrend(String startDate, String endDate)`
  * `void updateAvatar(int userId, String avatarUrl)`
  * `boolean isPhoneExists(String phone, int excludeUserId)`

## 2. Phân Nhóm Các Trang JSP Theo Vai Trò (Actor)
Các trang giao diện được tổ chức trong thư mục `web/WEB-INF/jsp/` theo các actor chính:

### Thư mục `jsp/admin`
- [admin-categories.jsp](file:///web/WEB-INF/jsp/admin/admin-categories.jsp)
- [admin-config.jsp](file:///web/WEB-INF/jsp/admin/admin-config.jsp)
- [admin-notifications.jsp](file:///web/WEB-INF/jsp/admin/admin-notifications.jsp)
- [admin-orders.jsp](file:///web/WEB-INF/jsp/admin/admin-orders.jsp)
- [admin-products.jsp](file:///web/WEB-INF/jsp/admin/admin-products.jsp)
- [admin-profile.jsp](file:///web/WEB-INF/jsp/admin/admin-profile.jsp)
- [admin-refunds.jsp](file:///web/WEB-INF/jsp/admin/admin-refunds.jsp)
- [admin-settlements.jsp](file:///web/WEB-INF/jsp/admin/admin-settlements.jsp)
- [admin-shops.jsp](file:///web/WEB-INF/jsp/admin/admin-shops.jsp)
- [category-list.jsp](file:///web/WEB-INF/jsp/admin/category-list.jsp)
- [chat.jsp](file:///web/WEB-INF/jsp/admin/chat.jsp)
- [coming-soon.jsp](file:///web/WEB-INF/jsp/admin/coming-soon.jsp)
- [dashboard.jsp](file:///web/WEB-INF/jsp/admin/dashboard.jsp)
- [order-monitor.jsp](file:///web/WEB-INF/jsp/admin/order-monitor.jsp)
- [orders.jsp](file:///web/WEB-INF/jsp/admin/orders.jsp)
- [payment-dashboard.jsp](file:///web/WEB-INF/jsp/admin/payment-dashboard.jsp)
- [report.jsp](file:///web/WEB-INF/jsp/admin/report.jsp)
- [return-requests.jsp](file:///web/WEB-INF/jsp/admin/return-requests.jsp)
- [review-management.jsp](file:///web/WEB-INF/jsp/admin/review-management.jsp)
- [settlement-manage.jsp](file:///web/WEB-INF/jsp/admin/settlement-manage.jsp)
- [shop-approval-list.jsp](file:///web/WEB-INF/jsp/admin/shop-approval-list.jsp)
- [shop-approvals.jsp](file:///web/WEB-INF/jsp/admin/shop-approvals.jsp)
- [user-list.jsp](file:///web/WEB-INF/jsp/admin/user-list.jsp)
- [user-view.jsp](file:///web/WEB-INF/jsp/admin/user-view.jsp)

### Thư mục `jsp/auth`
- [change-password.jsp](file:///web/WEB-INF/jsp/auth/change-password.jsp)
- [forgot-password.jsp](file:///web/WEB-INF/jsp/auth/forgot-password.jsp)
- [login.jsp](file:///web/WEB-INF/jsp/auth/login.jsp)
- [register.jsp](file:///web/WEB-INF/jsp/auth/register.jsp)
- [reset-password.jsp](file:///web/WEB-INF/jsp/auth/reset-password.jsp)
- [verify.jsp](file:///web/WEB-INF/jsp/auth/verify.jsp)

### Thư mục `jsp/common`
- [admin-sidebar.jsp](file:///web/WEB-INF/jsp/common/admin-sidebar.jsp)
- [alert.jsp](file:///web/WEB-INF/jsp/common/alert.jsp)
- [error.jsp](file:///web/WEB-INF/jsp/common/error.jsp)
- [footer.jsp](file:///web/WEB-INF/jsp/common/footer.jsp)
- [header.jsp](file:///web/WEB-INF/jsp/common/header.jsp)
- [navbar.jsp](file:///web/WEB-INF/jsp/common/navbar.jsp)
- [profile.jsp](file:///web/WEB-INF/jsp/common/profile.jsp)
- [shop-sidebar.jsp](file:///web/WEB-INF/jsp/common/shop-sidebar.jsp)

### Thư mục `jsp/customer`
- [cart.jsp](file:///web/WEB-INF/jsp/customer/cart.jsp)
- [chat.jsp](file:///web/WEB-INF/jsp/customer/chat.jsp)
- [checkout.jsp](file:///web/WEB-INF/jsp/customer/checkout.jsp)
- [dashboard.jsp](file:///web/WEB-INF/jsp/customer/dashboard.jsp)
- [invoice.jsp](file:///web/WEB-INF/jsp/customer/invoice.jsp)
- [notification.jsp](file:///web/WEB-INF/jsp/customer/notification.jsp)
- [notifications.jsp](file:///web/WEB-INF/jsp/customer/notifications.jsp)
- [order-detail.jsp](file:///web/WEB-INF/jsp/customer/order-detail.jsp)
- [order-payment.jsp](file:///web/WEB-INF/jsp/customer/order-payment.jsp)
- [order-reviews.jsp](file:///web/WEB-INF/jsp/customer/order-reviews.jsp)
- [order-success.jsp](file:///web/WEB-INF/jsp/customer/order-success.jsp)
- [orders.jsp](file:///web/WEB-INF/jsp/customer/orders.jsp)
- [profile-order-detail.jsp](file:///web/WEB-INF/jsp/customer/profile-order-detail.jsp)
- [return-request.jsp](file:///web/WEB-INF/jsp/customer/return-request.jsp)
- [review-submit.jsp](file:///web/WEB-INF/jsp/customer/review-submit.jsp)
- [review.jsp](file:///web/WEB-INF/jsp/customer/review.jsp)
- [shop-apply.jsp](file:///web/WEB-INF/jsp/customer/shop-apply.jsp)

### Thư mục `jsp/delivery`
- [dashboard.jsp](file:///web/WEB-INF/jsp/delivery/dashboard.jsp)
- [delivery-detail.jsp](file:///web/WEB-INF/jsp/delivery/delivery-detail.jsp)
- [delivery-list.jsp](file:///web/WEB-INF/jsp/delivery/delivery-list.jsp)

### Thư mục `jsp/error`
- [403.jsp](file:///web/WEB-INF/jsp/error/403.jsp)
- [404.jsp](file:///web/WEB-INF/jsp/error/404.jsp)
- [500.jsp](file:///web/WEB-INF/jsp/error/500.jsp)

### Thư mục `jsp/guest`
- [about.jsp](file:///web/WEB-INF/jsp/guest/about.jsp)
- [home.jsp](file:///web/WEB-INF/jsp/guest/home.jsp)
- [product-detail.jsp](file:///web/WEB-INF/jsp/guest/product-detail.jsp)
- [product-list.jsp](file:///web/WEB-INF/jsp/guest/product-list.jsp)

### Thư mục `jsp/shop`
- [chat-inbox.jsp](file:///web/WEB-INF/jsp/shop/chat-inbox.jsp)
- [chat.jsp](file:///web/WEB-INF/jsp/shop/chat.jsp)
- [dashboard.jsp](file:///web/WEB-INF/jsp/shop/dashboard.jsp)
- [inventory.jsp](file:///web/WEB-INF/jsp/shop/inventory.jsp)
- [order-detail.jsp](file:///web/WEB-INF/jsp/shop/order-detail.jsp)
- [orders.jsp](file:///web/WEB-INF/jsp/shop/orders.jsp)
- [product-list.jsp](file:///web/WEB-INF/jsp/shop/product-list.jsp)
- [profile.jsp](file:///web/WEB-INF/jsp/shop/profile.jsp)
- [promotion.jsp](file:///web/WEB-INF/jsp/shop/promotion.jsp)
- [report.jsp](file:///web/WEB-INF/jsp/shop/report.jsp)
- [return-requests.jsp](file:///web/WEB-INF/jsp/shop/return-requests.jsp)
- [settlement.jsp](file:///web/WEB-INF/jsp/shop/settlement.jsp)
- [status.jsp](file:///web/WEB-INF/jsp/shop/status.jsp)
- [view.jsp](file:///web/WEB-INF/jsp/shop/view.jsp)

## 3. Phân Tích Trùng Lặp Chức Năng & Vai Trò
Qua khảo sát danh sách tệp tin và các phương thức, chúng tôi phát hiện một số điểm trùng lặp hoặc chồng chéo nhiệm vụ cần được làm rõ:

### A. Các trang JSP bị trùng lặp hoặc phân mảnh chức năng
1. **Quản lý đơn hàng (Orders) phía Admin:**
   - `admin-orders.jsp`, `orders.jsp`, `order-monitor.jsp` đều nằm dưới thư mục `admin/`.
   - *Khuyến nghị*: Gộp các tính năng giám sát và quản lý trạng thái đơn hàng vào một trang duy nhất hoặc phân định rõ: `admin-orders.jsp` để duyệt/quản lý chung, `order-monitor.jsp` để xem dòng chảy đơn hàng thời gian thực. Bỏ `orders.jsp` dư thừa.

2. **Quản lý Hoàn tiền & Trả hàng phía Admin:**
   - `admin-refunds.jsp` và `return-requests.jsp` đều nằm dưới thư mục `admin/`.
   - *Khuyến nghị*: Quy trình đổi trả/hoàn tiền thường đi liền với nhau. Cần gộp hoặc làm rõ luồng xử lý để tránh code trùng.

3. **Quản lý Cửa hàng (Shop Approval) phía Admin:**
   - `admin-shops.jsp`, `shop-approval-list.jsp`, `shop-approvals.jsp` nằm dưới thư mục `admin/`.
   - *Khuyến nghị*: Tích hợp danh sách cửa hàng và chức năng duyệt cửa hàng mới đăng ký vào chung một dashboard quản lý cửa hàng.

4. **Quản lý Quyết toán (Settlement) phía Admin:**
   - `admin-settlements.jsp` và `settlement-manage.jsp` nằm dưới thư mục `admin/`.
   - *Khuyến nghị*: Gộp thành một trang quản lý quyết toán duy nhất.

5. **Trò chuyện (Chat):**
   - `chat.jsp` xuất hiện ở nhiều thư mục: `admin/chat.jsp`, `customer/chat.jsp`, `shop/chat.jsp` và `shop/chat-inbox.jsp`.
   - *Khuyến nghị*: Sử dụng chung cấu trúc giao diện chat thông qua iframe hoặc WebSocket component dùng chung, chỉ khác biệt về phân quyền dữ liệu load lên.

### B. Các phương thức DAO trùng lặp hoặc chồng chéo
1. **User session management:**
   - `UserDAO.java` chứa các hàm quản lý session: `saveUserSession`, `findUserIdBySessionToken`, `deleteSessionToken`, `deleteUserSession`, `deleteAllSessions`, `deleteSessionsByUserId`.
   - *Đánh giá*: Phù hợp nếu lưu session trong Database, tuy nhiên nên xem xét di chuyển logic này sang một lớp chuyên biệt hoặc tầng Filter/AuthService để `UserDAO` chỉ tập trung vào CRUD thông tin User.
2. **Thao tác Stock trong ProductVariantDAO:**
   - Có nhiều overload cho `updateStock(variantId, delta)` và `updateStock(conn, variantId, delta)` cũng như `getStockQuantity`.
   - *Đánh giá*: Đây là thiết kế tốt hỗ trợ transaction (`Connection conn`). Tuy nhiên, cần đảm bảo các Service gọi đúng hàm nhận `Connection` khi chạy trong transaction để tránh deadlock hoặc mất mát dữ liệu.

## 4. Khuyến Nghị Phân Định Trách Nhiệm (Separation of Concerns)
Để đảm bảo dự án phát triển sạch và dễ bảo trì, cần tuân thủ các nguyên tắc sau:

1. **Controller/Servlet**: Chỉ nhận request, validate input cơ bản, gọi Service thích hợp và forward/redirect. Không gọi trực tiếp DAO từ Servlet nếu đã có lớp Service tương ứng.
2. **Service Layer**: Nơi chứa logic nghiệp vụ, quản lý transaction (commit/rollback) và xử lý dữ liệu trước khi đưa xuống DAO hoặc trả về Servlet.
3. **DAO Layer**: Chỉ chứa mã nguồn SQL và ánh xạ (mapping) dữ liệu từ ResultSet sang Object. Không chứa logic nghiệp vụ.
4. **JSP Pages**: Chỉ làm nhiệm vụ hiển thị (View) sử dụng JSTL và EL. Không nhúng mã Java (`<% ... %>`) vào JSP.
package service.order;

import config.AppConfig;
import dao.order.DeliveryDAO;
import dao.order.DeliveryTripDAO;
import dao.order.OrderDAO;
import model.entity.order.Delivery;
import model.entity.order.Order;
import model.entity.auth.User;
import dao.auth.UserDAO;
import service.chat.NotificationService;
import service.system.EmailService;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import java.util.logging.Logger;
import util.LoggerUtil;

/**
 * DeliveryService — Tầng business logic cho nghiệp vụ tương ứng.
 *
 * QUY TẮC:
 *   - Chỉ gọi DAO, không viết SQL ở đây
 *   - Chứa tất cả validation và business rule
 *   - Ném RuntimeException hoặc custom exception cho Servlet xử lý
 *   - Không tương tác trực tiếp với HttpRequest/Response
 *
 * @author fruitmkt-team
 */
public class DeliveryService {

    private static final Logger log = LoggerUtil.getLogger(DeliveryService.class);

    private final DeliveryDAO deliveryDAO = new DeliveryDAO();
    private final DeliveryTripDAO deliveryTripDAO = new DeliveryTripDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final UserDAO userDAO = new UserDAO();
    private final NotificationService notificationService = new NotificationService();
    private final EmailService emailService = new EmailService();

    public List<Delivery> getDeliveriesForStaff(int staffId) throws SQLException {
        return deliveryDAO.findByStaffId(staffId);
    }

    public boolean updateStatusAndProof(int staffId, int deliveryId, String status, String failureReason, String proofImageUrl) throws SQLException {
        Delivery del = deliveryDAO.findById(deliveryId);
        if (del == null) {
            throw new IllegalArgumentException("Không tìm thấy thông tin giao hàng.");
        }
        // B1 Fix: use Objects.equals() to avoid Integer auto-unbox NPE
        if (del.getStaffId() == null || !del.getStaffId().equals(staffId)) {
            throw new IllegalArgumentException("Bạn không có quyền cập nhật trạng thái đơn giao hàng này.");
        }

        // Validate
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Trạng thái là bắt buộc.");
        }
        if ("DELIVERED".equals(status) && (proofImageUrl == null || proofImageUrl.trim().isEmpty())) {
            throw new IllegalArgumentException("Vui lòng cung cấp ảnh bằng chứng giao hàng!");
        }
        if ("FAILED".equals(status) && (failureReason == null || failureReason.trim().isEmpty())) {
            throw new IllegalArgumentException("Vui lòng nhập lý do giao hàng thất bại!");
        }

        deliveryDAO.updateStatusAndProof(deliveryId, status, failureReason, proofImageUrl);
        return true;
    }

    public void updateEstimatedTime(int staffId, int deliveryId, LocalDateTime estimatedTime) throws SQLException {
        Delivery del = deliveryDAO.findById(deliveryId);
        if (del == null) {
            throw new IllegalArgumentException("Không tìm thấy thông tin giao hàng.");
        }
        // B1 Fix: use Objects.equals() to avoid Integer auto-unbox NPE
        if (del.getStaffId() == null || !del.getStaffId().equals(staffId)) {
            throw new IllegalArgumentException("Bạn không có quyền cập nhật thời gian dự kiến cho đơn giao hàng này.");
        }
        if ("DELIVERED".equals(del.getStatus()) || "FAILED".equals(del.getStatus())) {
            throw new IllegalArgumentException("Không thể cập nhật thời gian dự kiến cho đơn hàng đã hoàn tất hoặc thất bại.");
        }
        deliveryDAO.updateEstimatedTime(deliveryId, estimatedTime);
    }

    public void assignShipper(int orderId, int staffId, LocalDateTime estimatedTime) throws SQLException {
        List<Order> orders = orderDAO.findById(orderId);
        Order order = orders.isEmpty() ? null : orders.get(0);
        if (order == null) {
            throw new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y Ä‘Æ¡n hÃ ng Ä‘á»ƒ táº¡o chuyáº¿n giao.");
        }
        int parentOrderId = order.getParentOrderId() != null ? order.getParentOrderId() : order.getOrderId();
        String tripStatus = staffId > 0 ? AppConfig.DELIVERY_TRIP_ASSIGNED : AppConfig.DELIVERY_TRIP_PLANNED;
        try (Connection conn = orderDAO.openConnection()) {
            conn.setAutoCommit(false);
            try {
                int tripId = deliveryTripDAO.save(conn, parentOrderId, staffId > 0 ? staffId : null,
                        tripStatus, null, estimatedTime);
                deliveryDAO.assignShipper(conn, orderId, tripId, 1, staffId, estimatedTime);
                conn.commit();
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    public Delivery getDeliveryByOrderId(int orderId) throws SQLException {
        return deliveryDAO.findByOrderId(orderId);
    }

    /**
     * Delivery staff xác nhận giao hàng thành công.
     * Cập nhật cả bảng deliveries và orders (DISPATCHED -> DELIVERED).
     * B5 Fix: trước đây chỉ update deliveries, orders.status vẫn bị stale.
     */
    public void markAsDelivered(int staffId, int deliveryId, String proofImageUrl) throws SQLException {
        Delivery del = deliveryDAO.findById(deliveryId);
        if (del == null) {
            throw new IllegalArgumentException("Không tìm thấy thông tin giao hàng.");
        }
        if (del.getStaffId() == null || !del.getStaffId().equals(staffId)) {
            throw new IllegalArgumentException("Bạn không có quyền xác nhận đơn giao hàng này.");
        }
        if (proofImageUrl == null || proofImageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng cung cấp ảnh bằng chứng giao hàng!");
        }
        // Update deliveries table
        deliveryDAO.updateStatusAndProof(deliveryId, "DELIVERED", null, proofImageUrl);
        // Sync orders table: DISPATCHED -> DELIVERED
        orderDAO.updateStatus(del.getOrderId(), "DELIVERED");

        // Gửi thông báo giao hàng thành công cho Customer
        try {
            List<Order> orders = orderDAO.findById(del.getOrderId());
            if (!orders.isEmpty()) {
                Order order = orders.get(0);
                User customer = userDAO.findUserById(order.getCustomerId());
                if (customer != null) {
                    String customerMsg = "Đơn hàng #" + order.getOrderId() + " đã giao hàng thành công bởi người vận chuyển. Vui lòng xác nhận nhận hàng.";
                    notificationService.send(customer.getUserId(), AppConfig.NOTIF_ORDER_UPDATE, "Giao hàng thành công", customerMsg, "/orders/detail?orderId=" + order.getOrderId());
                    String orderDetailUrl = AppConfig.APP_BASE_URL + "/orders/detail?orderId=" + order.getOrderId();
                    emailService.sendOrderNotificationEmail(customer.getEmail(), customer.getFullName(), String.valueOf(order.getOrderId()), "Giao hàng thành công", orderDetailUrl);
                }
            }
        } catch (Exception ex) {
            LoggerUtil.warn(log, "Không gửi được thông báo giao hàng thành công cho orderId=" + del.getOrderId(), ex);
        }
    }

    /**
     * Lấy danh sách đơn giao hàng gộp gồm: đơn đã nhận của staff + đơn chưa được gán.
     */
    public List<Delivery> getDashboardDeliveries(int staffId) throws SQLException {
        List<Delivery> assigned = deliveryDAO.findByStaffId(staffId);
        List<Delivery> unassigned = deliveryDAO.findUnassigned();
        
        List<Delivery> merged = new ArrayList<>(unassigned);
        merged.addAll(assigned);
        
        // Sắp xếp theo created_at giảm dần (tin mới nhất lên đầu)
        merged.sort((d1, d2) -> {
            LocalDateTime t1 = d1.getCreatedAt();
            LocalDateTime t2 = d2.getCreatedAt();
            if (t1 == null && t2 == null) return 0;
            if (t1 == null) return 1;
            if (t2 == null) return -1;
            return t2.compareTo(t1);
        });
        return merged;
    }

    /**
     * Shipper tự nhận đơn hàng chưa có người giao.
     */
    public void claimDelivery(int staffId, int deliveryId) throws SQLException {
        Delivery del = deliveryDAO.findById(deliveryId);
        if (del == null) {
            throw new IllegalArgumentException("Không tìm thấy thông tin giao hàng.");
        }
        if (del.getStaffId() != null) {
            throw new IllegalArgumentException("Đơn giao hàng này đã được shipper khác nhận trước đó.");
        }
        boolean claimed = deliveryDAO.claimDelivery(deliveryId, staffId);
        if (!claimed) {
            throw new IllegalArgumentException("Nhận đơn hàng thất bại. Đơn hàng này có thể đã được shipper khác nhận.");
        }
    }
}

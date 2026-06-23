package service.order;

import config.AppConfig;
import dao.order.DeliveryDAO;
import dao.order.OrderDAO;
import dao.order.ReturnRequestDAO;
import dao.system.SystemConfigDAO;
import dao.auth.UserDAO;
import exception.BusinessException;
import model.entity.order.Delivery;
import model.entity.order.Order;
import model.entity.order.OrderItem;
import model.entity.order.ReturnRequest;
import model.entity.auth.User;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ReturnService — Quản lý vòng đời yêu cầu Đổi/Trả/Hoàn tiền.
 */
public class ReturnService {

    private final ReturnRequestDAO returnDAO = new ReturnRequestDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final DeliveryDAO deliveryDAO = new DeliveryDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();
    private final UserDAO userDAO = new UserDAO();

    /**
     * Tạo yêu cầu đổi trả hàng mới.
     * Quy tắc nghiệp vụ:
     *   - Đơn hàng phải ở trạng thái DELIVERED.
     *   - Yêu cầu phải được tạo trong vòng X giờ (mặc định 24h) từ khi nhận hàng.
     *   - Một sản phẩm trong đơn chỉ có tối đa 1 yêu cầu đổi trả đang xử lý hoặc đã xong.
     */
    public int createRequest(ReturnRequest req) throws SQLException {
        // 1. Kiểm tra đơn hàng có tồn tại không
        List<Order> orders = orderDAO.findById(req.getOrderId());
        if (orders.isEmpty()) {
            throw new BusinessException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng #" + req.getOrderId());
        }
        Order order = orders.get(0);

        // REF-01: Kiểm tra quyền sở hữu — khách hàng phải là chủ đơn hàng
        if (order.getCustomerId() != req.getCustomerId()) {
            throw new BusinessException("ACCESS_DENIED", "Đơn hàng không thuộc về khách hàng này.");
        }

        // REF-01: Kiểm tra trạng thái đơn hàng — chỉ DELIVERED mới được khiếu nại
        if (!AppConfig.ORDER_DELIVERED.equals(order.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể đổi trả đơn hàng đã giao thành công.");
        }

        // 3. Kiểm tra sản phẩm trong đơn hàng và số lượng
        if (req.getOrderItemId() == null) {
            throw new BusinessException("MISSING_ITEM", "Vui lòng chọn sản phẩm cần đổi trả.");
        }
        OrderItem targetItem = null;
        List<OrderItem> orderItems = orderDAO.findItemsByOrderId(req.getOrderId());
        for (OrderItem oi : orderItems) {
            if (oi.getOrderItemId() == req.getOrderItemId()) {
                targetItem = oi;
                break;
            }
        }
        if (targetItem == null) {
            throw new BusinessException("ITEM_NOT_IN_ORDER", "Sản phẩm không thuộc đơn hàng này.");
        }
        if (req.getRequestedQuantity() <= 0 || req.getRequestedQuantity() > targetItem.getQuantity()) {
            throw new BusinessException("INVALID_QUANTITY", "Số lượng đổi trả không hợp lệ (tối đa " + targetItem.getQuantity() + ").");
        }

        // 4. Kiểm tra xem đã có yêu cầu đổi trả nào chưa cho sản phẩm này
        List<ReturnRequest> existing = returnDAO.findByOrder(req.getOrderId());
        for (ReturnRequest r : existing) {
            if (!AppConfig.RETURN_CANCELLED.equals(r.getStatus()) && r.getOrderItemId() != null && r.getOrderItemId().equals(req.getOrderItemId())) {
                throw new BusinessException("DUPLICATE_REQUEST", "Sản phẩm này đã có yêu cầu đổi/trả đang được xử lý.");
            }
        }

        // REF-01: Kiểm tra thời hạn đổi trả — đọc từ AppConfig (không hardcode)
        Delivery del = deliveryDAO.findByOrderId(req.getOrderId());
        LocalDateTime deliveredAt;
        if (del != null && del.getDeliveredAt() != null) {
            deliveredAt = del.getDeliveredAt();
        } else {
            // Fallback về thời gian cập nhật đơn hàng nếu không thấy log giao hàng
            deliveredAt = order.getUpdatedAt();
        }

        // Đọc cấu hình từ DB với fallback về hằng số AppConfig (không hardcode giá trị)
        int returnWindowHours = configDAO.getInt(AppConfig.CONFIG_RETURN_MAX_HOURS, AppConfig.RETURN_REQUEST_MAX_HOURS);
        if (LocalDateTime.now().isAfter(deliveredAt.plusHours(returnWindowHours))) {
            throw new BusinessException("RETURN_WINDOW_EXPIRED", "Quá thời hạn " + returnWindowHours + " giờ khiếu nại.");
        }

        // 6. Tính số tiền hoàn thực tế cho sản phẩm này
        if ("RETURN".equalsIgnoreCase(req.getRequestType())) {
            req.setResolutionType("REFUND");
            BigDecimal itemRefund = targetItem.getUnitPrice().multiply(new BigDecimal(req.getRequestedQuantity()));
            req.setRefundAmount(itemRefund.min(order.getFinalAmount())); // Capped by order final amount
        } else {
            req.setRefundAmount(BigDecimal.ZERO);
        }

        req.setStatus("REQUESTED");
        
        int requestId = returnDAO.save(req);
        if (requestId > 0 && "RETURN".equalsIgnoreCase(req.getRequestType())) {
            // Cập nhật trạng thái hoàn tiền của đơn hàng
            orderDAO.updateRefundStatus(req.getOrderId(), "PENDING");
        }
        return requestId;
    }

    public ReturnRequest getRequest(int requestId) throws SQLException {
        return returnDAO.findById(requestId);
    }

    public List<ReturnRequest> getMyRequests(int customerId) throws SQLException {
        return returnDAO.findByCustomer(customerId);
    }

    public List<ReturnRequest> getPendingRequests() throws SQLException {
        return returnDAO.findPending();
    }

    public List<ReturnRequest> getAllRequests() throws SQLException {
        return returnDAO.findAll();
    }

    public List<ReturnRequest> getRequestsByOwner(int ownerId) throws SQLException {
        return returnDAO.findByOwner(ownerId);
    }

    /**
     * Xử lý duyệt hoặc từ chối yêu cầu đổi trả.
     */
    public void decide(int requestId, String status, String reason, int decidedBy) throws SQLException {
        // REF-04: Chỉ ADMIN mới có quyền phê duyệt — xác minh server-side qua DB
        User adminUser = userDAO.findUserById(decidedBy);
        if (adminUser == null || !AppConfig.ROLE_ADMIN.equals(adminUser.getRole())) {
            throw new BusinessException("ACCESS_DENIED", "Chỉ Admin mới có quyền phê duyệt yêu cầu đổi trả.");
        }

        ReturnRequest req = returnDAO.findById(requestId);
        if (req == null) {
            throw new BusinessException("NOT_FOUND", "Không tìm thấy yêu cầu đổi trả #" + requestId);
        }

        if (!AppConfig.RETURN_REQUESTED.equals(req.getStatus())) {
            throw new BusinessException("INVALID_STATUS", "Yêu cầu này đã được xử lý từ trước.");
        }

        if (!AppConfig.RETURN_APPROVED.equals(status) && !AppConfig.RETURN_REJECTED.equals(status)) {
            throw new BusinessException("INVALID_STATUS", "Trạng thái phê duyệt không hợp lệ.");
        }

        // Cập nhật trạng thái yêu cầu
        returnDAO.updateStatus(requestId, status, reason, decidedBy);

        // Cập nhật đơn hàng tương ứng
        if ("RETURN".equalsIgnoreCase(req.getRequestType())) {
            if (AppConfig.RETURN_APPROVED.equals(status)) {
                // Đổi trạng thái hoàn tiền thành REFUNDED (hoặc APPROVED tùy luồng tài chính)
                orderDAO.updateRefundStatus(req.getOrderId(), "REFUNDED");
                // Hoàn trả lại tồn kho của đúng sản phẩm và số lượng yêu cầu
                if (req.getOrderItemId() != null) {
                    orderDAO.restoreItemInventoryStock(req.getOrderItemId(), req.getRequestedQuantity());
                } else {
                    orderDAO.restoreInventoryStock(req.getOrderId());
                }
            } else {
                orderDAO.updateRefundStatus(req.getOrderId(), AppConfig.RETURN_REJECTED);
            }
        }
    }
}

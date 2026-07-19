package service.order;

import dao.order.ReturnRequestDAO;
import dao.order.OrderDAO;
import model.entity.order.ReturnRequest;
import java.sql.SQLException;
import java.util.List;

public class ReturnRequestService {

    private final ReturnRequestDAO returnRequestDAO = new ReturnRequestDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    public List<ReturnRequest> getAllRequests(String status, int page, int pageSize) throws SQLException {
        return returnRequestDAO.findAll(status, page, pageSize);
    }

    public int countAllRequests(String status) throws SQLException {
        return returnRequestDAO.countAll(status);
    }

    /**
     * Compatibility overload for existing tests/callers. The supplied orderId
     * is intentionally ignored; the relationship is derived from requestId.
     */
    public void processRequest(int requestId, String action, String reason, int adminId, int ignoredOrderId) throws SQLException {
        processRequest(requestId, action, reason, adminId);
    }

    public void processRequest(int requestId, String action, String reason, int adminId) throws SQLException {
        ReturnRequest request = returnRequestDAO.findById(requestId);
        if (request == null) {
            throw new IllegalArgumentException("Không tìm thấy yêu cầu đổi trả.");
        }
        if (action == null || reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Thiếu hành động hoặc lý do quyết định.");
        }
        if (!"process".equals(action) && !"approve".equals(action)
                && !"complete".equals(action) && !"reject".equals(action)) {
            throw new IllegalArgumentException("Hành động đổi trả không được hỗ trợ.");
        }
        int orderId = request.getOrderId();
        if ("process".equals(action)) {
            returnRequestDAO.updateStatus(requestId, "PROCESSING", reason, adminId);
        } else if ("approve".equals(action)) {
            returnRequestDAO.updateStatus(requestId, "APPROVED", reason, adminId);
        } else if ("complete".equals(action)) {
            returnRequestDAO.updateStatus(requestId, "COMPLETED", reason, adminId);
            // REFUNDED là trạng thái của orders.refund_status, không phải orders.status.
            // orders.status có CHECK constraint và không nhận giá trị REFUNDED.
            orderDAO.updateRefundStatus(orderId, "REFUNDED");
            // Could also add refund transaction logic here if real payment gateway was integrated
        } else if ("reject".equals(action)) {
            returnRequestDAO.updateStatus(requestId, "REJECTED", reason, adminId);
            // Optionally revert order status if needed
        }
    }
}

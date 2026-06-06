package com.fruitmkt.service;

import com.fruitmkt.dao.ReturnRequestDAO;
import com.fruitmkt.dao.OrderDAO;
import com.fruitmkt.model.entity.ReturnRequest;
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

    public void processRequest(int requestId, String action, String reason, int adminId, int orderId) throws SQLException {
        if ("process".equals(action)) {
            returnRequestDAO.updateStatus(requestId, "PROCESSING", reason, adminId);
        } else if ("approve".equals(action)) {
            returnRequestDAO.updateStatus(requestId, "APPROVED", reason, adminId);
        } else if ("complete".equals(action)) {
            returnRequestDAO.updateStatus(requestId, "COMPLETED", reason, adminId);
            orderDAO.updateStatus(orderId, "REFUNDED");
            // Could also add refund transaction logic here if real payment gateway was integrated
        } else if ("reject".equals(action)) {
            returnRequestDAO.updateStatus(requestId, "REJECTED", reason, adminId);
            // Optionally revert order status if needed
        }
    }
}

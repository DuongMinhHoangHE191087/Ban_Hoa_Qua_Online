package com.fruitmkt.service;

import com.fruitmkt.dao.DeliveryDAO;
import com.fruitmkt.model.entity.Delivery;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

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

    private final DeliveryDAO deliveryDAO = new DeliveryDAO();

    public List<Delivery> getDeliveriesForStaff(int staffId) throws SQLException {
        return deliveryDAO.findByStaffId(staffId);
    }

    public boolean updateStatusAndProof(int staffId, int deliveryId, String status, String failureReason, String proofImageUrl) throws SQLException {
        // Validate
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required");
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

    public void updateEstimatedTime(int deliveryId, LocalDateTime estimatedTime) throws SQLException {
        deliveryDAO.updateEstimatedTime(deliveryId, estimatedTime);
    }

    public void assignShipper(int orderId, int staffId, LocalDateTime estimatedTime) throws SQLException {
        deliveryDAO.assignShipper(orderId, staffId, estimatedTime);
    }
}

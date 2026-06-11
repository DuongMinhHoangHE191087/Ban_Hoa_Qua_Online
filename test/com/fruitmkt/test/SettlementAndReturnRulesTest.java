package com.fruitmkt.test;

import com.fruitmkt.dao.ReturnRequestDAO;
import com.fruitmkt.dao.OrderDAO;
import com.fruitmkt.service.ReturnRequestService;
import com.fruitmkt.model.entity.ReturnRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.sql.SQLException;
import java.util.List;

public class SettlementAndReturnRulesTest {
    private ReturnRequestDAO returnRequestDAO;
    private OrderDAO orderDAO;
    private ReturnRequestService returnRequestService;

    @Before
    public void setUp() {
        returnRequestDAO = new ReturnRequestDAO();
        orderDAO = new OrderDAO();
        returnRequestService = new ReturnRequestService();
    }

    @Test
    public void testProcessReturnRequestFlow() throws SQLException {
        // Lấy danh sách yêu cầu đổi trả hiện có
        List<ReturnRequest> list = returnRequestService.getAllRequests(null, 1, 10);
        Assert.assertNotNull(list);

        if (!list.isEmpty()) {
            ReturnRequest req = list.get(0);
            int requestId = req.getReturnRequestId();
            int orderId = req.getOrderId();

            // Test chuyển trạng thái PROCESSING
            returnRequestService.processRequest(requestId, "process", "Đang xử lý yêu cầu", 1, orderId);
            List<ReturnRequest> processingList = returnRequestService.getAllRequests("PROCESSING", 1, 10);
            Assert.assertFalse(processingList.isEmpty());

            // Test chuyển trạng thái APPROVED
            returnRequestService.processRequest(requestId, "approve", "Đồng ý xử lý", 1, orderId);
            List<ReturnRequest> approvedList = returnRequestService.getAllRequests("APPROVED", 1, 10);
            Assert.assertFalse(approvedList.isEmpty());
        }
    }
}

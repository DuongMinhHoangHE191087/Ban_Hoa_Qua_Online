package com.fruitmkt.servlet.delivery;

import com.fruitmkt.dao.OrderDAO;
import com.fruitmkt.model.dto.DeliveryWithOrderDTO;
import com.fruitmkt.model.entity.Delivery;
import com.fruitmkt.model.entity.Order;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.DeliveryService;
import com.fruitmkt.util.LoggerUtil;
import com.fruitmkt.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * DeliveryDashboardServlet — Dashboard giao hàng cho Delivery Staff.
 *
 * URL: /delivery/dashboard
 * GET: Load danh sách đơn được phân công (hoặc unassigned) + Order info (recipient, address)
 *
 * QUY TẮC SERVLET:
 *   1. Không viết SQL — gọi Service/DAO
 *   2. Forward đến WEB-INF/jsp/delivery/dashboard.jsp
 *   3. Kiểm tra ROLE=DELIVERY trước
 *
 * @author fruitmkt-team
 */
@WebServlet("/delivery/dashboard")
public class DeliveryDashboardServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DeliveryDashboardServlet.class.getName());

    private final DeliveryService deliveryService = new DeliveryService();
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = SessionUtil.getCurrentUser(req.getSession(false));
        if (currentUser == null || !"DELIVERY".equals(currentUser.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Tab/status filter
        String filterStatus = req.getParameter("status");

        try {
            List<Delivery> deliveries = deliveryService.getDashboardDeliveries(currentUser.getUserId());
            List<DeliveryWithOrderDTO> enrichedList = new ArrayList<>();

            for (Delivery del : deliveries) {
                // Skip filtered statuses if tab is active
                if (filterStatus != null && !filterStatus.isEmpty() && !filterStatus.equals(del.getStatus())) {
                    continue;
                }
                // Enrich with Order info (recipient name, address)
                Order order = null;
                try {
                    List<Order> orders = orderDAO.findById(del.getOrderId());
                    if (!orders.isEmpty()) {
                        order = orders.get(0);
                    }
                } catch (Exception e) {
                    LoggerUtil.warn(log, "Không thể tải thông tin đơn hàng cho delivery " + del.getDeliveryId(), e);
                }
                enrichedList.add(new DeliveryWithOrderDTO(del, order));
            }

            req.setAttribute("deliveryList", enrichedList);
            req.setAttribute("filterStatus", filterStatus);
            req.getRequestDispatcher("/WEB-INF/jsp/delivery/dashboard.jsp").forward(req, resp);
        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi tải danh sách đơn hàng cần giao", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi tải danh sách đơn hàng cần giao.");
        }
    }
}

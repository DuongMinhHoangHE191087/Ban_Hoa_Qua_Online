package servlet.delivery;

import dao.order.OrderDAO;
import model.dto.order.DeliveryWithOrderDTO;
import model.entity.order.Delivery;
import model.entity.order.Order;
import model.entity.auth.User;
import service.order.DeliveryService;
import util.LoggerUtil;
import util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
@WebServlet({"/delivery/dashboard", "/delivery/list"})
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

            // Lọc theo status trước để giảm batch size
            if (filterStatus != null && !filterStatus.isEmpty()) {
                deliveries = deliveries.stream()
                        .filter(d -> filterStatus.equals(d.getStatus()))
                        .collect(Collectors.toList());
            }

            // Batch load toàn bộ orders trong 1 query thay vì N queries (N+1 fix)
            Collection<Integer> orderIds = deliveries.stream()
                    .map(Delivery::getOrderId)
                    .collect(Collectors.toList());
            Map<Integer, Order> ordersById = orderDAO.findByIds(orderIds);

            List<DeliveryWithOrderDTO> enrichedList = new ArrayList<>();
            for (Delivery del : deliveries) {
                enrichedList.add(new DeliveryWithOrderDTO(del, ordersById.get(del.getOrderId())));
            }

            req.setAttribute("deliveryList", enrichedList);
            req.setAttribute("filterStatus", filterStatus);

            String path = req.getServletPath();
            String jspTarget = "/delivery/list".equals(path)
                    ? "/WEB-INF/jsp/delivery/delivery-list.jsp"
                    : "/WEB-INF/jsp/delivery/dashboard.jsp";
            req.getRequestDispatcher(jspTarget).forward(req, resp);
        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi tải danh sách đơn hàng cần giao", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi tải danh sách đơn hàng cần giao.");
        }
    }
}

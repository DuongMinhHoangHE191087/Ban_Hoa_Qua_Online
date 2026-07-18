package servlet.shop.shop;

import config.AppConfig;
import model.entity.auth.User;
import service.shop.ReportService;
import util.SessionUtil;
import util.JsonUtil;

import util.LoggerUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import dao.catalog.CategoryDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * ShopReportServlet — Controller hiển thị báo cáo thống kê dành cho Shop Owner.
 *
 * URL: /shop/reports
 * GET: Hiển thị biểu đồ và báo cáo cho Shop Owner
 *
 * @author fruitmkt-team
 */
@WebServlet("/shop/reports")
public class ShopReportServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ShopReportServlet.class.getName());

    private final ReportService reportService = new ReportService();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final service.order.OrderService orderService = new service.order.OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        HttpSession session = req.getSession(false);
        User user = SessionUtil.getCurrentUser(session);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        if (!SessionUtil.hasRole(session, AppConfig.ROLE_SHOP_OWNER)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String startDate = req.getParameter("startDate");
        String endDate = req.getParameter("endDate");
        
        String catParam = req.getParameter("categoryId");
        Integer categoryId = null;
        if (catParam != null && !catParam.trim().isEmpty()) {
            try {
                categoryId = Integer.parseInt(catParam);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        try {
            Map<String, Object> reportData = reportService.getShopReportData(user.getUserId(), startDate, endDate, categoryId);
            
            // Đưa dữ liệu thô vào request attributes để JSTL xử lý
            req.setAttribute("startDate", reportData.get("startDate"));
            req.setAttribute("endDate", reportData.get("endDate"));
            req.setAttribute("fruitUsage", reportData.get("fruitUsage"));
            req.setAttribute("selectedCategoryId", categoryId);
            
            // Metadata for dropdowns
            req.setAttribute("categories", categoryDAO.findAllActive());
            
            // Serialize dữ liệu biểu đồ sang JSON để Chart.js ở Client-side vẽ biểu đồ
            req.setAttribute("revenueTrendJson", JsonUtil.toJson(reportData.get("revenueTrend")));
            req.setAttribute("orderStatusStatsJson", JsonUtil.toJson(reportData.get("orderStatusStats")));
            req.setAttribute("cancellationReasonStatsJson", JsonUtil.toJson(reportData.get("cancellationReasonStats")));
            
            // Tính toán nhanh số liệu tổng quan (KPI) để đưa lên thẻ đầu trang của Shop
            calculateKPIs(req, reportData);

            java.math.BigDecimal estimatedRevenue = java.math.BigDecimal.ZERO;
            try {
                estimatedRevenue = orderService.getEstimatedRevenueByOwner(user.getUserId());
            } catch (Exception e) {
                // Ignore
            }
            req.setAttribute("kpiEstimatedRevenue", estimatedRevenue);

            req.getRequestDispatcher("/WEB-INF/jsp/shop/report.jsp").forward(req, resp);
        } catch (Exception e) {
            util.ServletUtil.sendPageInternalServerError(
                    req,
                    resp,
                    java.util.logging.Logger.getLogger(ShopReportServlet.class.getName()),
                    "ShopReportServlet#doGet",
                        "Lỗi hệ thống khi tải báo cáo cửa hàng.",
                        e);
        }
    }

    private void calculateKPIs(HttpServletRequest req, Map<String, Object> reportData) {
        java.util.List<Map<String, Object>> revenueTrend = (java.util.List<Map<String, Object>>) reportData.get("revenueTrend");
        java.util.List<Map<String, Object>> orderStatusStats = (java.util.List<Map<String, Object>>) reportData.get("orderStatusStats");
        java.util.List<Map<String, Object>> fruitUsage = (java.util.List<Map<String, Object>>) reportData.get("fruitUsage");

        // 1. Tính tổng doanh thu của shop
        java.math.BigDecimal totalRevenue = java.math.BigDecimal.ZERO;
        if (revenueTrend != null) {
            for (Map<String, Object> day : revenueTrend) {
                java.math.BigDecimal rev = (java.math.BigDecimal) day.get("revenue");
                if (rev != null) {
                    totalRevenue = totalRevenue.add(rev);
                }
            }
        }
        req.setAttribute("kpiTotalRevenue", totalRevenue);

        // 2. Tính tổng đơn hàng và đơn thành công/hủy của shop
        int totalOrders = 0;
        int successfulOrders = 0;
        int cancelledOrders = 0;
        if (orderStatusStats != null) {
            for (Map<String, Object> stat : orderStatusStats) {
                String status = (String) stat.get("status");
                int count = (Integer) stat.get("count");
                totalOrders += count;
                if ("DELIVERED".equals(status)) {
                    successfulOrders += count;
                } else if ("CANCELLED".equals(status)) {
                    cancelledOrders += count;
                }
            }
        }
        req.setAttribute("kpiTotalOrders", totalOrders);
        req.setAttribute("kpiSuccessfulOrders", successfulOrders);
        req.setAttribute("kpiCancelledOrders", cancelledOrders);
        
        // Tỷ lệ hủy đơn
        double cancellationRate = totalOrders > 0 ? ((double) cancelledOrders / totalOrders) * 100 : 0.0;
        req.setAttribute("kpiCancellationRate", Math.round(cancellationRate * 10.0) / 10.0);

        // 3. Tính số sản phẩm (trái cây) đã bán
        int totalUnitsSold = 0;
        if (fruitUsage != null) {
            for (Map<String, Object> item : fruitUsage) {
                totalUnitsSold += (Integer) item.get("totalQuantity");
            }
        }
        req.setAttribute("kpiTotalUnitsSold", totalUnitsSold);
    }
}

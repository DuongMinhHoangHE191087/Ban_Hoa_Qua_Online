package com.fruitmkt.servlet.admin;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.ReportService;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * AdminReportServlet — Controller hiển thị báo cáo thống kê dành cho Admin.
 *
 * URL: /admin/reports
 * GET: Hiển thị biểu đồ và báo cáo
 *
 * @author fruitmkt-team
 */
@WebServlet("/admin/reports")
public class AdminReportServlet extends HttpServlet {

    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        HttpSession session = req.getSession(false);
        User user = SessionUtil.getCurrentUser(session);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        if (!AppConfig.ROLE_ADMIN.equals(user.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String startDate = req.getParameter("startDate");
        String endDate = req.getParameter("endDate");

        try {
            Map<String, Object> reportData = reportService.getAdminReportData(startDate, endDate);
            
            // Đưa dữ liệu thô vào request attributes để JSTL xử lý
            req.setAttribute("startDate", reportData.get("startDate"));
            req.setAttribute("endDate", reportData.get("endDate"));
            req.setAttribute("fruitUsage", reportData.get("fruitUsage"));
            
            // Serialize dữ liệu biểu đồ sang JSON để Chart.js ở Client-side vẽ biểu đồ
            req.setAttribute("revenueTrendJson", JsonUtil.toJson(reportData.get("revenueTrend")));
            req.setAttribute("orderStatusStatsJson", JsonUtil.toJson(reportData.get("orderStatusStats")));
            req.setAttribute("cancellationReasonStatsJson", JsonUtil.toJson(reportData.get("cancellationReasonStats")));
            req.setAttribute("userGrowthJson", JsonUtil.toJson(reportData.get("userGrowth")));
            
            // Tính toán nhanh số liệu tổng quan (KPI) để đưa lên thẻ đầu trang
            calculateKPIs(req, reportData);

            req.getRequestDispatcher("/WEB-INF/jsp/admin/report.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi tải báo cáo: " + e.getMessage());
        }
    }

    private void calculateKPIs(HttpServletRequest req, Map<String, Object> reportData) {
        java.util.List<Map<String, Object>> revenueTrend = (java.util.List<Map<String, Object>>) reportData.get("revenueTrend");
        java.util.List<Map<String, Object>> orderStatusStats = (java.util.List<Map<String, Object>>) reportData.get("orderStatusStats");
        java.util.List<Map<String, Object>> userGrowth = (java.util.List<Map<String, Object>>) reportData.get("userGrowth");

        // 1. Tính tổng doanh thu
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

        // 2. Tính tổng đơn hàng và đơn thành công
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
        
        // Tỷ lệ hủy đơn
        double cancellationRate = totalOrders > 0 ? ((double) cancelledOrders / totalOrders) * 100 : 0.0;
        req.setAttribute("kpiCancellationRate", Math.round(cancellationRate * 10.0) / 10.0);

        // 3. Tính số người dùng mới đăng ký trong giai đoạn
        int newUsers = 0;
        if (userGrowth != null) {
            for (Map<String, Object> day : userGrowth) {
                newUsers += (Integer) day.get("count");
            }
        }
        req.setAttribute("kpiNewUsers", newUsers);
    }
}

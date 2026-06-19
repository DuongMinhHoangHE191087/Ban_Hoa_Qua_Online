package service.shop;

import dao.order.OrderDAO;
import dao.auth.UserDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * ReportService — Xử lý logic và chuẩn bị dữ liệu báo cáo thống kê.
 *
 * @author fruitmkt-team
 */
public class ReportService {

    private final OrderDAO orderDAO = new OrderDAO();
    private final UserDAO userDAO = new UserDAO();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Lấy dữ liệu báo cáo cho Admin.
     */
    public Map<String, Object> getAdminReportData(String startDate, String endDate, Integer categoryId, Integer shopId) throws SQLException {
        Map<String, Object> report = new HashMap<>();
        
        // Thiết lập ngày mặc định (30 ngày gần đây) nếu trống
        String[] dates = resolveDateRange(startDate, endDate);
        String start = dates[0];
        String end = dates[1];
        
        report.put("startDate", start);
        report.put("endDate", end);
        report.put("selectedCategoryId", categoryId);
        report.put("selectedShopId", shopId);
        report.put("revenueTrend", orderDAO.getRevenueTrend(shopId, start, end, categoryId));
        report.put("orderStatusStats", orderDAO.getOrderStatusStats(shopId, start, end, categoryId));
        report.put("cancellationReasonStats", orderDAO.getCancellationReasonStats(shopId, start, end, categoryId));
        report.put("fruitUsage", orderDAO.getFruitUsageReport(shopId, start, end, categoryId));
        report.put("userGrowth", userDAO.getUserRegistrationTrend(start, end));
        
        return report;
    }

    /**
     * Lấy dữ liệu báo cáo cho Shop Owner.
     */
    public Map<String, Object> getShopReportData(int ownerId, String startDate, String endDate, Integer categoryId) throws SQLException {
        Map<String, Object> report = new HashMap<>();
        
        // Thiết lập ngày mặc định (30 ngày gần đây) nếu trống
        String[] dates = resolveDateRange(startDate, endDate);
        String start = dates[0];
        String end = dates[1];
        
        report.put("startDate", start);
        report.put("endDate", end);
        report.put("selectedCategoryId", categoryId);
        report.put("revenueTrend", orderDAO.getRevenueTrend(ownerId, start, end, categoryId));
        report.put("orderStatusStats", orderDAO.getOrderStatusStats(ownerId, start, end, categoryId));
        report.put("cancellationReasonStats", orderDAO.getCancellationReasonStats(ownerId, start, end, categoryId));
        report.put("fruitUsage", orderDAO.getFruitUsageReport(ownerId, start, end, categoryId));
        
        return report;
    }

    /**
     * Giải quyết khoảng ngày mặc định nếu không được chỉ định.
     * Trả về [startDate, endDate] dưới định dạng yyyy-MM-dd.
     */
    private String[] resolveDateRange(String startDate, String endDate) {
        LocalDate end;
        try {
            if (endDate != null && !endDate.trim().isEmpty()) {
                end = LocalDate.parse(endDate, DATE_FORMATTER);
            } else {
                end = LocalDate.now();
            }
        } catch (Exception e) {
            end = LocalDate.now();
        }

        LocalDate start;
        try {
            if (startDate != null && !startDate.trim().isEmpty()) {
                start = LocalDate.parse(startDate, DATE_FORMATTER);
            } else {
                start = end.minusDays(30);
            }
        } catch (Exception e) {
            start = end.minusDays(30);
        }

        return new String[] { start.format(DATE_FORMATTER), end.format(DATE_FORMATTER) };
    }
}

package com.fruitmkt.servlet.shop;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.model.entity.InventoryLog;
import com.fruitmkt.model.entity.ProductVariant;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.ProductService;
import com.fruitmkt.dao.ProductVariantDAO;
import com.fruitmkt.dao.InventoryDAO;
import com.fruitmkt.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * InventoryServlet — Controller cho chức năng: Bảng tồn kho và lịch sử điều chỉnh
 *
 * URL: /shop/inventory
 * GET : Bảng tồn kho và lịch sử điều chỉnh
 * POST: Điều chỉnh tồn kho thủ công (Restock - II.13)
 *
 * @author fruitmkt-team
 */
@WebServlet("/shop/inventory")
public class InventoryServlet extends HttpServlet {

    private final ProductService productService = new ProductService();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        HttpSession session = req.getSession();
        if (!SessionUtil.isLoggedIn(session) || !SessionUtil.hasRole(session, AppConfig.ROLE_SHOP_OWNER)) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        User user = SessionUtil.getCurrentUser(session);
        List<ProductVariant> variants = Collections.emptyList();
        List<InventoryLog> logs = Collections.emptyList();

        try {
            variants = productVariantDAO.findByOwner(user.getUserId());
            logs = inventoryDAO.findLogsByOwner(user.getUserId());
        } catch (SQLException e) {
            req.getServletContext().log("InventoryServlet GET error: " + e.getMessage(), e);
            SessionUtil.flashError(session, "Không thể tải dữ liệu kho hàng.");
        }

        req.setAttribute("variants", variants);
        req.setAttribute("logs", logs);
        
        req.getRequestDispatcher("/WEB-INF/jsp/shop/inventory.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        HttpSession session = req.getSession();
        if (!SessionUtil.isLoggedIn(session) || !SessionUtil.hasRole(session, AppConfig.ROLE_SHOP_OWNER)) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        User user = SessionUtil.getCurrentUser(session);
        
        try {
            int variantId = Integer.parseInt(req.getParameter("variantId"));
            int quantity = Integer.parseInt(req.getParameter("quantity"));

            // II.13 Test case: submitting a negative or zero restock quantity must be rejected
            if (quantity <= 0) {
                throw new IllegalArgumentException("Số lượng nhập kho phải lớn hơn 0.");
            }

            productService.restock(variantId, quantity, user.getUserId());
            SessionUtil.flashSuccess(session, "Cập nhật số lượng tồn kho thành công!");
            
        } catch (NumberFormatException e) {
            SessionUtil.flashError(session, "Số lượng không hợp lệ.");
        } catch (IllegalArgumentException e) {
            SessionUtil.flashError(session, e.getMessage());
        } catch (SQLException e) {
            req.getServletContext().log("InventoryServlet POST error: " + e.getMessage(), e);
            SessionUtil.flashError(session, "Cập nhật tồn kho thất bại: Lỗi cơ sở dữ liệu.");
        }

        resp.sendRedirect(req.getContextPath() + "/shop/inventory");
    }
}

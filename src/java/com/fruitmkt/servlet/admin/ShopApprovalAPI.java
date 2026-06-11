package com.fruitmkt.servlet.admin;

import com.fruitmkt.model.response.ApiResponse;
import com.fruitmkt.service.ShopService;
import com.fruitmkt.util.JsonUtil;
import com.fruitmkt.util.LoggerUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Logger;

@WebServlet("/admin/shops/approve")
public class ShopApprovalAPI extends HttpServlet {

    private static final Logger log = Logger.getLogger(ShopApprovalAPI.class.getName());

    private final ShopService shopService = new ShopService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            int profileId = Integer.parseInt(request.getParameter("profileId"));
            String status = request.getParameter("status");
            String rejectionReason = request.getParameter("rejectionReason");

            shopService.updateShopStatus(profileId, status, rejectionReason);

            String message = "APPROVED".equals(status) ? "Đã duyệt cửa hàng" : "Đã từ chối cửa hàng";
            response.setStatus(HttpServletResponse.SC_OK);
            JsonUtil.writeJson(response, ApiResponse.ok(message));

        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi khi phê duyệt shop profileId=" + request.getParameter("profileId"), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server: " + e.getMessage()));
        }
    }
}

package com.fruitmkt.servlet.api;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.UserAddressDAO;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.model.entity.UserAddress;
import com.fruitmkt.util.JsonUtil;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/address")
public class AddressAPIServlet extends HttpServlet {

    private final UserAddressDAO addressDAO = new UserAddressDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        HttpSession session = req.getSession(false);
        User user = SessionUtil.getCurrentUser(session);

        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJson(resp, Map.of("success", false, "error", "Nguoi dung chua dang nhap."));
            return;
        }

        // CSRF Verification
        String sessionCsrf = (session != null) ? (String) session.getAttribute(AppConfig.SESSION_CSRF_TOKEN) : null;
        String reqCsrf = req.getParameter("_csrf");
        if (reqCsrf == null) {
            reqCsrf = req.getHeader("X-CSRF-Token");
        }
        if (sessionCsrf == null || !sessionCsrf.equals(reqCsrf)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonUtil.writeJson(resp, Map.of("success", false, "error", "Yeu cau khong hop le (CSRF)."));
            return;
        }

        String action = req.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            action = "list";
        }

        try {
            if ("list".equals(action)) {
                List<UserAddress> addresses = addressDAO.findByUser(user.getUserId());
                JsonUtil.writeJson(resp, Map.of("success", true, "addresses", addresses));
                return;
            }

            if ("add".equals(action)) {
                String name = req.getParameter("recipientName");
                String phone = req.getParameter("recipientPhone");
                String detail = req.getParameter("addressDetail");
                boolean isDefault = "true".equals(req.getParameter("isDefault"));

                name = name != null ? name.replaceAll("<[^>]*>", "").trim() : "";
                phone = phone != null ? phone.replaceAll("<[^>]*>", "").trim() : "";
                detail = detail != null ? detail.replaceAll("<[^>]*>", "").trim() : "";

                if (name.length() < 3) {
                    JsonUtil.writeJson(resp, Map.of("success", false, "error", "Ho va ten nguoi nhan phai tu 3 ky tu tro len."));
                    return;
                }
                if (phone == null || !phone.matches("^(0|\\+84)[3|5|7|8|9][0-9]{8}$")) {
                    JsonUtil.writeJson(resp, Map.of("success", false, "error", "So dien thoai khong hop le (VN 10 chu so)."));
                    return;
                }
                if (detail.length() < 5) {
                    JsonUtil.writeJson(resp, Map.of("success", false, "error", "Dia chi chi tiet phai tu 5 ky tu tro len."));
                    return;
                }

                // If user has no addresses, force default
                List<UserAddress> existing = addressDAO.findByUser(user.getUserId());
                if (existing.isEmpty()) {
                    isDefault = true;
                } else if (isDefault) {
                    addressDAO.clearDefault(user.getUserId());
                }

                UserAddress addr = new UserAddress();
                addr.setUserId(user.getUserId());
                addr.setRecipientName(name);
                addr.setRecipientPhone(phone);
                addr.setAddressDetail(detail);
                addr.setDefault(isDefault);

                boolean ok = addressDAO.save(addr);
                if (ok) {
                    JsonUtil.writeJson(resp, Map.of("success", true, "address", addr));
                } else {
                    JsonUtil.writeJson(resp, Map.of("success", false, "error", "Khong the luu dia chi vao co so du lieu."));
                }
                return;
            }

            if ("update".equals(action)) {
                String addressIdStr = req.getParameter("addressId");
                if (addressIdStr == null || addressIdStr.trim().isEmpty()) {
                    JsonUtil.writeJson(resp, Map.of("success", false, "error", "Ma dia chi khong hop le."));
                    return;
                }
                int addressId = Integer.parseInt(addressIdStr.trim());
                UserAddress addr = addressDAO.findById(addressId);
                if (addr == null || addr.getUserId() != user.getUserId()) {
                    JsonUtil.writeJson(resp, Map.of("success", false, "error", "Khong tim thay hoac khong co quyen chinh sua dia chi nay."));
                    return;
                }

                String name = req.getParameter("recipientName");
                String phone = req.getParameter("recipientPhone");
                String detail = req.getParameter("addressDetail");
                boolean isDefault = "true".equals(req.getParameter("isDefault"));

                name = name != null ? name.replaceAll("<[^>]*>", "").trim() : "";
                phone = phone != null ? phone.replaceAll("<[^>]*>", "").trim() : "";
                detail = detail != null ? detail.replaceAll("<[^>]*>", "").trim() : "";

                if (name.length() < 3) {
                    JsonUtil.writeJson(resp, Map.of("success", false, "error", "Ho va ten nguoi nhan phai tu 3 ky tu tro len."));
                    return;
                }
                if (phone == null || !phone.matches("^(0|\\+84)[3|5|7|8|9][0-9]{8}$")) {
                    JsonUtil.writeJson(resp, Map.of("success", false, "error", "So dien thoai khong hop le (VN 10 chu so)."));
                    return;
                }
                if (detail.length() < 5) {
                    JsonUtil.writeJson(resp, Map.of("success", false, "error", "Dia chi chi tiet phai tu 5 ky tu tro len."));
                    return;
                }

                if (isDefault) {
                    addressDAO.clearDefault(user.getUserId());
                }

                addr.setRecipientName(name);
                addr.setRecipientPhone(phone);
                addr.setAddressDetail(detail);
                addr.setDefault(isDefault);

                boolean ok = addressDAO.update(addr);
                if (ok) {
                    JsonUtil.writeJson(resp, Map.of("success", true, "address", addr));
                } else {
                    JsonUtil.writeJson(resp, Map.of("success", false, "error", "Khong the cap nhat dia chi."));
                }
                return;
            }

            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(resp, Map.of("success", false, "error", "Hanh dong khong hop le."));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJson(resp, Map.of("success", false, "error", "Loi may chu: " + e.getMessage()));
        }
    }
}

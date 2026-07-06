package servlet.api.user;

import config.AppConfig;
import dao.auth.UserAddressDAO;
import model.entity.auth.User;
import model.entity.auth.UserAddress;
import model.response.ApiResponse;
import util.JsonUtil;
import util.SessionUtil;
import util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/address")
public class AddressAPIServlet extends HttpServlet {

    private final UserAddressDAO addressDAO = new UserAddressDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        HttpSession session = req.getSession(false);
        User user = SessionUtil.getCurrentUser(session);

        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJson(resp, ApiResponse.error("Nguoi dung chua dang nhap."));
            return;
        }

        String action = req.getParameter("action");
        if (action != null && !action.trim().isEmpty() && !"list".equalsIgnoreCase(action)) {
            resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            JsonUtil.writeJson(resp, ApiResponse.error("Hanh dong khong hop le."));
            return;
        }

        try {
            List<UserAddress> addresses = addressDAO.findByUser(user.getUserId());
            JsonUtil.writeJson(resp, ApiResponse.ok(Map.of("addresses", addresses)));
        } catch (SQLException e) {
            util.ServletUtil.sendJsonInternalServerError(
                    req,
                    resp,
                    java.util.logging.Logger.getLogger(AddressAPIServlet.class.getName()),
                    "AddressAPIServlet#doGet",
                    "Lỗi hệ thống khi tải địa chỉ.",
                    e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        HttpSession session = req.getSession(false);
        User user = SessionUtil.getCurrentUser(session);

        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJson(resp, ApiResponse.error("Nguoi dung chua dang nhap."));
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
            JsonUtil.writeJson(resp, ApiResponse.error("Yêu cầu không hợp lệ (CSRF)."));
            return;
        }

        String action = req.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            action = "list";
        }

        try {
            if ("list".equals(action)) {
                List<UserAddress> addresses = addressDAO.findByUser(user.getUserId());
                JsonUtil.writeJson(resp, ApiResponse.ok(Map.of("addresses", addresses)));
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
                    JsonUtil.writeJson(resp, ApiResponse.error("Họ và tên người nhận phải từ 3 ký tự trở lên."));
                    return;
                }
                phone = ValidationUtil.normalizePhone(phone);
                if (!ValidationUtil.isValidPhone(phone)) {
                    JsonUtil.writeJson(resp, ApiResponse.error("Số điện thoại không hợp lệ (VN 10 chữ số)."));
                    return;
                }
                if (detail.length() < 5) {
                    JsonUtil.writeJson(resp, ApiResponse.error("Địa chỉ chi tiết phải từ 5 ký tự trở lên."));
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
                    JsonUtil.writeJson(resp, ApiResponse.ok(Map.of("address", addr)));
                } else {
                    JsonUtil.writeJson(resp, ApiResponse.error("Không thể lưu địa chỉ vào cơ sở dữ liệu."));
                }
                return;
            }

            if ("update".equals(action)) {
                String addressIdStr = req.getParameter("addressId");
                if (addressIdStr == null || addressIdStr.trim().isEmpty()) {
                    JsonUtil.writeJson(resp, ApiResponse.error("Mã địa chỉ không hợp lệ."));
                    return;
                }
                int addressId = Integer.parseInt(addressIdStr.trim());
                UserAddress addr = addressDAO.findById(addressId);
                if (addr == null || addr.getUserId() != user.getUserId()) {
                    JsonUtil.writeJson(resp, ApiResponse.error("Không tìm thấy hoặc không có quyền chỉnh sửa địa chỉ này."));
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
                    JsonUtil.writeJson(resp, ApiResponse.error("Họ và tên người nhận phải từ 3 ký tự trở lên."));
                    return;
                }
                phone = ValidationUtil.normalizePhone(phone);
                if (!ValidationUtil.isValidPhone(phone)) {
                    JsonUtil.writeJson(resp, ApiResponse.error("Số điện thoại không hợp lệ (VN 10 chữ số)."));
                    return;
                }
                if (detail.length() < 5) {
                    JsonUtil.writeJson(resp, ApiResponse.error("Địa chỉ chi tiết phải từ 5 ký tự trở lên."));
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
                    JsonUtil.writeJson(resp, ApiResponse.ok(Map.of("address", addr)));
                } else {
                    JsonUtil.writeJson(resp, ApiResponse.error("Không thể cập nhật địa chỉ."));
                }
                return;
            }

            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(resp, ApiResponse.error("Hành động không hợp lệ."));
        } catch (Exception e) {
            util.ServletUtil.sendJsonInternalServerError(
                    req,
                    resp,
                    java.util.logging.Logger.getLogger(AddressAPIServlet.class.getName()),
                    "AddressAPIServlet#doPost",
                    "Lỗi hệ thống khi xử lý địa chỉ.",
                    e);
        }
    }
}

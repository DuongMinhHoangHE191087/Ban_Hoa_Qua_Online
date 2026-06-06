package com.fruitmkt.servlet.common;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.model.entity.UserAddress;
import com.fruitmkt.model.entity.Order;
import com.fruitmkt.model.entity.OrderItem;
import com.fruitmkt.model.entity.PaymentTransaction;
import com.fruitmkt.dao.UserAddressDAO;
import com.fruitmkt.dao.OrderDAO;
import com.fruitmkt.dao.PaymentDAO;
import com.fruitmkt.dao.ShopProfileDAO;
import com.fruitmkt.model.entity.ShopProfile;
import com.fruitmkt.service.UserService;
import com.fruitmkt.service.AuthService;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.util.FileUploadUtil;
import com.fruitmkt.util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/profile")
@MultipartConfig(
    maxFileSize = 5242880L,      // 5MB
    maxRequestSize = 20971520L,  // 20MB
    fileSizeThreshold = 1048576  // 1MB
)
public class UserProfileServlet extends HttpServlet {
    private final UserService userService = new UserService();
    private final AuthService authService = new AuthService();
    private final UserAddressDAO addressDAO = new UserAddressDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final ShopProfileDAO shopProfileDAO = new ShopProfileDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = SessionUtil.getCurrentUser(req.getSession());
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        try {
            // Lấy lại user mới nhất từ DB
            User dbUser = userService.findById(currentUser.getUserId());
            req.setAttribute("user", dbUser);

            // Load Address Book
            List<UserAddress> addresses = addressDAO.findByUser(dbUser.getUserId());
            req.setAttribute("addresses", addresses);

            // Load Orders & Item snapshots
            List<Order> orders = orderDAO.findByCustomer(dbUser.getUserId(), 1, 100);
            req.setAttribute("orders", orders);

            Map<Integer, List<OrderItem>> orderItemsMap = new HashMap<>();
            Map<Integer, String> shopNamesMap = new HashMap<>();
            for (Order o : orders) {
                orderItemsMap.put(o.getOrderId(), orderDAO.findItemsByOrderId(o.getOrderId()));
                try {
                    // Ưu tiên lấy shopName từ shop_owner_profiles; fallback về fullName
                    List<ShopProfile> shopProfiles = shopProfileDAO.findByUserId(o.getOwnerId());
                    if (!shopProfiles.isEmpty() && shopProfiles.get(0).getShopName() != null) {
                        shopNamesMap.put(o.getOrderId(), shopProfiles.get(0).getShopName());
                    } else {
                        User owner = userService.findById(o.getOwnerId());
                        shopNamesMap.put(o.getOrderId(), owner != null ? owner.getFullName() : "Cửa hàng");
                    }
                } catch (Exception ignored) {}
            }
            req.setAttribute("orderItemsMap", orderItemsMap);
            req.setAttribute("shopNamesMap", shopNamesMap);

            // Load Payment Transactions
            List<PaymentTransaction> payments = paymentDAO.findByCustomer(dbUser.getUserId());
            req.setAttribute("payments", payments);

            req.getRequestDispatcher("/WEB-INF/jsp/common/profile.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi tải thông tin cá nhân");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = SessionUtil.getCurrentUser(req.getSession());
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        try {
            User dbUser = userService.findById(currentUser.getUserId());

            // 1. Kiểm tra CSRF token thủ công tại Servlet để tăng tính bảo mật
            String sessionCsrf = (String) req.getSession().getAttribute(AppConfig.SESSION_CSRF_TOKEN);
            String reqCsrf = req.getParameter("_csrf");
            if (sessionCsrf == null || !sessionCsrf.equals(reqCsrf)) {
                throw new Exception("CSRF token không hợp lệ hoặc phiên làm việc đã hết hạn.");
            }

            if ("updateProfile".equals(action)) {
                String fullName = req.getParameter("fullName");
                String phone = req.getParameter("phone");

                // Phòng chống bypass HTML: kiểm tra email gửi lên nếu có phải trùng khớp với email trong DB
                String submittedEmail = req.getParameter("email");
                if (submittedEmail != null && !submittedEmail.trim().isEmpty() && !submittedEmail.trim().equalsIgnoreCase(dbUser.getEmail())) {
                    throw new Exception("Không được phép thay đổi địa chỉ Email đăng nhập!");
                }

                fullName = fullName != null ? fullName.replaceAll("<[^>]*>", "").trim() : "";
                phone = phone != null ? phone.replaceAll("<[^>]*>", "").trim() : "";

                ValidationUtil.requireNotBlank(fullName, "Họ và tên");
                phone = ValidationUtil.normalizePhone(phone);
                if (phone != null && !phone.isEmpty()) {
                    ValidationUtil.requireValidPhone(phone, "Số điện thoại");
                    if (userService.isPhoneTakenByAnother(phone, dbUser.getUserId())) {
                        throw new Exception("Số điện thoại này đã được đăng ký bởi tài khoản khác!");
                    }
                }

                Part avatarPart = req.getPart("avatar");
                if (avatarPart != null && avatarPart.getSize() > 0) {
                    String uploadDir = getServletContext().getRealPath("");
                    String savedPath = FileUploadUtil.save(avatarPart, uploadDir);
                    if (savedPath != null) {
                        String oldAvatar = dbUser.getAvatarUrl();
                        if (oldAvatar != null && !oldAvatar.equals("assets/images/default-avatar.svg") && !oldAvatar.startsWith("http")) {
                            FileUploadUtil.delete(uploadDir + File.separator + oldAvatar);
                        }
                        dbUser.setAvatarUrl(savedPath);
                    }
                }

                dbUser.setFullName(fullName);
                dbUser.setPhone(phone);

                userService.updateUser(dbUser);
                SessionUtil.setCurrentUser(req.getSession(), dbUser);
                SessionUtil.flashSuccess(req.getSession(), "Cập nhật thông tin cá nhân thành công!");

            } else if ("addAddress".equals(action)) {
                String name = req.getParameter("recipientName");
                String phone = req.getParameter("recipientPhone");
                String detail = req.getParameter("addressDetail");
                boolean isDefault = "true".equals(req.getParameter("isDefault"));

                name = name != null ? name.replaceAll("<[^>]*>", "").trim() : "";
                phone = phone != null ? phone.replaceAll("<[^>]*>", "").trim() : "";
                detail = detail != null ? detail.replaceAll("<[^>]*>", "").trim() : "";

                ValidationUtil.requireNotBlank(name, "Tên người nhận");
                ValidationUtil.requireValidPhone(phone, "Số điện thoại nhận hàng");
                ValidationUtil.requireValidAddress(detail, "Địa chỉ chi tiết");

                if (isDefault) {
                    addressDAO.clearDefault(dbUser.getUserId());
                }

                UserAddress addr = new UserAddress();
                addr.setUserId(dbUser.getUserId());
                addr.setRecipientName(name);
                addr.setRecipientPhone(phone);
                addr.setAddressDetail(detail);
                addr.setDefault(isDefault);

                addressDAO.save(addr);
                SessionUtil.flashSuccess(req.getSession(), "Thêm địa chỉ giao hàng thành công!");

            } else if ("updateAddress".equals(action)) {
                // Compatibility layer for legacy single address update or book edit
                String addressIdStr = req.getParameter("addressId");
                if (addressIdStr == null || addressIdStr.trim().isEmpty()) {
                    // Update legacy profile single address column
                    String address = req.getParameter("userAddress");
                    address = address != null ? address.replaceAll("<[^>]*>", "").trim() : "";
                    ValidationUtil.requireValidAddress(address, "Địa chỉ giao hàng");
                    dbUser.setUserAddress(address);
                    userService.updateUser(dbUser);
                    SessionUtil.setCurrentUser(req.getSession(), dbUser);
                    SessionUtil.flashSuccess(req.getSession(), "Cập nhật địa chỉ mặc định thành công!");
                } else {
                    int addressId = Integer.parseInt(addressIdStr);
                    UserAddress addr = addressDAO.findById(addressId);
                    if (addr == null || addr.getUserId() != dbUser.getUserId()) {
                        throw new Exception("Không có quyền chỉnh sửa địa chỉ này.");
                    }

                    String name = req.getParameter("recipientName");
                    String phone = req.getParameter("recipientPhone");
                    String detail = req.getParameter("addressDetail");
                    boolean isDefault = "true".equals(req.getParameter("isDefault"));

                    name = name != null ? name.replaceAll("<[^>]*>", "").trim() : "";
                    phone = phone != null ? phone.replaceAll("<[^>]*>", "").trim() : "";
                    detail = detail != null ? detail.replaceAll("<[^>]*>", "").trim() : "";

                    ValidationUtil.requireNotBlank(name, "Tên người nhận");
                    ValidationUtil.requireValidPhone(phone, "Số điện thoại nhận hàng");
                    ValidationUtil.requireValidAddress(detail, "Địa chỉ chi tiết");

                    if (isDefault) {
                        addressDAO.clearDefault(dbUser.getUserId());
                    }

                    addr.setRecipientName(name);
                    addr.setRecipientPhone(phone);
                    addr.setAddressDetail(detail);
                    addr.setDefault(isDefault);

                    addressDAO.update(addr);
                    SessionUtil.flashSuccess(req.getSession(), "Cập nhật địa chỉ giao hàng thành công!");
                }

            } else if ("deleteAddress".equals(action)) {
                int addressId = Integer.parseInt(req.getParameter("addressId"));
                UserAddress addr = addressDAO.findById(addressId);
                if (addr == null || addr.getUserId() != dbUser.getUserId()) {
                    throw new Exception("Không có quyền xóa địa chỉ này.");
                }

                addressDAO.delete(addressId);
                SessionUtil.flashSuccess(req.getSession(), "Xóa địa chỉ giao hàng thành công!");

            } else if ("setDefaultAddress".equals(action)) {
                int addressId = Integer.parseInt(req.getParameter("addressId"));
                UserAddress addr = addressDAO.findById(addressId);
                if (addr == null || addr.getUserId() != dbUser.getUserId()) {
                    throw new Exception("Không thể thực hiện hành động.");
                }

                addressDAO.clearDefault(dbUser.getUserId());
                addr.setDefault(true);
                addressDAO.update(addr);
                SessionUtil.flashSuccess(req.getSession(), "Đã đặt làm địa chỉ mặc định!");

            } else if ("changePassword".equals(action)) {
                String currentPassword = req.getParameter("currentPassword");
                String newPassword = req.getParameter("newPassword");
                String confirmPassword = req.getParameter("confirmPassword");

                if (currentPassword == null || currentPassword.isEmpty() ||
                    newPassword == null || newPassword.isEmpty() ||
                    confirmPassword == null || confirmPassword.isEmpty()) {
                    throw new Exception("Vui lòng điền đầy đủ thông tin mật khẩu!");
                }

                if (!newPassword.equals(confirmPassword)) {
                    throw new Exception("Mật khẩu mới và xác nhận mật khẩu không khớp!");
                }

                authService.changePassword(dbUser.getUserId(), currentPassword, newPassword);
                SessionUtil.flashSuccess(req.getSession(), "Đổi mật khẩu tài khoản thành công!");

            } else if ("cancelOrder".equals(action)) {
                String orderIdStr = req.getParameter("orderId");
                if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
                    throw new Exception("Mã đơn hàng không hợp lệ!");
                }
                int orderId = Integer.parseInt(orderIdStr.trim());
                Order targetOrder = orderDAO.findByIdForCustomer(orderId, dbUser.getUserId());
                if (targetOrder == null) {
                    throw new Exception("Không tìm thấy đơn hàng hoặc bạn không có quyền thao tác đơn hàng này.");
                }
                if (!"PENDING_PAYMENT".equals(targetOrder.getStatus())) {
                    throw new Exception("Chỉ có thể hủy đơn hàng ở trạng thái 'Chờ thanh toán'.");
                }
                orderDAO.cancel(orderId, dbUser.getUserId(), "Khách hàng tự hủy đơn hàng");
                orderDAO.restoreInventoryStock(orderId);
                SessionUtil.flashSuccess(req.getSession(), "Đã hủy đơn hàng #" + orderId + " thành công. Tồn kho đã được hoàn trả.");

            } else {
                SessionUtil.flashError(req.getSession(), "Hành động không hợp lệ!");
            }
        } catch (Exception e) {
            req.getServletContext().log("UserProfileServlet POST error: " + e.getMessage(), e);
            SessionUtil.flashError(req.getSession(), e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/profile");
    }
}

package servlet.customer.order;

import config.AppConfig;
import exception.BusinessException;
import model.entity.order.Order;
import model.entity.order.OrderItem;
import model.entity.order.ReturnRequest;
import model.entity.auth.User;
import service.order.OrderService;
import service.order.ReturnService;
import util.FileUploadUtil;
import util.SessionUtil;
import util.ErrorMessageUtil;

import util.LoggerUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * ReturnRequestServlet — Quản lý yêu cầu Hủy/Đổi/Trả của khách hàng và Phán quyết của Admin.
 *
 * URL: /returns
 *
 * REF-02: Yêu cầu đổi trả bắt buộc đính kèm ít nhất 1 video HOẶC ít nhất 2 ảnh làm bằng chứng.
 */
@MultipartConfig(
    maxFileSize    = 50L * 1024 * 1024,   // 50 MB mỗi file (video có thể lớn)
    maxRequestSize = 150L * 1024 * 1024,  // 150 MB toàn request
    fileSizeThreshold = 1024 * 1024       // 1 MB — ghi xuống disk nếu vượt
)
@WebServlet("/returns")
public class ReturnRequestServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ReturnRequestServlet.class.getName());

    private final ReturnService returnService = new ReturnService();
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        HttpSession session = req.getSession();
        User user = SessionUtil.getCurrentUser(session);

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        String action = req.getParameter("action");

        // --- Admin view list of return requests ---
        if ("list".equals(action)) {
            if (!AppConfig.ROLE_ADMIN.equals(user.getRole()) && !AppConfig.ROLE_SHOP_OWNER.equals(user.getRole())) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền xem trang này.");
                return;
            }
            try {
                List<ReturnRequest> list;
                if (AppConfig.ROLE_ADMIN.equals(user.getRole())) {
                    list = returnService.getAllRequests();
                } else {
                    list = returnService.getRequestsByOwner(user.getUserId());
                }
                req.setAttribute("returnRequests", list);
                
                if (AppConfig.ROLE_ADMIN.equals(user.getRole())) {
                    req.getRequestDispatcher("/WEB-INF/jsp/admin/return-requests.jsp").forward(req, resp);
                } else {
                    req.getRequestDispatcher("/WEB-INF/jsp/shop/return-requests.jsp").forward(req, resp);
                }
            } catch (SQLException e) {
                LoggerUtil.error(log, "Lỗi truy vấn dữ liệu yêu cầu hoàn trả", e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi truy vấn dữ liệu.");
            }
            return;
        }

        // --- Customer creates return request form ---
        String orderIdStr = req.getParameter("orderId");
        if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
            SessionUtil.flashError(session, "Mã đơn hàng không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/home");
            return;
        }

        try {
            int orderId = Integer.parseInt(orderIdStr);
            Order order = orderService.getOrderDetail(orderId);
            if (order == null) {
                SessionUtil.flashError(session, "Không tìm thấy đơn hàng.");
                resp.sendRedirect(req.getContextPath() + "/home");
                return;
            }

            // Guard: chỉ customer sở hữu đơn mới được gửi yêu cầu đổi trả
            if (order.getCustomerId() != user.getUserId()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện thao tác này.");
                return;
            }

            // Guard: chỉ đơn hàng đã giao thành công (DELIVERED) mới được gửi yêu cầu đổi trả
            if (!"DELIVERED".equals(order.getStatus())) {
                SessionUtil.flashError(session, "Chỉ có đơn hàng giao thành công mới được yêu cầu đổi trả / hoàn tiền.");
                resp.sendRedirect(req.getContextPath() + "/orders");
                return;
            }

            List<OrderItem> items = orderService.getOrderItems(orderId);
            req.setAttribute("order", order);
            req.setAttribute("orderItems", items);

            req.getRequestDispatcher("/WEB-INF/jsp/customer/return-request.jsp").forward(req, resp);

        } catch (Exception e) {
            String userMsg = ErrorMessageUtil.logAndGetUserMessage(log, "Failed to show return request form", e);
            SessionUtil.flashError(session, userMsg);
            resp.sendRedirect(req.getContextPath() + "/home");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        User user = SessionUtil.getCurrentUser(session);

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        String action = req.getParameter("action");

        // --- Admin phán quyết duyệt/từ chối ---
        if ("decide".equals(action)) {
            if (!AppConfig.ROLE_ADMIN.equals(user.getRole())) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ Admin mới có quyền phê duyệt đổi trả.");
                return;
            }

            String reqIdStr = req.getParameter("requestId");
            String status = req.getParameter("status"); // APPROVED hoặc REJECTED
            String reason = req.getParameter("reason");

            if (reqIdStr == null || status == null || reason == null || reason.trim().isEmpty()) {
                SessionUtil.flashError(session, "Vui lòng nhập đầy đủ thông tin quyết định.");
                resp.sendRedirect(req.getContextPath() + "/returns?action=list");
                return;
            }

            try {
                int requestId = Integer.parseInt(reqIdStr);
                returnService.decide(requestId, status, reason, user.getUserId());
                SessionUtil.flashSuccess(session, "Đã cập nhật phán quyết cho yêu cầu #" + requestId);
            } catch (Exception e) {
                String userMsg = ErrorMessageUtil.logAndGetUserMessage(log, "Failed to decide return request", e);
                SessionUtil.flashError(session, userMsg);
            }
            resp.sendRedirect(req.getContextPath() + "/returns?action=list");
            return;
        }

        // --- Customer tạo mới yêu cầu đổi trả ---
        String orderIdStr = req.getParameter("orderId");
        String orderItemIdStr = req.getParameter("orderItemId");
        String requestType = req.getParameter("requestType"); // RETURN hoặc EXCHANGE
        String reasonCode = req.getParameter("reasonCode");
        String description = req.getParameter("description");
        String qtyStr = req.getParameter("requestedQuantity");

        if (orderIdStr == null || orderItemIdStr == null || requestType == null || reasonCode == null
                || description == null || description.trim().isEmpty()) {
            SessionUtil.flashError(session, "Vui lòng điền đầy đủ các thông tin bắt buộc.");
            resp.sendRedirect(req.getContextPath() + "/returns?orderId=" + orderIdStr);
            return;
        }

        try {
            int orderId = Integer.parseInt(orderIdStr);
            int orderItemId = Integer.parseInt(orderItemIdStr);
            int requestedQuantity = qtyStr != null ? Integer.parseInt(qtyStr) : 1;

            // REF-02: Đếm và phân loại bằng chứng từ multipart (ảnh / video)
            Collection<Part> evidenceParts = req.getParts();
            int photoCount = 0;
            int videoCount = 0;
            String firstEvidenceUrl = null;

            String uploadDir = getServletContext().getRealPath("");

            for (Part part : evidenceParts) {
                if (!"evidence".equals(part.getName())) {
                    continue; // chỉ xử lý field có name="evidence"
                }
                if (part.getSize() == 0 || part.getSubmittedFileName() == null
                        || part.getSubmittedFileName().trim().isEmpty()) {
                    continue; // bỏ qua slot rỗng
                }

                String filename = part.getSubmittedFileName().toLowerCase();
                if (isVideoExtension(filename)) {
                    videoCount++;
                    if (firstEvidenceUrl == null) {
                        // Lưu video bằng FileUploadUtil nếu extension là ảnh được phép;
                        // video thường không nằm trong ALLOWED_IMAGE_EXTS nên lưu tên file thôi
                        // (multi-row storage bị DEFER — xem ghi chú bên dưới)
                        firstEvidenceUrl = part.getSubmittedFileName();
                    }
                } else if (FileUploadUtil.isAllowedImage(filename)) {
                    photoCount++;
                    if (firstEvidenceUrl == null) {
                        // Lưu ảnh đầu tiên qua FileUploadUtil (validate extension + magic bytes)
                        String saved = FileUploadUtil.save(part, uploadDir);
                        if (saved != null) {
                            firstEvidenceUrl = saved;
                        }
                    }
                }
                // File không phải ảnh và không phải video — bỏ qua
            }

            // REF-02: Kiểm tra điều kiện bằng chứng bắt buộc
            if (videoCount < 1 && photoCount < 2) {
                SessionUtil.flashError(session,
                    "Yêu cầu đổi trả cần đính kèm ít nhất 1 video hoặc ít nhất 2 ảnh làm bằng chứng.");
                resp.sendRedirect(req.getContextPath() + "/returns?orderId=" + orderIdStr);
                return;
            }

            // DEFER: schema hiện tại chỉ có 1 cột evidence_url (VARCHAR).
            // Multi-row evidence storage cần migration (return_request_evidence table).
            // Hiện tại lưu bằng chứng đầu tiên — count đã được kiểm tra ở trên.
            ReturnRequest rr = new ReturnRequest();
            rr.setOrderId(orderId);
            rr.setOrderItemId(orderItemId);
            rr.setCustomerId(user.getUserId());
            rr.setRequestType(requestType);
            rr.setReasonCode(reasonCode);
            rr.setDescription(description);
            rr.setEvidenceUrl(firstEvidenceUrl);
            rr.setRequestedQuantity(requestedQuantity);

            int reqId = returnService.createRequest(rr);
            if (reqId > 0) {
                SessionUtil.flashSuccess(session, "Gửi yêu cầu đổi trả hàng thành công. Vui lòng chờ Admin xử lý.");
            } else {
                SessionUtil.flashError(session, "Tạo yêu cầu thất bại.");
            }
        } catch (BusinessException e) {
            SessionUtil.flashError(session, e.getMessage());
        } catch (NumberFormatException e) {
            LoggerUtil.warn(log, "Tham số không hợp lệ khi tạo return request orderId=" + orderIdStr, e);
            SessionUtil.flashError(session, "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.");
        } catch (Exception e) {
            String userMsg = ErrorMessageUtil.logAndGetUserMessage(log, "Failed to create return request for orderId=" + orderIdStr, e);
            SessionUtil.flashError(session, userMsg);
        }
        resp.sendRedirect(req.getContextPath() + "/returns?orderId=" + orderIdStr);
    }

    private boolean isVideoExtension(String filename) {
        if (filename == null) {
            return false;
        }
        String lower = filename.toLowerCase();
        return lower.endsWith(".mp4") || lower.endsWith(".webm") || lower.endsWith(".ogg");
    }
}

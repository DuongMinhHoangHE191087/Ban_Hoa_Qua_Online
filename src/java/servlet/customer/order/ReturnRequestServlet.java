package servlet.customer.order;

import exception.BusinessException;
import model.entity.order.Order;
import model.entity.order.OrderItem;
import model.entity.order.ReturnRequest;
import model.entity.auth.User;
import service.order.OrderService;
import service.order.ReturnService;
import util.ActorAccessPolicy;
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
 * ReturnRequestServlet - Quản lý yêu cầu Hủy/Đổi/Trả của khách hàng và Phán quyết của Admin.
 *
 * URL: /returns
 *
 * REF-02: Yêu cầu đổi trả bắt buộc đính kèm ít nhất 1 video HOẶC ít nhất 2 ảnh làm bằng chứng.
 */
@MultipartConfig(
    maxFileSize    = 50L * 1024 * 1024,
    maxRequestSize = 150L * 1024 * 1024,
    fileSizeThreshold = 1024 * 1024
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
            if (!ActorAccessPolicy.canViewReturnRequestList(user)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền xem trang này.");
                return;
            }
            try {
                if (ActorAccessPolicy.isAdmin(user)) {
                    resp.sendRedirect(req.getContextPath() + "/admin/refunds");
                    return;
                }

                List<ReturnRequest> list = returnService.getRequestsByOwner(user.getUserId());
                req.setAttribute("returnRequests", list);
                req.getRequestDispatcher("/WEB-INF/jsp/shop/return-requests.jsp").forward(req, resp);
            } catch (SQLException e) {
                util.ServletUtil.sendPageInternalServerError(
                        req,
                        resp,
                        java.util.logging.Logger.getLogger(ReturnRequestServlet.class.getName()),
                        "ReturnRequestServlet#doGet",
                        "Lỗi truy vấn dữ liệu.",
                        e);
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

            if (!ActorAccessPolicy.isOrderOwnedByUser(order, user)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện thao tác này.");
                return;
            }

            if (!ActorAccessPolicy.isDelivered(order)) {
                SessionUtil.flashError(session, "Chỉ có đơn hàng giao thành công mới được gửi yêu cầu đổi trả / hoàn tiền.");
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

        // --- Admin decision approve/reject ---
        if ("decide".equals(action)) {
            if (!ActorAccessPolicy.canDecideReturnRequest(user)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ Admin mới có quyền phê duyệt đổi trả.");
                return;
            }

            String reqIdStr = req.getParameter("requestId");
            String status = req.getParameter("status");
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

        // --- Customer create new return request ---
        if (!ActorAccessPolicy.canCreateReturnRequest(user)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ khách hàng mới có quyền gửi yêu cầu đổi trả.");
            return;
        }

        String orderIdStr = req.getParameter("orderId");
        String orderItemIdStr = req.getParameter("orderItemId");
        String requestType = req.getParameter("requestType");
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

            Collection<Part> evidenceParts = req.getParts();
            int photoCount = 0;
            int videoCount = 0;
            String firstEvidenceUrl = null;

            String uploadDir = getServletContext().getRealPath("");

            for (Part part : evidenceParts) {
                if (!"evidence".equals(part.getName())) {
                    continue;
                }
                if (part.getSize() == 0 || part.getSubmittedFileName() == null
                        || part.getSubmittedFileName().trim().isEmpty()) {
                    continue;
                }

                String filename = part.getSubmittedFileName().toLowerCase();
                if (isVideoExtension(filename)) {
                    videoCount++;
                    if (firstEvidenceUrl == null) {
                        firstEvidenceUrl = part.getSubmittedFileName();
                    }
                } else if (FileUploadUtil.isAllowedImage(filename)) {
                    photoCount++;
                    if (firstEvidenceUrl == null) {
                        String saved = FileUploadUtil.save(part, uploadDir);
                        if (saved != null) {
                            firstEvidenceUrl = saved;
                        }
                    }
                }
            }

            if (videoCount < 1 && photoCount < 2) {
                SessionUtil.flashError(session,
                    "Yêu cầu đổi trả cần đính kèm ít nhất 1 video hoặc ít nhất 2 ảnh làm bằng chứng.");
                resp.sendRedirect(req.getContextPath() + "/returns?orderId=" + orderIdStr);
                return;
            }

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
            SessionUtil.flashError(session, util.ErrorMessageUtil.getUserMessage(e));
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

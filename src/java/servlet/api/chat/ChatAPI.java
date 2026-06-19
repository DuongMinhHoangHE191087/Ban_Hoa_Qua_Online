package servlet.api.chat;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.chat.ChatDAO;
import model.entity.chat.ChatMessage;
import model.entity.chat.ChatSession;
import model.entity.auth.User;
import model.response.ApiResponse;
import service.chat.ChatDeliveryService;
import util.JsonUtil;
import util.LoggerUtil;
import util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * ChatAPI - REST endpoints hỗ trợ HTTP fallback và các thao tác chat.
 */
@WebServlet("/api/chat")
public class ChatAPI extends HttpServlet {

    private static final Logger log = Logger.getLogger(ChatAPI.class.getName());

    private final ChatDAO chatDAO = new ChatDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ChatDeliveryService chatDeliveryService = new ChatDeliveryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            User currentUser = SessionUtil.getCurrentUser(request.getSession());
            if (currentUser == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập"));
                return;
            }

            String action = request.getParameter("action");
            if ("getMessages".equals(action)) {
                int sessionId = Integer.parseInt(request.getParameter("sessionId"));

                ChatSession session = chatDAO.findSessionById(sessionId);
                if (session == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_NOT_FOUND, "Session không tồn tại"));
                    return;
                }

                int uid = currentUser.getUserId();
                boolean isAdmin = AppConfig.ROLE_ADMIN.equals(currentUser.getRole());
                boolean isParticipant = session.getCustomerId() == uid || session.getOwnerId() == uid;
                if (!isParticipant && !isAdmin) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_FORBIDDEN, "Không có quyền truy cập"));
                    return;
                }

                chatDAO.markRead(sessionId, currentUser.getUserId());
                List<ChatMessage> messages = chatDAO.findMessages(sessionId);

                // Che giấu tên thật của Admin đối với Customer và Shop Owner
                if (!isAdmin) {
                    for (ChatMessage msg : messages) {
                        if ("ADMIN".equals(msg.getSenderRole())) {
                            msg.setSenderName("Hỗ trợ Admin");
                        }
                    }
                }

                response.setStatus(HttpServletResponse.SC_OK);
                JsonUtil.writeJson(response, ApiResponse.ok(Map.of(
                    "messages", messages,
                    "currentUserId", currentUser.getUserId()
                )));
            } else if ("getOnlineStatus".equals(action)) {
                int targetSessionId = Integer.parseInt(request.getParameter("sessionId"));
                int targetUserId = Integer.parseInt(request.getParameter("userId"));
                boolean online = websocket.ChatEndpoint.isUserOnline(targetSessionId, targetUserId);
                response.setStatus(HttpServletResponse.SC_OK);
                JsonUtil.writeJson(response, ApiResponse.ok(Map.of("online", online)));
            } else if ("getSessions".equals(action)) {
                List<ChatSession> sessions;
                String role = currentUser.getRole();
                if (AppConfig.ROLE_SHOP_OWNER.equals(role)) {
                    sessions = chatDAO.findSessionsByOwner(currentUser.getUserId());
                } else if (AppConfig.ROLE_CUSTOMER.equals(role)) {
                    sessions = chatDAO.findSessionsByCustomer(currentUser.getUserId());
                } else if (AppConfig.ROLE_ADMIN.equals(role)) {
                    sessions = chatDAO.findAllSessions();
                } else {
                    sessions = new ArrayList<>();
                }
                response.setStatus(HttpServletResponse.SC_OK);
                JsonUtil.writeJson(response, ApiResponse.ok(Map.of(
                    "sessions", sessions,
                    "currentUserId", currentUser.getUserId()
                )));
            } else if ("markRead".equals(action)) {
                int targetSessionId = Integer.parseInt(request.getParameter("sessionId"));
                chatDAO.markRead(targetSessionId, currentUser.getUserId());
                response.setStatus(HttpServletResponse.SC_OK);
                JsonUtil.writeJson(response, ApiResponse.ok(Map.of("message", "Đã đánh dấu đã đọc")));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Hành động không hợp lệ"));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Tham số không hợp lệ"));
        } catch (Exception e) {
            util.ServletUtil.sendJsonInternalServerError(
                    request,
                    response,
                    log,
                    "ChatAPI#doGet",
                    "Lỗi server: " + e.getMessage(),
                    e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            User currentUser = SessionUtil.getCurrentUser(request.getSession());
            if (currentUser == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập"));
                return;
            }

            // CSRF validation
            String sessionToken = (String) request.getSession().getAttribute(AppConfig.SESSION_CSRF_TOKEN);
            String requestToken = request.getParameter("_csrf");
            if (requestToken == null || requestToken.trim().isEmpty()) {
                requestToken = request.getHeader("X-CSRF-Token");
            }
            if (requestToken == null || requestToken.trim().isEmpty()) {
                requestToken = request.getHeader("X-XSRF-TOKEN");
            }
            if (sessionToken == null || !sessionToken.equals(requestToken)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_FORBIDDEN, "CSRF token không hợp lệ"));
                return;
            }

            String action = request.getParameter("action");
            if ("sendMessage".equals(action)) {
                int sessionId = Integer.parseInt(request.getParameter("sessionId"));
                String content = request.getParameter("content");

                ChatSession session = chatDAO.findSessionById(sessionId);
                if (session == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_NOT_FOUND, "Session không tồn tại"));
                    return;
                }

                int uid = currentUser.getUserId();
                boolean isAdmin = AppConfig.ROLE_ADMIN.equals(currentUser.getRole());
                boolean isParticipant = session.getCustomerId() == uid || session.getOwnerId() == uid;
                if (!isParticipant && !isAdmin) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_FORBIDDEN, "Không có quyền gửi vào session này"));
                    return;
                }

                String mediaUrl = request.getParameter("mediaUrl");
                String mediaType = request.getParameter("mediaType");

                try {
                    chatDeliveryService.sendMessage(
                            sessionId,
                            currentUser.getUserId(),
                            currentUser.getRole(),
                            currentUser.getFullName(),
                            content,
                            mediaUrl,
                            mediaType
                    );
                    response.setStatus(HttpServletResponse.SC_OK);
                    JsonUtil.writeJson(response, ApiResponse.ok(Map.of("message", "Đã gửi tin nhắn")));
                } catch (IllegalArgumentException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
                }
            } else if ("createAdminSession".equals(action)) {
                if (AppConfig.ROLE_ADMIN.equals(currentUser.getRole())) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Admin không thể tạo phiên hỗ trợ với chính mình"));
                    return;
                }

                int adminId = 0;
                String adminIdStr = request.getParameter("adminId");
                if (adminIdStr != null && !adminIdStr.trim().isEmpty()) {
                    adminId = Integer.parseInt(adminIdStr);
                    User requestedAdmin = userDAO.findActiveAdminById(adminId);
                    if (requestedAdmin == null) {
                        adminId = 0;
                    }
                }

                if (adminId <= 0) {
                    User fallbackAdmin = userDAO.findFirstActiveAdmin();
                    if (fallbackAdmin != null) {
                        adminId = fallbackAdmin.getUserId();
                    }
                }

                if (adminId <= 0) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy tài khoản Admin hỗ trợ"));
                    return;
                }

                List<ChatSession> existing = chatDAO.findSessionByParticipants(currentUser.getUserId(), adminId);
                int sessionId;
                if (!existing.isEmpty()) {
                    sessionId = existing.get(0).getSessionId();
                } else {
                    sessionId = chatDAO.createSession(currentUser.getUserId(), adminId, "ADMIN");
                }
                response.setStatus(HttpServletResponse.SC_OK);
                JsonUtil.writeJson(response, ApiResponse.ok(Map.of("sessionId", sessionId)));
            } else if ("createShopSession".equals(action)) {
                String ownerIdStr = request.getParameter("ownerId");
                if (ownerIdStr == null || ownerIdStr.trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Thiếu thông tin ownerId"));
                    return;
                }
                int ownerId = Integer.parseInt(ownerIdStr.trim());
                User requestedOwner = userDAO.findUserById(ownerId);
                if (requestedOwner == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Cửa hàng không tồn tại hoặc không hợp lệ"));
                    return;
                }

                List<ChatSession> existing = chatDAO.findSessionByParticipants(currentUser.getUserId(), ownerId);
                int sessionId;
                if (!existing.isEmpty()) {
                    sessionId = existing.get(0).getSessionId();
                } else {
                    sessionId = chatDAO.createSession(currentUser.getUserId(), ownerId, "SHOP");
                }
                response.setStatus(HttpServletResponse.SC_OK);
                JsonUtil.writeJson(response, ApiResponse.ok(Map.of("sessionId", sessionId)));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Hành động không hợp lệ"));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Tham số không hợp lệ"));
        } catch (Exception e) {
            util.ServletUtil.sendJsonInternalServerError(
                    request,
                    response,
                    log,
                    "ChatAPI#doPost",
                    "Lỗi server: " + e.getMessage(),
                    e);
        }
    }
}

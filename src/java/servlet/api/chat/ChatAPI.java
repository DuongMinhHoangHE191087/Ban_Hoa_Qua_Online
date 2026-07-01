package servlet.api.chat;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.chat.ChatDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.entity.auth.User;
import model.entity.chat.ChatSession;
import model.response.ApiResponse;
import service.chat.ChatDeliveryService;
import service.chat.ChatReadService;
import util.JsonUtil;
import util.SessionUtil;

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
    private final ChatReadService chatReadService = new ChatReadService();

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
            if ("bootstrap".equals(action)) {
                Integer requestedSessionId = parseOptionalInt(request.getParameter("sessionId"));
                Integer requestedLimit = parseOptionalInt(request.getParameter("limit"));
                Map<String, Object> payload = chatReadService.loadBootstrap(currentUser, requestedSessionId, requestedLimit);
                setMessageCountMetric(request, payload.get("messages"));
                response.setStatus(HttpServletResponse.SC_OK);
                JsonUtil.writeJson(response, ApiResponse.ok(payload));
                return;
            }

            if ("getMessages".equals(action)) {
                int sessionId = Integer.parseInt(request.getParameter("sessionId"));
                Integer beforeMessageId = parseOptionalInt(request.getParameter("beforeMessageId"));
                Integer requestedLimit = parseOptionalInt(request.getParameter("limit"));
                Map<String, Object> payload = chatReadService.loadMessagePage(currentUser, sessionId, beforeMessageId, requestedLimit);
                setMessageCountMetric(request, payload.get("messages"));
                response.setStatus(HttpServletResponse.SC_OK);
                JsonUtil.writeJson(response, ApiResponse.ok(payload));
                return;
            }

            if ("getOnlineStatus".equals(action)) {
                int targetSessionId = Integer.parseInt(request.getParameter("sessionId"));
                int targetUserId = Integer.parseInt(request.getParameter("userId"));
                boolean online = websocket.ChatEndpoint.isUserOnline(targetSessionId, targetUserId);
                response.setStatus(HttpServletResponse.SC_OK);
                JsonUtil.writeJson(response, ApiResponse.ok(Map.of("online", online)));
                return;
            }

            if ("getSessions".equals(action)) {
                List<ChatSession> sessions = loadSessions(currentUser);
                maskAdminSessionNames(currentUser, sessions);
                response.setStatus(HttpServletResponse.SC_OK);
                JsonUtil.writeJson(response, ApiResponse.ok(Map.of(
                        "sessions", sessions,
                        "currentUserId", currentUser.getUserId()
                )));
                return;
            }

            if ("markRead".equals(action)) {
                int targetSessionId = Integer.parseInt(request.getParameter("sessionId"));
                chatDAO.markRead(targetSessionId, currentUser.getUserId());
                response.setStatus(HttpServletResponse.SC_OK);
                JsonUtil.writeJson(response, ApiResponse.ok(Map.of("message", "Đã đánh dấu đã đọc")));
                return;
            }

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Hành động không hợp lệ"));
        } catch (ChatReadService.ChatApiException e) {
            response.setStatus(e.getStatusCode());
            JsonUtil.writeJson(response, ApiResponse.fail(e.getStatusCode(), e.getMessage()));
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

            String sessionToken = (String) request.getSession().getAttribute(AppConfig.SESSION_CSRF_TOKEN);
            String requestToken = resolveCsrfToken(request);
            if (sessionToken == null || !sessionToken.equals(requestToken)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_FORBIDDEN, "CSRF token không hợp lệ"));
                return;
            }

            String action = request.getParameter("action");
            if ("sendMessage".equals(action)) {
                int sessionId = Integer.parseInt(request.getParameter("sessionId"));
                String content = request.getParameter("content");
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
                return;
            }

            if ("createAdminSession".equals(action)) {
                if (AppConfig.ROLE_ADMIN.equals(currentUser.getRole())) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Admin không thể tạo phiên hỗ trợ với chính mình"));
                    return;
                }

                int adminId = 0;
                Integer requestedAdminId = parseOptionalInt(request.getParameter("adminId"));
                if (requestedAdminId != null && requestedAdminId > 0) {
                    User requestedAdmin = userDAO.findActiveAdminById(requestedAdminId);
                    if (requestedAdmin != null) {
                        adminId = requestedAdmin.getUserId();
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
                int sessionId = existing.isEmpty()
                        ? chatDAO.createSession(currentUser.getUserId(), adminId, "ADMIN")
                        : existing.get(0).getSessionId();

                response.setStatus(HttpServletResponse.SC_OK);
                JsonUtil.writeJson(response, ApiResponse.ok(Map.of("sessionId", sessionId)));
                return;
            }

            if ("createShopSession".equals(action)) {
                Integer ownerId = parseOptionalInt(request.getParameter("ownerId"));
                if (ownerId == null || ownerId <= 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Thiếu thông tin ownerId"));
                    return;
                }

                User requestedOwner = userDAO.findUserById(ownerId);
                if (requestedOwner == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Cửa hàng không tồn tại hoặc không hợp lệ"));
                    return;
                }

                List<ChatSession> existing = chatDAO.findSessionByParticipants(currentUser.getUserId(), ownerId);
                int sessionId = existing.isEmpty()
                        ? chatDAO.createSession(currentUser.getUserId(), ownerId, "SHOP")
                        : existing.get(0).getSessionId();

                response.setStatus(HttpServletResponse.SC_OK);
                JsonUtil.writeJson(response, ApiResponse.ok(Map.of("sessionId", sessionId)));
                return;
            }

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Hành động không hợp lệ"));
        } catch (ChatReadService.ChatApiException e) {
            response.setStatus(e.getStatusCode());
            JsonUtil.writeJson(response, ApiResponse.fail(e.getStatusCode(), e.getMessage()));
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

    private List<ChatSession> loadSessions(User currentUser) throws Exception {
        String role = currentUser.getRole();
        if (AppConfig.ROLE_SHOP_OWNER.equals(role)) {
            return chatDAO.findSessionsByOwner(currentUser.getUserId());
        }
        if (AppConfig.ROLE_CUSTOMER.equals(role)) {
            return chatDAO.findSessionsByCustomer(currentUser.getUserId());
        }
        if (AppConfig.ROLE_ADMIN.equals(role)) {
            return chatDAO.findAllSessions();
        }
        return new ArrayList<>();
    }

    private void maskAdminSessionNames(User currentUser, List<ChatSession> sessions) {
        if (currentUser == null || AppConfig.ROLE_ADMIN.equals(currentUser.getRole()) || sessions == null) {
            return;
        }
        for (ChatSession session : sessions) {
            if ("ADMIN".equalsIgnoreCase(session.getSessionType())) {
                session.setPartnerName("Hỗ trợ Admin");
            }
        }
    }

    private void setMessageCountMetric(HttpServletRequest request, Object messages) {
        if (messages instanceof List<?> list) {
            request.setAttribute("requestMetricsItemCount", list.size());
        }
    }

    private String resolveCsrfToken(HttpServletRequest request) {
        String requestToken = request.getParameter("_csrf");
        if (requestToken == null || requestToken.trim().isEmpty()) {
            requestToken = request.getHeader("X-CSRF-Token");
        }
        if (requestToken == null || requestToken.trim().isEmpty()) {
            requestToken = request.getHeader("X-XSRF-TOKEN");
        }
        return requestToken;
    }

    private Integer parseOptionalInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return Integer.parseInt(value.trim());
    }
}

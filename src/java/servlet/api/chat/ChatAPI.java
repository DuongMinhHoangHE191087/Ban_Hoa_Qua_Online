package servlet.api.chat;

import config.AppConfig;
import dao.chat.ChatDAO;
import model.entity.chat.ChatMessage;
import model.entity.chat.ChatSession;
import model.entity.auth.User;
import model.response.ApiResponse;
import util.JsonUtil;
import util.LoggerUtil;
import util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Hành động không hợp lệ"));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Tham số không hợp lệ"));
        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi server khi xử lý GET chat", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server: " + e.getMessage()));
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

                if ((content == null || content.trim().isEmpty()) && (mediaUrl == null || mediaUrl.trim().isEmpty())) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Nội dung tin nhắn hoặc tệp đính kèm không được rỗng"));
                } else {
                    ChatMessage msg = new ChatMessage();
                    msg.setSessionId(sessionId);
                    msg.setSenderId(currentUser.getUserId());
                    msg.setContent(content != null ? content.trim() : null);
                    msg.setMediaUrl(mediaUrl != null ? mediaUrl.trim() : null);
                    msg.setMediaType(mediaType != null ? mediaType.trim() : null);
                    msg.setIsRead(false);
                    chatDAO.saveMessage(msg);
                    response.setStatus(HttpServletResponse.SC_OK);
                    JsonUtil.writeJson(response, ApiResponse.ok(Map.of("message", "Đã gửi tin nhắn")));
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
                    try (Connection conn = chatDAO.getConnection();
                         PreparedStatement ps = conn.prepareStatement(
                                 "SELECT COUNT(*) FROM users WHERE user_id = ? AND role = 'ADMIN' AND status = 'ACTIVE'")) {
                        ps.setInt(1, adminId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next() || rs.getInt(1) == 0) {
                                adminId = 0;
                            }
                        }
                    }
                }

                if (adminId <= 0) {
                    try (Connection conn = chatDAO.getConnection();
                         PreparedStatement ps = conn.prepareStatement(
                                 "SELECT TOP 1 user_id FROM users WHERE role = 'ADMIN' AND status = 'ACTIVE' ORDER BY user_id ASC")) {
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                adminId = rs.getInt("user_id");
                            }
                        }
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
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Hành động không hợp lệ"));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Tham số không hợp lệ"));
        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi server khi xử lý POST chat", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server: " + e.getMessage()));
        }
    }
}

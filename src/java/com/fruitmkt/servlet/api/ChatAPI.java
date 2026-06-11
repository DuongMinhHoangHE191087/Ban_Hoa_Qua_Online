package com.fruitmkt.servlet.api;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.ChatDAO;
import com.fruitmkt.model.entity.ChatMessage;
import com.fruitmkt.model.entity.ChatSession;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.util.JsonUtil;
import com.fruitmkt.util.LoggerUtil;
import com.fruitmkt.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
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
        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();

        try {
            User currentUser = SessionUtil.getCurrentUser(request.getSession());
            if (currentUser == null) {
                result.put("success", false);
                result.put("message", "Vui lòng đăng nhập");
                out.print(JsonUtil.toJson(result));
                return;
            }

            String action = request.getParameter("action");
            if ("getMessages".equals(action)) {
                int sessionId = Integer.parseInt(request.getParameter("sessionId"));

                ChatSession session = chatDAO.findSessionById(sessionId);
                if (session == null) {
                    result.put("success", false);
                    result.put("message", "Session không tồn tại");
                    out.print(JsonUtil.toJson(result));
                    return;
                }

                int uid = currentUser.getUserId();
                boolean isAdmin = AppConfig.ROLE_ADMIN.equals(currentUser.getRole());
                boolean isParticipant = session.getCustomerId() == uid || session.getOwnerId() == uid;
                if (!isParticipant && !isAdmin) {
                    result.put("success", false);
                    result.put("message", "Không có quyền truy cập");
                    out.print(JsonUtil.toJson(result));
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

                result.put("success", true);
                result.put("messages", messages);
                result.put("currentUserId", currentUser.getUserId());
            } else {
                result.put("success", false);
                result.put("message", "Hành động không hợp lệ");
            }

            out.print(JsonUtil.toJson(result));
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "Tham số không hợp lệ");
            try {
                out.print(JsonUtil.toJson(result));
            } catch (Exception writeEx) {
                LoggerUtil.warn(log, "Không thể ghi JSON lỗi tham số", writeEx);
            }
        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi server khi xử lý GET chat", e);
            result.clear();
            result.put("success", false);
            result.put("message", "Lỗi server: " + e.getMessage());
            try {
                out.print(JsonUtil.toJson(result));
            } catch (Exception writeEx) {
                LoggerUtil.warn(log, "Không thể ghi JSON lỗi server", writeEx);
            }
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();

        try {
            User currentUser = SessionUtil.getCurrentUser(request.getSession());
            if (currentUser == null) {
                result.put("success", false);
                result.put("message", "Vui lòng đăng nhập");
                out.print(JsonUtil.toJson(result));
                return;
            }

            String action = request.getParameter("action");
            if ("sendMessage".equals(action)) {
                int sessionId = Integer.parseInt(request.getParameter("sessionId"));
                String content = request.getParameter("content");

                ChatSession session = chatDAO.findSessionById(sessionId);
                if (session == null) {
                    result.put("success", false);
                    result.put("message", "Session không tồn tại");
                    out.print(JsonUtil.toJson(result));
                    return;
                }

                int uid = currentUser.getUserId();
                boolean isAdmin = AppConfig.ROLE_ADMIN.equals(currentUser.getRole());
                boolean isParticipant = session.getCustomerId() == uid || session.getOwnerId() == uid;
                if (!isParticipant && !isAdmin) {
                    result.put("success", false);
                    result.put("message", "Không có quyền gửi vào session này");
                    out.print(JsonUtil.toJson(result));
                    return;
                }

                String mediaUrl = request.getParameter("mediaUrl");
                String mediaType = request.getParameter("mediaType");

                if ((content == null || content.trim().isEmpty()) && (mediaUrl == null || mediaUrl.trim().isEmpty())) {
                    result.put("success", false);
                    result.put("message", "Nội dung tin nhắn hoặc tệp đính kèm không được rỗng");
                } else {
                    ChatMessage msg = new ChatMessage();
                    msg.setSessionId(sessionId);
                    msg.setSenderId(currentUser.getUserId());
                    msg.setContent(content != null ? content.trim() : null);
                    msg.setMediaUrl(mediaUrl != null ? mediaUrl.trim() : null);
                    msg.setMediaType(mediaType != null ? mediaType.trim() : null);
                    msg.setIsRead(false);
                    chatDAO.saveMessage(msg);
                    result.put("success", true);
                    result.put("message", "Đã gửi tin nhắn");
                }
            } else if ("createAdminSession".equals(action)) {
                if (AppConfig.ROLE_ADMIN.equals(currentUser.getRole())) {
                    result.put("success", false);
                    result.put("message", "Admin không thể tạo phiên hỗ trợ với chính mình");
                    out.print(JsonUtil.toJson(result));
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
                    result.put("success", false);
                    result.put("message", "Không tìm thấy tài khoản Admin hỗ trợ");
                    out.print(JsonUtil.toJson(result));
                    return;
                }

                List<ChatSession> existing = chatDAO.findSessionByParticipants(currentUser.getUserId(), adminId);
                int sessionId;
                if (!existing.isEmpty()) {
                    sessionId = existing.get(0).getSessionId();
                } else {
                    sessionId = chatDAO.createSession(currentUser.getUserId(), adminId, "ADMIN");
                }
                result.put("success", true);
                result.put("sessionId", sessionId);
            } else {
                result.put("success", false);
                result.put("message", "Hành động không hợp lệ");
            }

            out.print(JsonUtil.toJson(result));
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "Tham số không hợp lệ");
            try {
                out.print(JsonUtil.toJson(result));
            } catch (Exception writeEx) {
                LoggerUtil.warn(log, "Không thể ghi JSON lỗi tham số", writeEx);
            }
        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi server khi xử lý POST chat", e);
            result.clear();
            result.put("success", false);
            result.put("message", "Lỗi server: " + e.getMessage());
            try {
                out.print(JsonUtil.toJson(result));
            } catch (Exception writeEx) {
                LoggerUtil.warn(log, "Không thể ghi JSON lỗi server", writeEx);
            }
        } finally {
            out.flush();
        }
    }
}

package com.fruitmkt.servlet.api;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.util.JsonUtil;
import com.fruitmkt.dao.ChatDAO;
import com.fruitmkt.model.entity.ChatMessage;
import com.fruitmkt.model.entity.ChatSession;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.*;

/**
 * ChatAPI — REST endpoints hỗ trợ HTTP fallback và các thao tác chat.
 *
 * GET  /api/chat?action=getMessages&sessionId=X  → lấy tin nhắn + đánh dấu đã đọc
 * POST /api/chat?action=sendMessage              → gửi tin nhắn (HTTP fallback khi WS chưa kết nối)
 * POST /api/chat?action=createAdminSession       → tạo session Customer↔Admin
 *
 * @author fruitmkt-team
 */
@WebServlet("/api/chat")
public class ChatAPI extends HttpServlet {

    private final ChatDAO chatDAO = new ChatDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
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

                // [FIX bug#3] IDOR check: xác thực user thuộc session này mới được đọc
                ChatSession session = chatDAO.findSessionById(sessionId);
                if (session == null) {
                    result.put("success", false);
                    result.put("message", "Session không tồn tại");
                    out.print(JsonUtil.toJson(result));
                    return;
                }
                int uid = currentUser.getUserId();
                boolean isAdmin = AppConfig.ROLE_ADMIN.equals(currentUser.getRole());
                boolean isParticipant = (session.getCustomerId() == uid || session.getOwnerId() == uid);
                if (!isParticipant && !isAdmin) {
                    result.put("success", false);
                    result.put("message", "Không có quyền truy cập");
                    out.print(JsonUtil.toJson(result));
                    return;
                }

                // Mark messages as read by current user
                chatDAO.markRead(sessionId, currentUser.getUserId());
                List<ChatMessage> messages = chatDAO.findMessages(sessionId);
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
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.clear();
            result.put("success", false);
            result.put("message", "Lỗi server: " + e.getMessage());
            try { out.print(JsonUtil.toJson(result)); } catch (Exception ignored) {}
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
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
                // HTTP fallback khi WebSocket chưa kết nối được
                int sessionId = Integer.parseInt(request.getParameter("sessionId"));
                String content = request.getParameter("content");

                // IDOR check
                ChatSession session = chatDAO.findSessionById(sessionId);
                if (session == null) {
                    result.put("success", false);
                    result.put("message", "Session không tồn tại");
                    out.print(JsonUtil.toJson(result));
                    return;
                }
                int uid = currentUser.getUserId();
                boolean isAdmin = AppConfig.ROLE_ADMIN.equals(currentUser.getRole());
                boolean isParticipant = (session.getCustomerId() == uid || session.getOwnerId() == uid);
                if (!isParticipant && !isAdmin) {
                    result.put("success", false);
                    result.put("message", "Không có quyền gửi vào session này");
                    out.print(JsonUtil.toJson(result));
                    return;
                }

                if (content == null || content.trim().isEmpty()) {
                    result.put("success", false);
                    result.put("message", "Nội dung tin nhắn không được rỗng");
                } else {
                    ChatMessage msg = new ChatMessage();
                    msg.setSessionId(sessionId);
                    msg.setSenderId(currentUser.getUserId());
                    msg.setContent(content.trim());
                    msg.setIsRead(false);
                    chatDAO.saveMessage(msg);
                    result.put("success", true);
                    result.put("message", "Đã gửi tin nhắn");
                }

            } else if ("createAdminSession".equals(action)) {
                // Tạo session Customer↔Admin — chỉ CUSTOMER mới gọi được
                if (!AppConfig.ROLE_CUSTOMER.equals(currentUser.getRole())) {
                    result.put("success", false);
                    result.put("message", "Chỉ khách hàng mới có thể tạo session với Admin");
                    out.print(JsonUtil.toJson(result));
                    return;
                }
                int adminId = 0;
                String adminIdStr = request.getParameter("adminId");
                if (adminIdStr != null && !adminIdStr.trim().isEmpty()) {
                    adminId = Integer.parseInt(adminIdStr);
                    // Kiểm tra xem adminId này có thực sự là admin và active không
                    try (Connection conn = chatDAO.getConnection();
                         PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE user_id = ? AND role = 'ADMIN' AND status = 'ACTIVE'")) {
                        ps.setInt(1, adminId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next() || rs.getInt(1) == 0) {
                                adminId = 0; // Reset để tìm admin khác
                            }
                        }
                    }
                }
                
                if (adminId <= 0) {
                    // Tìm admin đầu tiên trong DB
                    try (Connection conn = chatDAO.getConnection();
                         PreparedStatement ps = conn.prepareStatement("SELECT TOP 1 user_id FROM users WHERE role = 'ADMIN' AND status = 'ACTIVE' ORDER BY user_id ASC")) {
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

                // Kiểm tra session ADMIN đã tồn tại chưa
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
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.clear();
            result.put("success", false);
            result.put("message", "Lỗi server: " + e.getMessage());
            try { out.print(JsonUtil.toJson(result)); } catch (Exception ignored) {}
        } finally {
            out.flush();
        }
    }
}

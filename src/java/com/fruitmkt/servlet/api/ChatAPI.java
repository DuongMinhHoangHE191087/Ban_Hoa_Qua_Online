package com.fruitmkt.servlet.api;

import com.fruitmkt.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fruitmkt.dao.ChatDAO;
import com.fruitmkt.model.entity.ChatMessage;
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

@WebServlet("/api/chat")
public class ChatAPI extends HttpServlet {

    private final ChatDAO chatDAO = new ChatDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
            String action = request.getParameter("action");
            if ("getMessages".equals(action)) {
                int sessionId = Integer.parseInt(request.getParameter("sessionId"));
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
        } catch (Exception e) {
            e.printStackTrace();
            result.clear();
            result.put("success", false);
            result.put("message", "Lỗi server: " + e.getMessage());
            try {
                out.print(JsonUtil.toJson(result));
            } catch (Exception ignored) {}
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
                int sessionId = Integer.parseInt(request.getParameter("sessionId"));
                String content = request.getParameter("content");

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
            } else {
                result.put("success", false);
                result.put("message", "Hành động không hợp lệ");
            }
            out.print(JsonUtil.toJson(result));
        } catch (Exception e) {
            e.printStackTrace();
            result.clear();
            result.put("success", false);
            result.put("message", "Lỗi server: " + e.getMessage());
            try {
                out.print(JsonUtil.toJson(result));
            } catch (Exception ignored) {}
        } finally {
            out.flush();
        }
    }
}

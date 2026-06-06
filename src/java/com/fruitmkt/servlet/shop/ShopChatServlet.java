package com.fruitmkt.servlet.shop;

import com.fruitmkt.dao.ChatDAO;
import com.fruitmkt.model.entity.ChatSession;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/shop/chat")
public class ShopChatServlet extends HttpServlet {

    private final ChatDAO chatDAO = new ChatDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User shopOwner = SessionUtil.getCurrentUser(req.getSession());
        if (shopOwner == null || (!"SHOP".equals(shopOwner.getRole()) && !"ADMIN".equals(shopOwner.getRole()))) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        try {
            int activeSessionId = -1;
            String sessionIdStr = req.getParameter("sessionId");
            if (sessionIdStr != null && !sessionIdStr.trim().isEmpty()) {
                activeSessionId = Integer.parseInt(sessionIdStr);
            }

            List<ChatSession> sessions = chatDAO.findSessionsByOwner(shopOwner.getUserId());
            if (activeSessionId == -1 && !sessions.isEmpty()) {
                activeSessionId = sessions.get(0).getSessionId();
            }

            req.setAttribute("chatSessions", sessions);
            req.setAttribute("activeSessionId", activeSessionId);
            
            req.getRequestDispatcher("/WEB-INF/jsp/shop/chat.jsp").forward(req, resp);

        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi lấy dữ liệu chat");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Vui lòng gọi qua API");
    }
}

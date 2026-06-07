package com.fruitmkt.servlet.customer;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.dao.ChatDAO;
import com.fruitmkt.dao.UserDAO;
import com.fruitmkt.model.entity.ChatSession;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/chat")
public class ChatServlet extends HttpServlet {

    private final ChatDAO chatDAO = new ChatDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User customer = SessionUtil.getCurrentUser(req.getSession());
        if (customer == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        try {
            String shopIdStr = req.getParameter("shopId");
            int activeSessionId = -1;

            if (shopIdStr != null && !shopIdStr.trim().isEmpty()) {
                int shopId = Integer.parseInt(shopIdStr);
                // Check if session exists
                List<ChatSession> existing = chatDAO.findSessionByParticipants(customer.getUserId(), shopId);
                if (existing.isEmpty()) {
                    // Create new
                    activeSessionId = chatDAO.createSession(customer.getUserId(), shopId);
                } else {
                    activeSessionId = existing.get(0).getSessionId();
                }
            }

            // Get all sessions for sidebar
            List<ChatSession> sessions = chatDAO.findSessionsByCustomer(customer.getUserId());
            
            // If shopId was not provided, but they have sessions, open the latest one
            if (activeSessionId == -1 && !sessions.isEmpty()) {
                activeSessionId = sessions.get(0).getSessionId();
            }

            req.setAttribute("chatSessions", sessions);
            req.setAttribute("activeSessionId", activeSessionId);
            
            req.getRequestDispatcher("/WEB-INF/jsp/customer/chat.jsp").forward(req, resp);

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

package servlet.customer.chat;

import dao.chat.ChatDAO;
import dao.auth.UserDAO;
import model.entity.chat.ChatSession;
import model.entity.auth.User;
import util.ActorAccessPolicy;
import util.SessionUtil;

import util.LoggerUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * ChatServlet — Trang chat của Customer.
 * URL: /chat[?shopId=X]
 *
 * [UPGRADE]: findSessionsByCustomer JOIN trả về partner_name (tên shop/admin)
 * Thêm nút "Chat với Admin" qua createAdminSession API.
 *
 * @author fruitmkt-team
 */
@WebServlet("/chat")
public class ChatServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ChatServlet.class.getName());

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
        // Chỉ CUSTOMER được vào màn hình này
        if (!ActorAccessPolicy.canAccessCustomerArea(customer)) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        try {
            String shopIdStr  = req.getParameter("shopId");
            String sessionIdStr = req.getParameter("sessionId");
            int activeSessionId = -1;

            if (shopIdStr != null && !shopIdStr.trim().isEmpty()) {
                // Mở hoặc tạo session với shop
                int shopId = Integer.parseInt(shopIdStr);
                List<ChatSession> existing = chatDAO.findSessionByParticipants(customer.getUserId(), shopId);
                if (existing.isEmpty()) {
                    activeSessionId = chatDAO.createSession(customer.getUserId(), shopId, "SHOP");
                } else {
                    activeSessionId = existing.get(0).getSessionId();
                }
            } else if (sessionIdStr != null && !sessionIdStr.trim().isEmpty()) {
                // Mở session trực tiếp từ ID (dùng khi click từ notification)
                activeSessionId = Integer.parseInt(sessionIdStr);
            }

            // [UPGRADE]: JOIN query trả về partner_name
            List<ChatSession> sessions = chatDAO.findSessionsByCustomer(customer.getUserId());

            // Nếu chưa có sessionId, mặc định mở session đầu tiên
            if (activeSessionId == -1 && !sessions.isEmpty()) {
                activeSessionId = sessions.get(0).getSessionId();
            }

            req.setAttribute("chatSessions", sessions);
            req.setAttribute("activeSessionId", activeSessionId);

            ChatSession activeSession = null;
            for (ChatSession s : sessions) {
                if (s.getSessionId() == activeSessionId) {
                    activeSession = s;
                    break;
                }
            }
            if (activeSession == null && activeSessionId > 0) {
                activeSession = chatDAO.findSessionById(activeSessionId);
                if (activeSession != null) {
                    User partner = userDAO.findUserById(activeSession.getOwnerId());
                    if (partner != null) {
                        activeSession.setPartnerName(partner.getFullName());
                        activeSession.setPartnerAvatar(partner.getAvatarUrl());
                    }
                }
            }
            req.setAttribute("activeSession", activeSession);

            req.getRequestDispatcher("/WEB-INF/jsp/customer/chat.jsp").forward(req, resp);

        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/chat");
        } catch (SQLException e) {
            util.ServletUtil.sendPageInternalServerError(
                    req,
                    resp,
                    java.util.logging.Logger.getLogger(ChatServlet.class.getName()),
                    "ChatServlet#doGet",
                    "Lỗi khi lấy dữ liệu chat",
                    e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Vui lòng gọi qua API");
    }
}

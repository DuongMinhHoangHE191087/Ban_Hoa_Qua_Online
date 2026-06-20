package servlet.shop.chat;

import config.AppConfig;
import dao.chat.ChatDAO;
import dao.auth.UserDAO;
import model.entity.chat.ChatSession;
import model.entity.auth.User;
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
 * ShopChatServlet — Trang quản lý tin nhắn của Shop Owner.
 * URL: /shop/chat[?sessionId=X]
 *
 * [FIX bug#2]: role check đổi từ "SHOP" → AppConfig.ROLE_SHOP_OWNER ("SHOP_OWNER")
 * [UPGRADE]: findSessionsByOwner JOIN trả về partner_name (tên customer)
 *
 * @author fruitmkt-team
 */
@WebServlet("/shop/chat")
public class ShopChatServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ShopChatServlet.class.getName());

    private final ChatDAO chatDAO = new ChatDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User shopOwner = SessionUtil.getCurrentUser(req.getSession());
        // [FIX bug#2]: Đổi "SHOP" → AppConfig.ROLE_SHOP_OWNER
        if (shopOwner == null
                || (!AppConfig.ROLE_SHOP_OWNER.equals(shopOwner.getRole())
                    && !AppConfig.ROLE_ADMIN.equals(shopOwner.getRole()))) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        try {
            int activeSessionId = -1;
            String sessionIdStr = req.getParameter("sessionId");
            if (sessionIdStr != null && !sessionIdStr.trim().isEmpty()) {
                activeSessionId = Integer.parseInt(sessionIdStr);
            }

            // [UPGRADE]: JOIN trả về partner_name + partner_avatar
            List<ChatSession> sessions = chatDAO.findSessionsByOwner(shopOwner.getUserId());
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
                    User partner = userDAO.findUserById(activeSession.getCustomerId());
                    if (partner != null) {
                        activeSession.setPartnerName(partner.getFullName());
                        activeSession.setPartnerAvatar(partner.getAvatarUrl());
                    }
                }
            }
            req.setAttribute("activeSession", activeSession);

            req.getRequestDispatcher("/WEB-INF/jsp/shop/chat.jsp").forward(req, resp);

        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/shop/chat");
        } catch (SQLException e) {
            util.ServletUtil.sendPageInternalServerError(
                    req,
                    resp,
                    java.util.logging.Logger.getLogger(ShopChatServlet.class.getName()),
                    "ShopChatServlet#doGet",
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

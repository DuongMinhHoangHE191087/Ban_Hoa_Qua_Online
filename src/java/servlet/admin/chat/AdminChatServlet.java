package servlet.admin.chat;

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
 * AdminChatServlet — Trang quản lý chat cho Admin.
 * URL: /admin/chat[?sessionId=X]
 *
 * Admin thấy TẤT CẢ chat sessions (cả SHOP lẫn ADMIN type).
 * Admin có thể tham gia bất kỳ session nào để hỗ trợ.
 *
 * @author fruitmkt-team
 */
@WebServlet("/admin/chat")
public class AdminChatServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AdminChatServlet.class.getName());

    private final ChatDAO chatDAO = new ChatDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User admin = SessionUtil.getCurrentUser(req.getSession());
        if (admin == null || !AppConfig.ROLE_ADMIN.equals(admin.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        try {
            int activeSessionId = -1;
            String sessionIdStr = req.getParameter("sessionId");
            if (sessionIdStr != null && !sessionIdStr.trim().isEmpty()) {
                activeSessionId = Integer.parseInt(sessionIdStr);
            }

            // Admin xem TẤT CẢ sessions
            List<ChatSession> sessions = chatDAO.findAllSessions();
            if (activeSessionId == -1 && !sessions.isEmpty()) {
                activeSessionId = sessions.get(0).getSessionId();
            }

            req.setAttribute("chatSessions", sessions);
            req.setAttribute("activeSessionId", activeSessionId);
            req.setAttribute("adminId", admin.getUserId());

            ChatSession activeSession = null;
            for (ChatSession s : sessions) {
                if (s.getSessionId() == activeSessionId) {
                    activeSession = s;
                    break;
                }
            }
            if (activeSession == null && activeSessionId > 0) {
                activeSession = chatDAO.findSessionById(activeSessionId);
            }
            req.setAttribute("activeSession", activeSession);

            req.getRequestDispatcher("/WEB-INF/jsp/admin/chat.jsp").forward(req, resp);

        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/admin/chat");
        } catch (SQLException e) {
            LoggerUtil.error(log, "Lỗi khi lấy dữ liệu chat", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi lấy dữ liệu chat");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Vui lòng gọi qua API");
    }
}

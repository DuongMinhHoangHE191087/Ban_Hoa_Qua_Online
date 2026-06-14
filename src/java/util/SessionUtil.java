package util;

import config.AppConfig;
import model.entity.auth.User;
import jakarta.servlet.http.HttpSession;

/**
 * SessionUtil — Tiện ích đọc/ghi HTTP Session.
 *
 * CÁCH DÙNG TRONG SERVLET:
 * <pre>
 *   User user = SessionUtil.getCurrentUser(req.getSession());
 *   if (!SessionUtil.isLoggedIn(req.getSession())) {
 *       resp.sendRedirect(req.getContextPath() + "/auth/login");
 *       return;
 *   }
 *   if (!SessionUtil.hasRole(req.getSession(), AppConfig.ROLE_ADMIN)) {
 *       resp.sendError(403);
 *       return;
 *   }
 * </pre>
 *
 * @author fruitmkt-team
 */
public final class SessionUtil {

    /** Lấy User đang đăng nhập từ session. Trả null nếu chưa login. */
    public static User getCurrentUser(HttpSession session) {
        if (session == null) return null;
        return (User) session.getAttribute(AppConfig.SESSION_USER);
    }

    /** Kiểm tra đã đăng nhập chưa */
    public static boolean isLoggedIn(HttpSession session) {
        return getCurrentUser(session) != null;
    }

    /**
     * Kiểm tra user có role cụ thể không.
     * @param role Ví dụ: AppConfig.ROLE_ADMIN
     */
    public static boolean hasRole(HttpSession session, String role) {
        User u = getCurrentUser(session);
        return u != null && role.equals(u.getRole());
    }

    /**
     * Lưu User vào session sau khi đăng nhập thành công.
     * Gọi session.invalidate() trước khi setCurrentUser để tránh session fixation.
     */
    public static void setCurrentUser(HttpSession session, User user) {
        session.setAttribute(AppConfig.SESSION_USER, user);
    }

    /** Đặt flash message để hiển thị sau PRG redirect */
    public static void setFlashMessage(HttpSession session, String message, String type) {
        session.setAttribute(AppConfig.SESSION_FLASH_MSG, message);
        session.setAttribute(AppConfig.SESSION_FLASH_TYPE, type);
    }

    /** Shortcut cho flash thành công */
    public static void flashSuccess(HttpSession session, String message) {
        setFlashMessage(session, message, "success");
    }

    /** Shortcut cho flash lỗi */
    public static void flashError(HttpSession session, String message) {
        setFlashMessage(session, message, "error");
    }

    /** Xóa session khi logout */
    public static void clearSession(HttpSession session) {
        if (session != null) session.invalidate();
    }

    private SessionUtil() {}
}

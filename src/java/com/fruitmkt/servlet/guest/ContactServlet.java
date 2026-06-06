package com.fruitmkt.servlet.guest;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * ContactServlet — Xử lý form liên hệ từ trang Giới thiệu.
 *
 * GET  /contact  → redirect về /about#contact
 * POST /contact  → validate + ghi log + PRG redirect về /about#contact với flash message
 */
@WebServlet("/contact")
public class ContactServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ContactServlet.class.getName());

    /** GET: không có trang riêng, redirect thẳng về form trên about page. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.sendRedirect(req.getContextPath() + "/about#contact");
    }

    /** POST: nhận form liên hệ, validate, ghi log, PRG redirect. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        // ── 1. Đọc các trường form ──────────────────────────────────────────
        String name    = trim(req.getParameter("contactName"));
        String email   = trim(req.getParameter("contactEmail"));
        String phone   = trim(req.getParameter("contactPhone"));
        String subject = trim(req.getParameter("contactSubject"));
        String message = trim(req.getParameter("contactMessage"));

        // ── 2. Validate cơ bản ─────────────────────────────────────────────
        HttpSession session = req.getSession();

        if (name.isEmpty() || email.isEmpty() || message.isEmpty()) {
            session.setAttribute("flashMsg",  "Vui lòng điền đầy đủ họ tên, email và nội dung tin nhắn.");
            session.setAttribute("flashType", "error");
            resp.sendRedirect(req.getContextPath() + "/about#contact");
            return;
        }

        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            session.setAttribute("flashMsg",  "Địa chỉ email không hợp lệ. Vui lòng kiểm tra lại.");
            session.setAttribute("flashType", "error");
            resp.sendRedirect(req.getContextPath() + "/about#contact");
            return;
        }

        if (message.length() > 2000) {
            session.setAttribute("flashMsg",  "Nội dung tin nhắn không được vượt quá 2000 ký tự.");
            session.setAttribute("flashType", "error");
            resp.sendRedirect(req.getContextPath() + "/about#contact");
            return;
        }

        // ── 3. Ghi log yêu cầu liên hệ (thay thế bằng gửi email hoặc lưu DB khi có) ──
        LOG.info(String.format(
            "[CONTACT] name=%s | email=%s | phone=%s | subject=%s | message=%s",
            name, email, phone, subject, message.replace("\n", " ")
        ));

        // ── 4. PRG: flash success + redirect ──────────────────────────────
        session.setAttribute("flashMsg",  "Cảm ơn " + escapeHtml(name) + "! Chúng tôi đã nhận được tin nhắn và sẽ phản hồi trong vòng 24 giờ.");
        session.setAttribute("flashType", "success");
        resp.sendRedirect(req.getContextPath() + "/about#contact");
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}

package com.fruitmkt.test;
import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
@WebServlet("/test-error")
public class ErrorCatcher extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.getRequestDispatcher("/WEB-INF/jsp/admin/admin-categories.jsp").forward(req, resp);
        } catch(Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            resp.setContentType("text/plain");
            resp.getWriter().write(sw.toString());
        }
    }
}

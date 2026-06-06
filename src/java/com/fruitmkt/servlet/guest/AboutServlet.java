package com.fruitmkt.servlet.guest;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * AboutServlet — Controller cho trang Giới thiệu về chúng tôi.
 * 
 * URL: /about
 * GET : Forward tới trang giới thiệu của hệ thống MetaFruit.
 */
@WebServlet("/about")
public class AboutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        
        req.getServletContext().getRequestDispatcher("/WEB-INF/jsp/guest/about.jsp").forward(req, resp);
    }
}

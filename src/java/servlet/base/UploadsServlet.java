package servlet.base;

import config.AppConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * UploadsServlet — Phục vụ hình ảnh và tài liệu tải lên tự động.
 * Đảm bảo file ảnh không bị 404 sau khi clean-build.
 * URL: /uploads/*
 */
@WebServlet("/uploads/*")
public class UploadsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.trim().isEmpty() || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Decode URL để tránh lỗi ký tự đặc biệt hoặc dấu cách
        String relativePath = URLDecoder.decode(pathInfo, StandardCharsets.UTF_8);
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }

        // Chặn Path Traversal
        if (relativePath.contains("..")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Yêu cầu không hợp lệ.");
            return;
        }

        // 1. Thử lấy file ở thư mục lưu trữ bền vững trước
        File file = new File(AppConfig.PERSISTENT_UPLOAD_DIR, relativePath);

        // 2. Nếu không tìm thấy, fallback sang thư mục deploy của webapp (cho các ảnh mặc định/ảnh test)
        if (!file.exists() || !file.isFile()) {
            String webAppRoot = getServletContext().getRealPath("/uploads");
            if (webAppRoot != null) {
                file = new File(webAppRoot, relativePath);
            }
        }

        // 3. Nếu vẫn không tồn tại thì báo 404
        if (!file.exists() || !file.isFile()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy tài nguyên yêu cầu.");
            return;
        }

        // 4. Xác định mime-type và set headers
        String mimeType = getServletContext().getMimeType(file.getName());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        resp.setContentType(mimeType);
        resp.setContentLengthLong(file.length());

        // Hỗ trợ browser caching (30 ngày để tối ưu hóa tốc độ tải ảnh tải lên)
        resp.setHeader("Cache-Control", "public, max-age=2592000"); // 30 ngày

        // 5. Stream nội dung file
        try (FileInputStream in = new FileInputStream(file);
             OutputStream out = resp.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}

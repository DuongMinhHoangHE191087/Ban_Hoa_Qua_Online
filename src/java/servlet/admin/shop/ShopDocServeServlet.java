package servlet.admin.shop;

import config.AppConfig;
import util.SessionUtil;
import util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * ShopDocServeServlet — Serve các file tài liệu xác minh shop cho ADMIN một cách an toàn.
 *
 * URL: /admin/shop-docs?path=uploads/shop-docs/1/uuid.pdf
 *
 * BẢO MẬT:
 * 1. Chỉ tài khoản ADMIN đã đăng nhập mới được truy cập tài liệu.
 * 2. Chặn hoàn toàn Path Traversal (tấn công lùi thư mục bằng ../).
 * 3. Bắt buộc tệp tin phục vụ phải nằm bên trong thư mục uploads/shop-docs.
 * 4. Sử dụng Content-Disposition để tải file an toàn, tránh XSS/HTML execution.
 *
 * @author fruitmkt-team
 */
@WebServlet("/admin/shop-docs")
public class ShopDocServeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Kiểm tra session và quyền ADMIN
        if (!SessionUtil.isLoggedIn(req.getSession())) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        if (!SessionUtil.hasRole(req.getSession(), AppConfig.ROLE_ADMIN)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập tài nguyên này.");
            return;
        }

        // 2. Nhận tham số path và kiểm tra rỗng
        String docPath = req.getParameter("path");
        if (docPath == null || docPath.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu tham số đường dẫn tài liệu.");
            return;
        }

        // Làm sạch chuỗi log
        String logPath = ValidationUtil.sanitizeForLog(docPath);

        // 3. Chặn đứng Path Traversal
        if (docPath.contains("..") || docPath.contains("/") == false && docPath.contains("\\") == false) {
            getServletContext().log("ShopDocServeServlet: Cảnh báo tấn công Path Traversal từ admin. Path: " + logPath);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Yêu cầu không hợp lệ.");
            return;
        }

        // 4. Xác định đường dẫn tuyệt đối trên đĩa và bảo vệ sandbox
        String webAppRoot = getServletContext().getRealPath("");
        File targetFile = new File(webAppRoot, docPath);

        try {
            // Chuẩn hóa canonical path để đối chiếu sandbox
            String canonicalRoot = new File(webAppRoot, AppConfig.UPLOAD_SHOP_DOCS_DIR).getCanonicalPath();
            String canonicalTarget = targetFile.getCanonicalPath();

            if (!canonicalTarget.startsWith(canonicalRoot)) {
                getServletContext().log("ShopDocServeServlet: Chặn truy cập ngoài sandbox! Path: " + logPath);
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Yêu cầu không hợp lệ.");
                return;
            }

            // 5. Kiểm tra file tồn tại
            if (!targetFile.exists() || !targetFile.isFile()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy tài liệu yêu cầu.");
                return;
            }

            // 6. Set headers an toàn
            String mimeType = getServletContext().getMimeType(targetFile.getName());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            resp.setContentType(mimeType);
            resp.setContentLengthLong(targetFile.length());

            // Buộc trình duyệt tải về thay vì hiển thị trực tiếp để tránh XSS
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + targetFile.getName() + "\"");

            // 7. Stream nội dung file ra output
            try (FileInputStream in = new FileInputStream(targetFile);
                 OutputStream out = resp.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

        } catch (IOException e) {
            getServletContext().log("ShopDocServeServlet error serving file: " + logPath, e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi truyền tải tài liệu.");
        }
    }
}

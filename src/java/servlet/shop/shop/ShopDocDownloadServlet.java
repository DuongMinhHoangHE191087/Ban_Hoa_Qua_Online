package servlet.shop.shop;

import config.AppConfig;
import dao.shop.ShopProfileDAO;
import model.entity.auth.User;
import model.entity.shop.ShopProfile;
import util.SessionUtil;
import util.ShopDocDraftUtil;
import util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * ShopDocDownloadServlet — Phục vụ tải xuống tài liệu xác minh shop cho ADMIN và chủ sở hữu của tài liệu.
 * Mapped to /shop/docs
 */
@WebServlet("/shop/docs")
public class ShopDocDownloadServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ShopDocDownloadServlet.class.getName());
    private final ShopProfileDAO shopProfileDAO = new ShopProfileDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User currentUser = SessionUtil.getCurrentUser(session);

        // 1. Check logged in
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        // 2. Get path parameter
        String docPath = req.getParameter("path");
        if (docPath == null || docPath.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu tham số đường dẫn tài liệu.");
            return;
        }

        String logPath = ValidationUtil.sanitizeForLog(docPath);

        // 3. Prevent Path Traversal
        if (docPath.contains("..") || (!docPath.contains("/") && !docPath.contains("\\"))) {
            log.warning("ShopDocDownloadServlet: Canh bao tan cong Path Traversal tu user ID " + currentUser.getUserId() + ". Path: " + logPath);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Yêu cầu không hợp lệ.");
            return;
        }

        // 4. Validate permissions (ADMIN or OWNER of this document)
        boolean hasPermission = false;
        if (SessionUtil.hasRole(session, AppConfig.ROLE_ADMIN)) {
            hasPermission = true;
        } else {
            // Check if user is the owner of this document
            try {
                List<ShopProfile> profiles = shopProfileDAO.findByUserId(currentUser.getUserId());
                ShopProfile profile = profiles.isEmpty() ? null : profiles.get(0);
                if (profile != null) {
                    List<String> docPaths = parseJsonArray(profile.getDocPaths());
                    if (docPaths.contains(docPath)) {
                        hasPermission = true;
                    }
                }
                
                // If not found in DB profile, check draft sessions
                if (!hasPermission) {
                    List<String> draftStatus = ShopDocDraftUtil.getDraftPaths(session, ShopDocDraftUtil.STATUS_SCOPE);
                    List<String> draftApply = ShopDocDraftUtil.getDraftPaths(session, ShopDocDraftUtil.APPLY_SCOPE);
                    List<String> draftRegister = ShopDocDraftUtil.getDraftPaths(session, ShopDocDraftUtil.REGISTER_SCOPE);
                    if (draftStatus.contains(docPath) || draftApply.contains(docPath) || draftRegister.contains(docPath)) {
                        hasPermission = true;
                    }
                }
            } catch (SQLException e) {
                log.severe("ShopDocDownloadServlet DB error checking ownership for user ID " + currentUser.getUserId() + ": " + e.getMessage());
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi hệ thống khi xác minh quyền sở hữu tài liệu.");
                return;
            }
        }

        if (!hasPermission) {
            log.warning("ShopDocDownloadServlet: User ID " + currentUser.getUserId() + " khong co quyen truy cap tai lieu. Path: " + logPath);
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập tài liệu này.");
            return;
        }

        // 5. Locate file on disk
        String webAppRoot = getServletContext().getRealPath("");
        File targetFile = null;

        String relativePath = docPath;
        if (docPath.startsWith(AppConfig.UPLOAD_DIR + "/")) {
            relativePath = docPath.substring(AppConfig.UPLOAD_DIR.length() + 1);
        } else if (docPath.startsWith(AppConfig.UPLOAD_DIR + "\\")) {
            relativePath = docPath.substring(AppConfig.UPLOAD_DIR.length() + 1);
        }

        File persistentFile = new File(AppConfig.PERSISTENT_UPLOAD_DIR, relativePath);
        if (persistentFile.exists() && persistentFile.isFile()) {
            targetFile = persistentFile;
        } else {
            targetFile = new File(webAppRoot, docPath);
        }

        try {
            // Check Sandbox
            String canonicalRoot = new File(webAppRoot, AppConfig.UPLOAD_SHOP_DOCS_DIR).getCanonicalPath();
            File persistentShopDocs = new File(AppConfig.PERSISTENT_UPLOAD_DIR, "shop-docs");
            if (!persistentShopDocs.exists()) {
                persistentShopDocs.mkdirs();
            }
            String canonicalPersistentRoot = persistentShopDocs.getCanonicalPath();
            String canonicalTarget = targetFile.getCanonicalPath();

            if (!canonicalTarget.startsWith(canonicalRoot) && !canonicalTarget.startsWith(canonicalPersistentRoot)) {
                log.warning("ShopDocDownloadServlet: Chan truy cap ngoai sandbox! Path: " + logPath);
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Yêu cầu không hợp lệ.");
                return;
            }

            if (!targetFile.exists() || !targetFile.isFile()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy tài liệu.");
                return;
            }

            // 6. Set Content headers
            String mimeType = getServletContext().getMimeType(targetFile.getName());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            resp.setContentType(mimeType);
            resp.setContentLengthLong(targetFile.length());

            // Avoid arbitrary HTML execution (Content-Disposition: attachment)
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + targetFile.getName() + "\"");

            // 7. Stream file content
            try (FileInputStream in = new FileInputStream(targetFile);
                 OutputStream out = resp.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

        } catch (IOException e) {
            log.severe("ShopDocDownloadServlet file serving error: " + e.getMessage());
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi truyền tải tài liệu.");
        }
    }

    private List<String> parseJsonArray(String json) {
        List<String> values = new ArrayList<>();
        if (json == null) {
            return values;
        }

        String trimmed = json.trim();
        if (trimmed.isEmpty() || "[]".equals(trimmed)) {
            return values;
        }

        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }

        if (trimmed.isEmpty()) {
            return values;
        }

        for (String token : trimmed.split(",")) {
            String item = token.trim();
            if (item.startsWith("\"") && item.endsWith("\"") && item.length() >= 2) {
                item = item.substring(1, item.length() - 1);
            }
            if (item.startsWith("'") && item.endsWith("'") && item.length() >= 2) {
                item = item.substring(1, item.length() - 1);
            }
            if (!item.isEmpty() && !"null".equalsIgnoreCase(item)) {
                values.add(item);
            }
        }

        return values;
    }
}

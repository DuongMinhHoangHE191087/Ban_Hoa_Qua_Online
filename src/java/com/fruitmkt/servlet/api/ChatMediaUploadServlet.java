package com.fruitmkt.servlet.api;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.model.response.ApiResponse;
import com.fruitmkt.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ChatMediaUploadServlet — API tải ảnh và video lên khung chat.
 * Chỉ cho phép định dạng IMAGE và VIDEO.
 * 
 * URL: /api/chat/upload
 * 
 * @author fruitmkt-team
 */
@WebServlet("/api/chat/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,       // 1MB
    maxFileSize = 50 * 1024 * 1024,        // 50MB (Cho video lớn)
    maxRequestSize = 60 * 1024 * 1024      // 60MB
)
public class ChatMediaUploadServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ChatMediaUploadServlet.class.getName());
    private static final String UPLOAD_DIR = "uploads" + File.separator + "chat";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        User user = (User) req.getSession().getAttribute(AppConfig.SESSION_USER);
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJson(resp, ApiResponse.error("Chưa đăng nhập."));
            return;
        }

        try {
            Part filePart = req.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                JsonUtil.writeJson(resp, ApiResponse.error("Không tìm thấy tệp tin tải lên."));
                return;
            }

            String contentType = filePart.getContentType();
            if (contentType == null) {
                JsonUtil.writeJson(resp, ApiResponse.error("Không xác định được loại tệp tin."));
                return;
            }

            String mediaType = null;
            if (contentType.startsWith("image/")) {
                mediaType = "IMAGE";
            } else if (contentType.startsWith("video/")) {
                mediaType = "VIDEO";
            } else {
                JsonUtil.writeJson(resp, ApiResponse.error("Định dạng không được hỗ trợ. Chỉ cho phép tải lên hình ảnh và video."));
                return;
            }

            String appPath = req.getServletContext().getRealPath("");
            String savePath = appPath + File.separator + UPLOAD_DIR;
            File fileSaveDir = new File(savePath);
            if (!fileSaveDir.exists()) {
                fileSaveDir.mkdirs();
            }

            String originalFileName = getFileName(filePart);
            String extension = "";
            int dotIdx = originalFileName.lastIndexOf('.');
            if (dotIdx > 0) {
                extension = originalFileName.substring(dotIdx);
            }

            String extLower = extension.toLowerCase();
            if (mediaType.equals("IMAGE") && !extLower.matches("\\.(jpg|jpeg|png|gif|webp)")) {
                JsonUtil.writeJson(resp, ApiResponse.error("Định dạng ảnh không hợp lệ (chỉ cho phép JPG, PNG, GIF, WEBP)."));
                return;
            }
            if (mediaType.equals("VIDEO") && !extLower.matches("\\.(mp4|webm|ogg)")) {
                JsonUtil.writeJson(resp, ApiResponse.error("Định dạng video không hợp lệ (chỉ cho phép MP4, WEBM, OGG)."));
                return;
            }

            String newFileName = UUID.randomUUID().toString() + extension;
            String filePhysicalPath = savePath + File.separator + newFileName;

            filePart.write(filePhysicalPath);

            String relativeUrl = req.getContextPath() + "/" + UPLOAD_DIR.replace(File.separator, "/") + "/" + newFileName;

            LOG.info("ChatMediaUploadServlet: User #" + user.getUserId() + " uploaded " + mediaType + " successfully: " + relativeUrl);

            java.util.Map<String, Object> data = java.util.Map.of("url", relativeUrl, "type", mediaType);
            JsonUtil.writeJson(resp, ApiResponse.ok(data));

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "ChatMediaUploadServlet: Lỗi xử lý tải lên media", e);
            JsonUtil.writeJson(resp, ApiResponse.error("Lỗi hệ thống khi tải tệp lên."));
        }
    }

    /**
     * Lấy tên file từ header Content-Disposition của Part.
     */
    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return "unknown";
    }
}

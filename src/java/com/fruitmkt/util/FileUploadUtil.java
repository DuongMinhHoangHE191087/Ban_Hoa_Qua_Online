package com.fruitmkt.util;

import com.fruitmkt.config.AppConfig;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * FileUploadUtil — Xử lý upload file ảnh sản phẩm lên server filesystem.
 * Upload dir: {webapp}/uploads/ — cấu hình trong AppConfig.UPLOAD_DIR
 * @author fruitmkt-team
 */
public final class FileUploadUtil {

    /**
     * Lưu Part từ multipart request, trả về đường dẫn tương đối.
     * @param part       jakarta.servlet.http.Part từ request
     * @param uploadDir  Đường dẫn thư mục gốc trên server (thường là getServletContext().getRealPath(""))
     * @return relative path (ví dụ: 'uploads/uuid.jpg') để lưu vào Database
     */
    public static String save(Part part, String uploadDir) throws IOException {
        // 1. Kiểm tra file có rỗng hay không
        if (part == null || part.getSize() == 0) {
            return null; 
        }

        String originalFilename = part.getSubmittedFileName();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return null;
        }

        // 2. Validate extension (Đuôi file)
        if (!isAllowedImage(originalFilename)) {
            throw new IOException("Định dạng file không được phép. Chỉ chấp nhận: " + String.join(", ", AppConfig.ALLOWED_IMAGE_EXTS));
        }

        // 3. Đảm bảo thư mục upload đã tồn tại
        File dir = new File(uploadDir, AppConfig.UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 4. Đổi tên file thành UUID để chống ghi đè và tránh lỗi ký tự đặc biệt
        String extension = getExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + "." + extension;

        // 5. Ghi file trực tiếp xuống đĩa cứng
        Path filePath = Paths.get(dir.getAbsolutePath(), newFilename);
        Files.copy(part.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        // 6. Trả về đường dẫn relative để lưu DB
        return AppConfig.UPLOAD_DIR + "/" + newFilename;
    }

    /** Xóa file upload theo đường dẫn thực tế trên server */
    public static void delete(String realPath) {
        if (realPath == null || realPath.trim().isEmpty()) return;
        try {
            Files.deleteIfExists(Paths.get(realPath));
        } catch (IOException e) {
            System.err.println("Không thể xóa file: " + realPath);
        }
    }

    /** Kiểm tra extension có nằm trong whitelist cấu hình ở AppConfig không */
    public static boolean isAllowedImage(String filename) {
        if (filename == null) return false;
        String ext = getExtension(filename).toLowerCase();
        for (String allowed : AppConfig.ALLOWED_IMAGE_EXTS) {
            if (allowed.equals(ext)) {
                return true;
            }
        }
        return false;
    }
    
    /** Lấy phần đuôi mở rộng của tên file (vd: jpg, png) */
    private static String getExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }

    private FileUploadUtil() {}
}

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

    /**
     * Lưu tài liệu xác minh shop (PDF, JPG, PNG, DOCX) theo userId.
     * Thư mục: {webapp}/uploads/shop-docs/{userId}/
     * Giới hạn kích thước: AppConfig.MAX_SHOP_DOC_SIZE_BYTES (25MB)
     *
     * @param part      Part từ multipart request
     * @param uploadDir Đường dẫn thực của webapp root (getServletContext().getRealPath(""))
     * @param userId    ID người dùng — tạo subfolder riêng biệt
     * @return Đường dẫn relative để lưu DB (ví dụ: 'uploads/shop-docs/42/uuid.pdf')
     */
    public static String saveShopDoc(Part part, String uploadDir, int userId) throws IOException {
        if (part == null || part.getSize() == 0) {
            return null;
        }

        String originalFilename = part.getSubmittedFileName();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return null;
        }

        // Validate kích thước file (25MB)
        if (part.getSize() > AppConfig.MAX_SHOP_DOC_SIZE_BYTES) {
            throw new IOException("File '" + sanitizeFilename(originalFilename)
                    + "' vượt quá giới hạn 25MB.");
        }

        // Validate extension
        if (!isAllowedDoc(originalFilename)) {
            throw new IOException("Định dạng file '" + sanitizeFilename(originalFilename)
                    + "' không được phép. Chỉ chấp nhận: PDF, JPG, PNG, DOCX.");
        }

        // Tạo thư mục uploads/shop-docs/{userId}/ nếu chưa tồn tại
        File dir = new File(uploadDir, AppConfig.UPLOAD_SHOP_DOCS_DIR + File.separator + userId);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Đổi tên file thành UUID để chống ghi đè và path traversal
        String extension = getExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + "." + extension;

        Path filePath = Paths.get(dir.getAbsolutePath(), newFilename);
        Files.copy(part.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        return AppConfig.UPLOAD_SHOP_DOCS_DIR + "/" + userId + "/" + newFilename;
    }

    /** Kiểm tra extension có nằm trong whitelist tài liệu shop không */
    public static boolean isAllowedDoc(String filename) {
        if (filename == null) return false;
        String ext = getExtension(filename).toLowerCase();
        for (String allowed : AppConfig.ALLOWED_DOC_EXTS) {
            if (allowed.equals(ext)) {
                return true;
            }
        }
        return false;
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

    /**
     * Làm sạch tên file để log an toàn (tránh log injection).
     * Chỉ giữ lại ký tự alphanumeric, dấu chấm, gạch ngang, gạch dưới.
     */
    private static String sanitizeFilename(String filename) {
        if (filename == null) return "unknown";
        return filename.replaceAll("[^a-zA-Z0-9._\\-]", "_");
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

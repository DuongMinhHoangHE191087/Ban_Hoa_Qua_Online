package util;

import config.AppConfig;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * FileUploadUtil — Xử lý upload file ảnh sản phẩm lên server filesystem.
 * Upload dir: {webapp}/uploads/ — cấu hình trong AppConfig.UPLOAD_DIR
 * @author fruitmkt-team
 */
public final class FileUploadUtil {

    private static final Logger log = Logger.getLogger(FileUploadUtil.class.getName());

    // Magic bytes (file signatures) cho từng loại file được phép
    private static final Map<String, byte[]> MAGIC_BYTES = Map.of(
        "jpg",  new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF},
        "jpeg", new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF},
        "png",  new byte[]{(byte)0x89, 0x50, 0x4E, 0x47},
        "gif",  new byte[]{0x47, 0x49, 0x46},
        "webp", new byte[]{0x52, 0x49, 0x46, 0x46},  // RIFF header
        "pdf",  new byte[]{0x25, 0x50, 0x44, 0x46}   // %PDF
    );

    /**
     * Kiểm tra magic bytes của file có khớp với extension không.
     * Phòng chống tấn công đổi tên file nguy hiểm (ví dụ: .exe → .jpg).
     */
    private static boolean hasMagicBytesMatchingExtension(Part part, String ext) {
        byte[] expected = MAGIC_BYTES.get(ext.toLowerCase());
        if (expected == null) return true; // DOCX không có magic bytes đơn giản — bỏ qua

        try (InputStream is = part.getInputStream()) {
            byte[] header = new byte[expected.length];
            int read = is.read(header);
            if (read < expected.length) return false;
            for (int i = 0; i < expected.length; i++) {
                if (header[i] != expected[i]) return false;
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

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

        // 2b. Validate magic bytes (chống giả mạo đuôi file)
        String extension = getExtension(originalFilename);
        if (!hasMagicBytesMatchingExtension(part, extension)) {
            throw new IOException("Nội dung file không khớp với định dạng khai báo. Vui lòng upload file hình ảnh hợp lệ.");
        }

        // 3. Đảm bảo thư mục upload đã tồn tại
        File dir = new File(AppConfig.PERSISTENT_UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 4. Đổi tên file thành UUID để chống ghi đè và tránh lỗi ký tự đặc biệt
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
            // Thử xóa từ thư mục bền vững trước
            String fileName = Paths.get(realPath).getFileName().toString();
            File persistentFile = new File(AppConfig.PERSISTENT_UPLOAD_DIR, fileName);
            if (persistentFile.exists()) {
                Files.deleteIfExists(persistentFile.toPath());
            }
            Files.deleteIfExists(Paths.get(realPath));
        } catch (IOException e) {
            LoggerUtil.warn(log, "Không thể xóa file: " + realPath, e);
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

        // Validate magic bytes (chống giả mạo đuôi file)
        String docExt = getExtension(originalFilename);
        if (!hasMagicBytesMatchingExtension(part, docExt)) {
            throw new IOException("Nội dung file '" + sanitizeFilename(originalFilename)
                    + "' không khớp với định dạng khai báo.");
        }

        // Tạo thư mục uploads/shop-docs/{userId}/ nếu chưa tồn tại trong thư mục bền vững
        File dir = new File(AppConfig.PERSISTENT_UPLOAD_DIR, "shop-docs" + File.separator + userId);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Đổi tên file thành UUID để chống ghi đè và path traversal
        String newFilename = UUID.randomUUID().toString() + "." + docExt;

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

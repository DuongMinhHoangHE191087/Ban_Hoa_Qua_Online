package util;

import config.AppConfig;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
        "webp", new byte[]{0x52, 0x49, 0x46, 0x46},  // RIFF header (full WEBP validated in helper)
        "pdf",  new byte[]{0x25, 0x50, 0x44, 0x46},  // %PDF
        // DOCX/XLSX là ZIP — magic bytes: PK\x03\x04
        "docx", new byte[]{0x50, 0x4B, 0x03, 0x04},
        "xlsx", new byte[]{0x50, 0x4B, 0x03, 0x04}
    );

    /**
     * Kiểm tra magic bytes của file có khớp với extension không.
     * Phòng chống tấn công đổi tên file nguy hiểm (ví dụ: .exe → .jpg).
     *
     * Quy tắc:
     *   - Extension có trong MAGIC_BYTES → đọc header và so sánh byte-by-byte.
     *   - Extension KHÔNG có trong MAGIC_BYTES và KHÔNG có trong allowlist → từ chối (false).
     *   - DOCX/XLSX: kiểm tra chữ ký ZIP (PK\x03\x04).
     */
    private static boolean hasMagicBytesMatchingExtension(Part part, String ext) {
        String lowerExt = ext.toLowerCase();
        try (InputStream is = part.getInputStream()) {
            if ("webp".equals(lowerExt)) {
                byte[] header = is.readNBytes(12);
                return header.length >= 12
                        && header[0] == 0x52
                        && header[1] == 0x49
                        && header[2] == 0x46
                        && header[3] == 0x46
                        && header[8] == 0x57
                        && header[9] == 0x45
                        && header[10] == 0x42
                        && header[11] == 0x50;
            }

            byte[] expected = MAGIC_BYTES.get(lowerExt);
            if (expected == null) {
                // Extension không có trong bảng magic bytes → từ chối thay vì bỏ qua
                return false;
            }

            byte[] header = is.readNBytes(expected.length);
            if (header.length < expected.length) return false;
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
            Path target = resolveDeleteTarget(realPath);
            if (target != null) {
                Files.deleteIfExists(target);
            }
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
        File dir = new File(AppConfig.PERSISTENT_UPLOAD_DIR, "shop-docs" + File.separator + userId);
        return storeShopDoc(part, dir, AppConfig.UPLOAD_SHOP_DOCS_DIR + "/" + userId);
    }

    /** Lưu tài liệu xác minh shop vào thư mục nháp theo session token. */
    public static String saveShopDocDraft(Part part, String uploadDir, String scope, String draftToken) throws IOException {
        File dir = new File(AppConfig.PERSISTENT_UPLOAD_DIR,
                "shop-docs" + File.separator + "_draft" + File.separator + scope + File.separator + draftToken);
        return storeShopDoc(part, dir, AppConfig.UPLOAD_SHOP_DOCS_DRAFT_DIR + "/" + scope + "/" + draftToken);
    }

    /** Sao chép tài liệu nháp sang thư mục chính của user và trả về path đích. */
    public static String promoteShopDocDraft(String draftRelativePath, int userId) throws IOException {
        if (draftRelativePath == null || draftRelativePath.trim().isEmpty()) {
            return null;
        }

        Path draftPath = resolveStoredPath(draftRelativePath);
        if (!Files.exists(draftPath) || !Files.isRegularFile(draftPath)) {
            throw new IOException("Không tìm thấy tệp tài liệu nháp để chuyển sang thư mục chính.");
        }

        String ext = getExtension(draftPath.getFileName().toString());
        File targetDir = new File(AppConfig.PERSISTENT_UPLOAD_DIR, "shop-docs" + File.separator + userId);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        String newFilename = UUID.randomUUID().toString() + "." + ext;
        Path targetPath = Paths.get(targetDir.getAbsolutePath(), newFilename);
        Files.copy(draftPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
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

    private static String storeShopDoc(Part part, File dir, String relativePrefix) throws IOException {
        if (part == null || part.getSize() == 0) {
            return null;
        }

        String originalFilename = part.getSubmittedFileName();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return null;
        }

        if (part.getSize() > AppConfig.MAX_SHOP_DOC_SIZE_BYTES) {
            throw new IOException("Tệp '" + sanitizeFilename(originalFilename)
                    + "' vượt quá giới hạn 25MB.");
        }

        if (!isAllowedDoc(originalFilename)) {
            throw new IOException("Tệp '" + sanitizeFilename(originalFilename)
                    + "' không được hỗ trợ. Chỉ chấp nhận: PDF, JPG, PNG, DOCX.");
        }

        String docExt = getExtension(originalFilename);
        if (!hasMagicBytesMatchingExtension(part, docExt)) {
            throw new IOException("Nội dung tệp '" + sanitizeFilename(originalFilename)
                    + "' không khớp với định dạng đã khai báo. Vui lòng chọn lại file gốc.");
        }

        if (!dir.exists()) {
            dir.mkdirs();
        }

        String newFilename = UUID.randomUUID().toString() + "." + docExt;
        Path filePath = Paths.get(dir.getAbsolutePath(), newFilename);
        Files.copy(part.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return relativePrefix + "/" + newFilename;
    }

    private static Path resolveDeleteTarget(String realPath) throws IOException {
        String trimmed = realPath.trim().replace("\\", "/");
        Path candidate = Paths.get(trimmed);
        if (candidate.isAbsolute()) {
            if (Files.exists(candidate)) {
                return candidate.normalize();
            }
            Path fallback = Paths.get(AppConfig.PERSISTENT_UPLOAD_DIR,
                    candidate.getFileName() != null ? candidate.getFileName().toString() : "");
            if (Files.exists(fallback)) {
                return fallback.normalize();
            }
            return candidate.normalize();
        }

        if (trimmed.startsWith(AppConfig.UPLOAD_DIR + "/")) {
            trimmed = trimmed.substring(AppConfig.UPLOAD_DIR.length() + 1);
        }

        Path root = Paths.get(AppConfig.PERSISTENT_UPLOAD_DIR).toAbsolutePath().normalize();
        return root.resolve(trimmed).normalize();
    }

    private static Path resolveStoredPath(String storedPath) throws IOException {
        String trimmed = storedPath.trim().replace("\\", "/");
        Path candidate = Paths.get(trimmed);
        if (candidate.isAbsolute()) {
            return candidate.normalize();
        }

        if (trimmed.startsWith(AppConfig.UPLOAD_DIR + "/")) {
            trimmed = trimmed.substring(AppConfig.UPLOAD_DIR.length() + 1);
        }

        Path root = Paths.get(AppConfig.PERSISTENT_UPLOAD_DIR).toAbsolutePath().normalize();
        Path resolved = root.resolve(trimmed).normalize();
        if (!resolved.startsWith(root)) {
            throw new IOException("Đường dẫn tài liệu không hợp lệ.");
        }
        return resolved;
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

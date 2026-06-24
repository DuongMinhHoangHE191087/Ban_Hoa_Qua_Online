package util;

import config.AppConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * ShopDocDraftUtil — Quản lý tài liệu nháp cho các form đăng ký shop_owner.
 *
 * Mục tiêu:
 * - upload vào thư mục nháp theo session token trước khi commit DB
 * - giữ danh sách file nháp trong session để JSP render lại sau khi forward lỗi
 * - promote sang thư mục chính khi submit thành công
 * - dọn file nháp cũ khi thay thế, submit xong hoặc quá hạn
 */
public final class ShopDocDraftUtil {

    public static final String REGISTER_SCOPE = "register";
    public static final String APPLY_SCOPE = "shopApply";
    public static final String STATUS_SCOPE = "shopStatus";

    private static final String PATHS_PREFIX = "shopDocDraftPaths:";
    private static final String TOKEN_PREFIX = "shopDocDraftToken:";
    private static final String UPDATED_PREFIX = "shopDocDraftUpdatedAt:";
    private static final Logger log = Logger.getLogger(ShopDocDraftUtil.class.getName());

    private ShopDocDraftUtil() {}

    public static List<String> uploadDraftDocs(HttpServletRequest req, String partName, String scope) throws Exception {
        purgeExpiredDrafts();

        HttpSession session = req.getSession();
        String draftToken = getOrCreateDraftToken(session, scope);
        List<Part> docParts = new ArrayList<>();
        for (Part part : req.getParts()) {
            if (!partName.equals(part.getName())) {
                continue;
            }
            if (part.getSize() == 0) {
                continue;
            }
            docParts.add(part);
        }

        if (docParts.isEmpty()) {
            return Collections.emptyList();
        }

        if (docParts.size() > AppConfig.MAX_SHOP_DOC_COUNT) {
            throw new Exception("Chỉ được upload tối đa " + AppConfig.MAX_SHOP_DOC_COUNT + " tài liệu.");
        }

        for (Part part : docParts) {
            String docError = ValidationUtil.validateShopDoc(part.getSubmittedFileName(), part.getSize());
            if (docError != null) {
                throw new Exception(docError);
            }
        }

        List<String> draftPaths = new ArrayList<>(docParts.size());
        for (Part part : docParts) {
            draftPaths.add(FileUploadUtil.saveShopDocDraft(part, req.getServletContext().getRealPath(""), scope, draftToken));
        }
        return draftPaths;
    }

    public static List<String> getDraftPaths(HttpSession session, String scope) {
        if (session == null) {
            return Collections.emptyList();
        }
        Object value = session.getAttribute(pathsKey(scope));
        if (!(value instanceof List<?> rawList)) {
            return Collections.emptyList();
        }
        List<String> paths = new ArrayList<>(rawList.size());
        for (Object item : rawList) {
            if (item instanceof String str && !str.trim().isEmpty()) {
                paths.add(str);
            }
        }
        return paths;
    }

    public static boolean hasDraftDocs(HttpSession session, String scope) {
        return !getDraftPaths(session, scope).isEmpty();
    }

    public static void exposeDraftDocs(HttpSession session, HttpServletRequest req, String scope, String requestAttributeName) {
        req.setAttribute(requestAttributeName, getDraftPaths(session, scope));
    }

    public static void replaceDraftDocs(HttpSession session, String scope, List<String> newDraftPaths) {
        if (session == null || newDraftPaths == null || newDraftPaths.isEmpty()) {
            return;
        }

        List<String> oldDraftPaths = getDraftPaths(session, scope);
        if (!oldDraftPaths.isEmpty()) {
            deletePaths(oldDraftPaths);
        }

        session.setAttribute(pathsKey(scope), new ArrayList<>(newDraftPaths));
        session.setAttribute(updatedKey(scope), System.currentTimeMillis());
        getOrCreateDraftToken(session, scope);
    }

    public static List<String> promoteDraftDocs(HttpSession session, String scope, int userId) throws IOException {
        List<String> draftPaths = getDraftPaths(session, scope);
        if (draftPaths.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> promoted = new ArrayList<>(draftPaths.size());
        try {
            for (String draftPath : draftPaths) {
                promoted.add(FileUploadUtil.promoteShopDocDraft(draftPath, userId));
            }
            return promoted;
        } catch (IOException ex) {
            deletePaths(promoted);
            throw ex;
        }
    }

    public static void clearDraftDocs(HttpSession session, String scope) {
        if (session == null) {
            return;
        }
        deletePaths(getDraftPaths(session, scope));
        session.removeAttribute(pathsKey(scope));
        session.removeAttribute(tokenKey(scope));
        session.removeAttribute(updatedKey(scope));
    }

    public static String getOrCreateDraftToken(HttpSession session, String scope) {
        if (session == null) {
            throw new IllegalArgumentException("Session không được null khi tạo draft token.");
        }
        String key = tokenKey(scope);
        Object existing = session.getAttribute(key);
        if (existing instanceof String token && !token.trim().isEmpty()) {
            return token;
        }
        String token = UUID.randomUUID().toString();
        session.setAttribute(key, token);
        session.setAttribute(updatedKey(scope), System.currentTimeMillis());
        return token;
    }

    public static String toJsonArray(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(value.replace("\"", "\\\"")).append("\"");
            first = false;
        }
        sb.append("]");
        return first ? null : sb.toString();
    }

    public static void purgeExpiredDrafts() {
        Path draftRoot = getDraftRoot();
        if (!Files.exists(draftRoot)) {
            return;
        }

        long cutoff = System.currentTimeMillis() - AppConfig.SHOP_DOC_DRAFT_TTL_MS;
        try (Stream<Path> scopeDirs = Files.list(draftRoot)) {
            for (Path scopeDir : scopeDirs.filter(Files::isDirectory).toList()) {
                try (Stream<Path> tokenDirs = Files.list(scopeDir)) {
                    for (Path tokenDir : tokenDirs.filter(Files::isDirectory).toList()) {
                        FileTime lastModified = Files.getLastModifiedTime(tokenDir);
                        if (lastModified.toMillis() < cutoff) {
                            deleteDirectory(tokenDir);
                        }
                    }
                }
            }
        } catch (IOException e) {
            LoggerUtil.warn(log, "Không thể dọn draft tài liệu cũ.", e);
        }
    }

    private static void deletePaths(List<String> storedPaths) {
        if (storedPaths == null || storedPaths.isEmpty()) {
            return;
        }
        for (String path : storedPaths) {
            FileUploadUtil.delete(path);
        }
    }

    private static void deleteDirectory(Path directory) throws IOException {
        if (directory == null || !Files.exists(directory)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(directory)) {
            walk.sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            LoggerUtil.warn(log, "Không thể xóa file nháp: " + path, e);
                        }
                    });
        }
    }

    private static Path getDraftRoot() {
        return Paths.get(AppConfig.PERSISTENT_UPLOAD_DIR, "shop-docs", "_draft").toAbsolutePath().normalize();
    }

    private static String pathsKey(String scope) {
        return PATHS_PREFIX + normalizeScope(scope);
    }

    private static String tokenKey(String scope) {
        return TOKEN_PREFIX + normalizeScope(scope);
    }

    private static String updatedKey(String scope) {
        return UPDATED_PREFIX + normalizeScope(scope);
    }

    private static String normalizeScope(String scope) {
        if (scope == null || scope.trim().isEmpty()) {
            return "default";
        }
        return scope.trim();
    }
}
